/*
 * Copyright 2010-2025 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.android

import org.gradle.util.GradleVersion
import org.jetbrains.kotlin.gradle.KaptBaseIT
import org.jetbrains.kotlin.gradle.testbase.*
import org.junit.jupiter.api.DisplayName

@DisplayName("kapt with AGP 9 com.android.legacy-kapt plugin")
@AndroidGradlePluginTests
class KaptAndroidLegacyKaptIT : KaptBaseIT() {

    @DisplayName("kapt generates sources via com.android.legacy-kapt on AGP 9 new DSL")
    @GradleAndroidTest
    @AndroidTestVersions(minVersion = TestVersions.AGP.AGP_90)
    fun testLegacyKaptGeneratesSources(
        gradleVersion: GradleVersion,
        agpVersion: String,
        jdkVersion: JdkVersions.ProvidedJdk,
    ) {
        project(
            "android-legacy-kapt".withPrefix,
            gradleVersion,
            buildOptions = defaultBuildOptions.copy(
                androidVersion = agpVersion,
                enableLegacyAgpDsl = false,
            ),
            buildJdk = jdkVersion.location,
        ) {
            build(":lib:kaptDebugKotlin") {
                assertFileInProjectExists("lib/build/generated/source/kapt/debug/com/example/legacykapt/DaggerAppComponent.java")
            }
        }
    }
}
