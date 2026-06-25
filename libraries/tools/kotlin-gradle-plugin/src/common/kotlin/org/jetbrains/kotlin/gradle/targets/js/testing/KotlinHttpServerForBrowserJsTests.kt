/*
 * Copyright 2010-2026 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.targets.js.testing

import com.sun.net.httpserver.HttpServer
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.logging.Logging
import org.gradle.api.provider.Provider
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import org.gradle.kotlin.dsl.provideDelegate
import org.jetbrains.kotlin.gradle.utils.registerClassLoaderScopedBuildService
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.URI
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import kotlin.io.path.extension
import kotlin.io.path.fileSize
import kotlin.io.path.inputStream
import kotlin.io.path.isRegularFile
import kotlin.jvm.java

private val logger = Logging.getLogger(KotlinHttpServerForBrowserJsTests::class.java)

internal abstract class KotlinHttpServerForBrowserJsTests : BuildService<BuildServiceParameters.None>, AutoCloseable {
    private val jdkHttpServer: SimpleJdkHttpServer by lazy {
        SimpleJdkHttpServer().also {
            it.start()
            logger.debug("HTTP server for js tests started at ${it.serverAddress}")
        }
    }

    fun serve(fullTaskPath: String, bundleDirectory: Directory): URI {
        val url = jdkHttpServer.serveStaticFiles(fullTaskPath, bundleDirectory.asFile.toPath())
        logger.debug("Serving JS test bundle directory (${bundleDirectory.asFile.toPath()}) at $url")
        return url
    }

    override fun close() {
        jdkHttpServer.stopAndClear()
        logger.debug("HTTP server for js tests stopped")
    }
}

internal fun Project.kotlinHttpServerForBrowserJsTests(): Provider<KotlinHttpServerForBrowserJsTests> =
    gradle.registerClassLoaderScopedBuildService(KotlinHttpServerForBrowserJsTests::class) {}

internal class SimpleJdkHttpServer {
    class PrefixLocator<V : Any>() {
        private val locations: ConcurrentHashMap<String, V> = ConcurrentHashMap()

        /** returns previous value or null for a freshly added [value] */
        fun add(key: String, value: V): V? = locations.put(key, value)

        /**
         * Example:
         * ("ab", "abc", "cba").findClosestGreaterPrefix("abc11111") => "abc"
         * ("ab", "abc", "cba").findClosestGreaterPrefix("a") => null
         */
        fun findClosestGreaterPrefix(key: String): Pair<String, V>? {
            // this should work fast enough for few thousands of entries
            var res: Pair<String, V>? = null
            for ((prefix, value) in locations) {
                if (prefix !in key) continue
                if (res == null || prefix.length > res.first.length) {
                    res = prefix to value
                }
            }
            return res
        }

        fun clear(): Unit = locations.clear()
    }

    private val locator = PrefixLocator<Path>()
    private val server: HttpServer = HttpServer.create(
        InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0
    )

    private val port: Int get() = server.address.port
    val serverAddress: String get() = "http://localhost:$port/"

    init {
        server.createContext("/") { exchange ->
            val requestPath = exchange.requestURI.path.trimStart('/')

            val (prefix, root) = locator.findClosestGreaterPrefix(requestPath) ?: run {
                exchange.sendResponseHeaders(404, -1)
                exchange.close()
                return@createContext
            }

            val relativePath = requestPath.removePrefix(prefix).trimStart('/')
            val file: Path = root.resolve(relativePath).normalize()

            if (!file.startsWith(root) || !file.isRegularFile()) {
                exchange.sendResponseHeaders(404, -1)
                exchange.close()
                return@createContext
            }

            val contentType = file.mimeContentType()
            exchange.responseHeaders.add("Content-Type", contentType)

            val size = file.fileSize()
            exchange.sendResponseHeaders(200, size)

            file.inputStream().use { fileStream ->
                exchange.responseBody.use { responseBodyStream ->
                    fileStream.copyTo(responseBodyStream)
                }
            }
        }
    }

    fun serveStaticFiles(prefix: String, location: Path): URI {
        if (locator.add(prefix, location) != null) {
            throw IllegalArgumentException("Prefix '$prefix' is already registered")
        }
        return URI("$serverAddress/$prefix/")
    }

    fun start() {
        server.start()
    }

    fun stopAndClear() {
        locator.clear()
        server.stop(0)
    }
}

private fun Path.mimeContentType(): String {
    return when (extension) {
        "html" -> "text/html; charset=utf-8"
        "js" -> "application/javascript; charset=utf-8"
        "css" -> "text/css; charset=utf-8"
        "map" -> "application/json"
        "wasm" -> "application/wasm"
        else -> "application/octet-stream"
    }
}
