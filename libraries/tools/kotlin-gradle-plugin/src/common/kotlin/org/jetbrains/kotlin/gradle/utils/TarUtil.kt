/*
 * Copyright 2010-2026 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.utils

import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import java.io.BufferedInputStream
import java.io.IOException
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.attribute.PosixFilePermission
import java.util.zip.GZIPInputStream
import kotlin.io.path.inputStream

internal fun Path.unzipTarGz(destinationDirectory: Path) {
    val targetBase = destinationDirectory.normalize().toAbsolutePath()
    createDirectoriesIfMissing(targetBase)

    GZIPInputStream(BufferedInputStream(inputStream())).use { gzipInputStream ->
        val hardLinks = mutableMapOf<Path, Path>()
        // Outward-pointing symlinks are allowed (xcode-addon bundles legitimately point outside
        // the archive root), but tracked so writes through them can be rejected.
        val escapingSymlinks = mutableSetOf<Path>()

        TarArchiveInputStream(gzipInputStream).use { tarInputStream ->
            generateSequence { tarInputStream.nextEntry }
                .forEach { entry: TarArchiveEntry ->
                    // CWE-22: reject ../.. in entry names
                    val outputPath = targetBase.resolve(entry.name).normalize()
                    requireInsideBase(targetBase, outputPath, "Entry '${entry.name}'")

                    when {
                        entry.isDirectory -> {
                            requireNoEscapingAncestor(escapingSymlinks, outputPath, "Directory '${entry.name}'")
                            createDirectoriesIfMissing(outputPath)
                        }
                        entry.isSymbolicLink -> {
                            requireNoEscapingAncestor(escapingSymlinks, outputPath, "Symlink '${entry.name}'")
                            createDirectoriesIfMissing(outputPath.parent)
                            Files.createSymbolicLink(outputPath, Paths.get(entry.linkName))
                            val linkTarget = outputPath.parent.resolve(entry.linkName).normalize()
                            if (!linkTarget.startsWith(targetBase)) {
                                escapingSymlinks.add(outputPath)
                            }
                        }
                        entry.isLink -> {
                            // Hardlink target is archive-root-relative; validate it doesn't point outside.
                            // The escaping-symlink check is deferred to creation time, when the whole set is known.
                            val hardlinkTarget = targetBase.resolve(entry.linkName).normalize()
                            requireInsideBase(targetBase, hardlinkTarget, "Hardlink target '${entry.linkName}'")
                            hardLinks[outputPath] = hardlinkTarget
                        }
                        else -> {
                            requireNoEscapingAncestor(escapingSymlinks, outputPath, "File '${entry.name}'")
                            createDirectoriesIfMissing(outputPath.parent)
                            // Overwrite rather than write through a symlink already sitting at this path.
                            Files.deleteIfExists(outputPath)
                            Files.newOutputStream(outputPath).use { tarInputStream.copyTo(it) }
                            if (supportsPosixFilePermissions) {
                                Files.setPosixFilePermissions(outputPath, getPosixFilePermissions(entry.mode))
                            }
                        }
                    }
                }
        }
        hardLinks.forEach { (linkPath, targetPath) ->
            requireNoEscapingAncestor(escapingSymlinks, linkPath, "Hardlink '${linkPath.fileName}'")
            requireNoEscapingAncestor(escapingSymlinks, targetPath, "Hardlink target '${targetPath.fileName}'")
            createDirectoriesIfMissing(linkPath.parent)
            Files.createLink(linkPath, targetPath)
        }
    }
}

internal class TarExtractionSecurityException(message: String) : IOException(message)

private val supportsPosixFilePermissions: Boolean by lazy {
    FileSystems.getDefault().supportedFileAttributeViews().contains("posix")
}

private fun createDirectoriesIfMissing(dir: Path?) {
    if (dir != null && !Files.isDirectory(dir)) {
        Files.createDirectories(dir)
    }
}

private fun requireInsideBase(base: Path, candidate: Path, description: String) {
    if (!candidate.startsWith(base)) {
        throw TarExtractionSecurityException("$description escapes target directory")
    }
}

private fun requireNoEscapingAncestor(escapingSymlinks: Set<Path>, outputPath: Path, description: String) {
    if (escapingSymlinks.any { outputPath.startsWith(it) }) {
        throw TarExtractionSecurityException("$description writes through an escaping symlink")
    }
}

private fun getPosixFilePermissions(mode: Int): Set<PosixFilePermission> = buildSet {
    addPermission(mode, 0b100_000_000, PosixFilePermission.OWNER_READ)
    addPermission(mode, 0b010_000_000, PosixFilePermission.OWNER_WRITE)
    addPermission(mode, 0b001_000_000, PosixFilePermission.OWNER_EXECUTE)
    addPermission(mode, 0b000_100_000, PosixFilePermission.GROUP_READ)
    addPermission(mode, 0b000_010_000, PosixFilePermission.GROUP_WRITE)
    addPermission(mode, 0b000_001_000, PosixFilePermission.GROUP_EXECUTE)
    addPermission(mode, 0b000_000_100, PosixFilePermission.OTHERS_READ)
    addPermission(mode, 0b000_000_010, PosixFilePermission.OTHERS_WRITE)
    addPermission(mode, 0b000_000_001, PosixFilePermission.OTHERS_EXECUTE)
}

private fun MutableSet<PosixFilePermission>.addPermission(mode: Int, permissionBitMask: Int, permission: PosixFilePermission) {
    if ((mode and permissionBitMask) != 0) {
        add(permission)
    }
}
