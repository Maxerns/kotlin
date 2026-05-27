/*
 * Copyright 2010-2026 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:OptIn(ExperimentalPathApi::class)

package org.jetbrains.kotlin

import jdk.jfr.consumer.RecordingStream
import org.jetbrains.kotlin.testFramework.inputchecking.TestInputsChecker
import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.TimeUnit
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.absolutePathString
import kotlin.io.path.createDirectories
import kotlin.io.path.createFile
import kotlin.io.path.deleteRecursively

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
open class TestInputsCheckerBenchmark {

    private val fileCount = 100_000
    private val benchmarkDir = Paths.get("build/benchmark").toFile().canonicalFile.toPath().apply { deleteRecursively() }
    private val outsideRootDir = benchmarkDir.resolve("outside").createDirectories()
    private val rootDir = benchmarkDir.resolve("root").createDirectories()
    private val buildDir = rootDir.resolve("some-project/build").createDirectories()
    private val srcKotlin = rootDir.resolve("some-project/src/main/kotlin").createDirectories()
    private val klibCacheDir = rootDir.resolve("kotlin-native/dist/klib/cache").createDirectories()
    private val klibStdlibCacheDir = klibCacheDir.resolve("macos_arm64-gSTATIC-system/stdlib-per-file-cache").createDirectories()

    private lateinit var declared: List<String>
    private lateinit var declaredNonCanonical: List<String>
    private lateinit var undeclared: List<String>
    private lateinit var undeclaredAlreadyDetected: List<String>
    private lateinit var undeclaredNonCanonical: List<String>
    private lateinit var nulls: List<String?>
    private lateinit var filesOutsideRootDir: List<String>
    private lateinit var filesInsideBuildDir: List<String>
    private lateinit var klibCacheFiles: List<String>
    private lateinit var klibStdlibCacheFiles: List<String>
    private lateinit var directories: List<String>

    private lateinit var pathsToCheck: List<String?>
    private lateinit var declaredInputs: Set<String>
    private lateinit var expectedUndeclaredInputs: Set<String>
    private lateinit var detectedUndeclaredInputs: Set<String>

    @Suppress("unused")
    @Setup(Level.Trial)
    fun generateFiles() {
        declared = generateDeclaredInputs(8)
        declaredNonCanonical = generateNonCanonicalDeclared(15)
        undeclared = generateUndeclaredInputs(11)
        undeclaredAlreadyDetected = generateAlreadyDetectedUndeclaredInputs(5)
        undeclaredNonCanonical = generateNonCanonicalUndeclared(15)
        nulls = generateNulls(1)
        filesOutsideRootDir = generateFilesOutsideRootDir(20)
        filesInsideBuildDir = generateFilesInsideBuildDir(10)
        klibCacheFiles = generateKlibCacheFiles(5)
        klibStdlibCacheFiles = generateKlibStdlibCacheFiles(5)
        directories = generateDirectories(5)

        pathsToCheck = buildList {
            addAll(declared)
            addAll(declaredNonCanonical)
            addAll(undeclared)
            addAll(undeclaredAlreadyDetected)
            addAll(undeclaredNonCanonical)
            addAll(nulls)
            addAll(filesOutsideRootDir)
            addAll(filesInsideBuildDir)
            addAll(klibCacheFiles)
            addAll(klibStdlibCacheFiles)
            addAll(directories)
        }.shuffled()

        declaredInputs = buildSet {
            addAll(declared)
            addAll(declaredNonCanonical.map { File(it).canonicalPath })
        }

        expectedUndeclaredInputs = buildSet {
            addAll(undeclared)
            addAll(undeclaredNonCanonical)
            addAll(klibStdlibCacheFiles)
        }

        require(pathsToCheck.size == fileCount)
    }

    @Suppress("unused")
    @Setup(Level.Invocation)
    fun initializeTestInputsChecker() {
        TestInputsChecker.initialize(
            rootDir.toString(),
            buildDir.toString(),
            klibCacheDir.toString(),
            klibStdlibCacheDir.toString(),
            declaredInputs,
            false
        )
        // preload internal list
        for (it in undeclaredAlreadyDetected) {
            TestInputsChecker.getInstance().checkPath(it)
        }
    }

    @Benchmark
    fun benchmark(blackhole: Blackhole) {
        detectedUndeclaredInputs = captureJfrEvents {
            for (path in pathsToCheck) {
                TestInputsChecker.getInstance().checkPath(path)
            }
        }
        blackhole.consume(detectedUndeclaredInputs)
    }

    @Suppress("unused")
    @TearDown(Level.Invocation)
    fun assertCorrectUndeclaredInputsWereDetected() {
        check(detectedUndeclaredInputs == expectedUndeclaredInputs) {
            val extraUndeclaredInputs = detectedUndeclaredInputs.filterNot { it in expectedUndeclaredInputs }
            val missingUndeclaredInputs = expectedUndeclaredInputs.filterNot { it in detectedUndeclaredInputs }

            buildString {
                appendLine("undeclaredInputs (${detectedUndeclaredInputs.size}) != expectedUndeclaredInputs (${expectedUndeclaredInputs.size})")
                if (extraUndeclaredInputs.isNotEmpty()) {
                    appendLine("Extra undeclared inputs (10/${extraUndeclaredInputs.size}):")
                    extraUndeclaredInputs.take(10).forEach(::appendLine)
                }
                if (missingUndeclaredInputs.isNotEmpty()) {
                    appendLine("Missing undeclared inputs (10/${missingUndeclaredInputs.size}):")
                    missingUndeclaredInputs.take(10).forEach(::appendLine)
                }
            }
        }
    }

    private fun captureJfrEvents(observedCodeBlock: () -> Unit): Set<String> =
        buildSet {
            RecordingStream().use { stream ->
                stream.enable("jetbrains.UndeclaredInput")
                stream.onEvent("jetbrains.UndeclaredInput") { add(it.getString("path")) }
                stream.startAsync()
                observedCodeBlock()
                stream.stop()
            }
        }

    private fun generateDeclaredInputs(n: Int) = buildList {
        repeat(n percentOf fileCount) {
            add(srcKotlin.createFile("Declared$it.kt"))
        }
    }

    private fun generateNonCanonicalDeclared(n: Int) = buildList {
        repeat(n percentOf fileCount) {
            add(srcKotlin.createFile("NonCanonicalDeclared$it.kt").replace("src/main", "src/../src/main"))
        }
    }

    private fun generateUndeclaredInputs(n: Int) = buildList {
        repeat(n percentOf fileCount) {
            add(srcKotlin.createFile("Undeclared$it.kt"))
        }
    }

    private fun generateAlreadyDetectedUndeclaredInputs(n: Int) = buildList {
        repeat(n percentOf fileCount) {
            add(srcKotlin.createFile("AlreadyDetectedUndeclared$it.kt"))
        }
    }

    private fun generateNonCanonicalUndeclared(n: Int) = buildList {
        repeat(n percentOf fileCount) {
            add(srcKotlin.createFile("NonCanonicalUndeclared$it.kt").replace("src/main", "src/../src/main"))
        }
    }

    private fun generateNulls(n: Int) = buildList<String?> {
        repeat(n percentOf fileCount) {
            add(null)
        }
    }

    private fun generateFilesOutsideRootDir(n: Int) = buildList {
        repeat(n percentOf fileCount) {
            add(outsideRootDir.createFile("outside$it.txt"))
        }
    }

    private fun generateFilesInsideBuildDir(n: Int) = buildList {
        repeat(n percentOf fileCount) {
            add(buildDir.createFile("Class$it.class"))
        }
    }

    private fun generateKlibCacheFiles(n: Int) = buildList {
        repeat(n percentOf fileCount) {
            add(klibCacheDir.createFile("klib_cache_$it"))
        }
    }

    private fun generateKlibStdlibCacheFiles(n: Int) = buildList {
        repeat(n percentOf fileCount) {
            add(klibStdlibCacheDir.createFile("klib_stdlib_cache_$it"))
        }
    }

    private fun generateDirectories(n: Int) = buildList {
        repeat(n percentOf fileCount) {
            add(srcKotlin.createDirectory("dir$it"))
        }
    }
}

private fun Path.createFile(file: String) =
    resolve(file).createFile().absolutePathString()

private fun Path.createDirectory(file: String) =
    resolve(file).createDirectories().absolutePathString()

private infix fun Int.percentOf(base: Int): Int =
    base * this / 100
