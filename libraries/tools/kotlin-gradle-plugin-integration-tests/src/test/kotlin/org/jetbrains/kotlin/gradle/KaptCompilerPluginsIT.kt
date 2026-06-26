/*
 * Copyright 2010-2026 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle

import org.gradle.util.GradleVersion
import org.jetbrains.kotlin.gradle.testbase.*
import org.jetbrains.kotlin.test.TestMetadata
import org.jetbrains.kotlin.testFederation.AffectedByCompilerPlugins
import org.junit.jupiter.api.DisplayName

@DisplayName("Kapt with compiler plugins")
@OtherGradlePluginTests
@AffectedByCompilerPlugins
class KaptCompilerPluginsIT : KaptBaseIT() {
    override val defaultBuildOptions: BuildOptions =
        super.defaultBuildOptions.copyEnsuringK2()

    @DisplayName("K2 kapt stubs use kotlin.jvm.functions.Function0 instead of compiler plugin function kinds")
    @GradleTest
    @TestMetadata("kapt2/compilerPluginFunctionKind")
    fun testFunctionTypeKindCompilerPluginInKapt(gradleVersion: GradleVersion) {
        val projectName = "compilerPluginFunctionKind".withPrefix
        val buildOptions = defaultBuildOptions.copy(
            isolatedProjects = BuildOptions.IsolatedProjectsMode.DISABLED
        )

        project(
            projectName,
            gradleVersion,
            buildOptions = buildOptions,
        ) {
            build(":example:kaptGenerateStubsKotlin") {
                assertTasksExecuted(":example:kaptGenerateStubsKotlin")
                assertFileInProjectDoesNotContain(
                    "example/build/tmp/kapt3/stubs/main/repro/TestInterface.java",
                    "PluginFunction0",
                )
                assertFileInProjectContains(
                    "example/build/tmp/kapt3/stubs/main/repro/TestInterface.java",
                    "kotlin.jvm.functions.Function0",
                )
            }
        }
    }
}
