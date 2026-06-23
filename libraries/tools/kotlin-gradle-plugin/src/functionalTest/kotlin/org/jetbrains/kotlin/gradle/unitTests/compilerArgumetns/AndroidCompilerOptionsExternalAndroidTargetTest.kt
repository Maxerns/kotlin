/*
 * Copyright 2010-2026 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.unitTests.compilerArgumetns

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.gradle.api.Project
import org.gradle.api.internal.project.ProjectInternal
import org.jetbrains.kotlin.compilerRunner.ArgumentUtils
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerArgumentsProducer.CreateCompilerArgumentsContext.Companion.lenient
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.util.buildProject
import org.jetbrains.kotlin.gradle.util.kotlin
import org.jetbrains.kotlin.gradle.util.setAndroidSdkDirProperty
import org.jetbrains.kotlin.gradle.utils.named
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AndroidCompilerOptionsExternalAndroidTargetTest {

    @Test
    fun androidLibraryCompilerOptionsPropagateToAndroidCompilation() {
        val project = externalAndroidLibraryProject(
            androidLibraryConfiguration = {
                compilerOptions {
                    optIn.add("kotlin.RequiresOptIn")
                    freeCompilerArgs.add("-Xexpect-actual-classes")
                    progressiveMode.set(true)
                    allWarningsAsErrors.set(true)
                    jvmTarget.set(JvmTarget.JVM_1_8)
                }
            },
        )
        project.evaluate()

        val arguments = project.compileArguments("compileAndroidMain")
        arguments.assertHasOptIn("kotlin.RequiresOptIn")
        arguments.assertHasFreeCompilerArg("-Xexpect-actual-classes")
        arguments.assertProgressiveMode()
        arguments.assertAllWarningsAsErrors()
        arguments.assertJvmTarget("1.8")
    }

    @Test
    fun kotlinMultiplatformCompilerOptionsPropagateToAndroidCompilation() {
        val project = externalAndroidLibraryProject {
            kotlin {
                compilerOptions {
                    progressiveMode.set(true)
                    allWarningsAsErrors.set(true)
                    optIn.add("kotlin.RequiresOptIn")
                    freeCompilerArgs.add("-Xexpect-actual-classes")
                }
            }
        }
        project.evaluate()

        val arguments = project.compileArguments("compileAndroidMain")
        arguments.assertHasOptIn("kotlin.RequiresOptIn")
        arguments.assertHasFreeCompilerArg("-Xexpect-actual-classes")
        arguments.assertProgressiveMode()
        arguments.assertAllWarningsAsErrors()
    }

    @Test
    fun compilationLevelCompilerOptionsPropagateToAndroidCompilation() {
        val project = externalAndroidLibraryProject(
            androidLibraryConfiguration = {
                compilations.getByName("main").compileTaskProvider.configure {
                    compilerOptions {
                        progressiveMode.set(true)
                        allWarningsAsErrors.set(true)
                    }
                }
            },
        )
        project.evaluate()

        val arguments = project.compileArguments("compileAndroidMain")
        arguments.assertProgressiveMode()
        arguments.assertAllWarningsAsErrors()
    }

    @Test
    fun compilationLevelCompilerOptionsPropagateToAndroidHostTestCompilation() {
        val project = externalAndroidLibraryProject(
            androidLibraryConfiguration = {
                withHostTest {}
                compilations.getByName("hostTest").compileTaskProvider.configure {
                    compilerOptions {
                        progressiveMode.set(true)
                        allWarningsAsErrors.set(true)
                    }
                }
            },
        )
        project.evaluate()

        val arguments = project.compileArguments("compileAndroidHostTest")
        arguments.assertProgressiveMode()
        arguments.assertAllWarningsAsErrors()
    }

    @Test
    fun androidLibraryCompilerOptionsOverrideKotlinMultiplatformAllWarningsAsErrors() {
        val project = externalAndroidLibraryProject(
            androidLibraryConfiguration = {
                compilerOptions {
                    allWarningsAsErrors.set(false)
                }
            },
        ) {
            kotlin {
                compilerOptions {
                    allWarningsAsErrors.set(true)
                }
            }
        }
        project.evaluate()

        project.compileArguments("compileAndroidMain").assertNoAllWarningsAsErrors()
    }

    private fun externalAndroidLibraryProject(
        androidLibraryConfiguration: KotlinMultiplatformAndroidLibraryTarget.() -> Unit = {},
        configureProject: Project.() -> Unit = {},
    ): ProjectInternal = buildProject {
        setAndroidSdkDirProperty(project)
        plugins.apply("kotlin-multiplatform")
        plugins.apply("com.android.kotlin.multiplatform.library")
        kotlin {
            iosArm64()
            targets.withType(KotlinMultiplatformAndroidLibraryTarget::class.java).configureEach { target ->
                target.compileSdk = 34
                target.namespace = "org.jetbrains.sample.options"
                target.withJava()
                target.androidLibraryConfiguration()
            }
        }
        configureProject()
    }

    private fun Project.compileArguments(taskName: String): List<String> {
        val compileTask = tasks.named<KotlinCompile>(taskName).get()
        return ArgumentUtils.convertArgumentsToStringList(compileTask.createCompilerArguments(lenient))
    }

    private fun List<String>.assertHasOptIn(value: String) {
        assertContainsCompilerArguments("-opt-in", value)
    }

    private fun List<String>.assertHasFreeCompilerArg(value: String) {
        assertTrue(value in this, "Expected free compiler arg '$value' in $this")
    }

    private fun List<String>.assertProgressiveMode() {
        assertTrue("-progressive" in this, "Expected progressive mode to be enabled: $this")
    }

    private fun List<String>.assertAllWarningsAsErrors() {
        assertTrue("-Werror" in this, "Expected allWarningsAsErrors to be enabled: $this")
    }

    private fun List<String>.assertNoAllWarningsAsErrors() {
        assertFalse("-Werror" in this, "Expected allWarningsAsErrors to be disabled: $this")
    }

    private fun List<String>.assertJvmTarget(expected: String) {
        assertContainsCompilerArguments("-jvm-target", expected)
    }

    private fun List<String>.assertContainsCompilerArguments(name: String, value: String) {
        val argumentIndex = indexOf(name)
        assertTrue(argumentIndex >= 0, "Expected '$name' argument in $this")
        assertEquals(getOrNull(argumentIndex + 1), value, "Expected '$name $value' in $this")
    }
}
