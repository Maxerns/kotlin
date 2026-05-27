import JdkMajorVersion.JDK_25_0
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("common-configuration")
    id("test-federation-convention")
    id("com.autonomousapps.dependency-analysis")
    kotlin("jvm")
    id("jmh")
}

sourceSets {
    "bootClasspath" {
        java.srcDirs("bootClasspath")
    }

    main {
        projectDefault()
        compileClasspath += sourceSets["bootClasspath"].output
    }

    test {
        projectDefault()
        compileClasspath += sourceSets["bootClasspath"].output
        runtimeClasspath += sourceSets["bootClasspath"].output
    }

    "jmh" {
        java.srcDirs("jmh")
        compileClasspath += sourceSets["bootClasspath"].output
    }
}

val bootClasspathCompileOnly by configurations.getting

dependencies {
    compileOnly(libs.intellij.asm)
    bootClasspathCompileOnly(libs.org.jetbrains.annotations)

    implementation(kotlinStdlib())
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.bytebuddy)
}

val agentJar by task<ShadowJar> {
    from(sourceSets.main.map { it.output })
    configurations = project.configurations.runtimeClasspath.map { listOf(it) }
    manifest {
        attributes["PreMain-Class"] = "org.jetbrains.kotlin.testFramework.TestInstrumentationAgent"
        attributes["Can-Retransform-Classes"] = "true"
    }
}

val bootClasspathJar by task<Jar> {
    archiveClassifier = "boot-classpath"
    from(sourceSets["bootClasspath"].output)
}

configurations {
    runtimeElements {
        outgoing {
            artifacts.clear()
            artifact(agentJar)
        }
    }
    consumable("bootClasspath") {
        outgoing {
            artifact(bootClasspathJar)
        }
    }
}

jmh {
    javaLauncher = getToolchainLauncherFor(JDK_25_0)
    resultsFile = layout.projectDirectory.file("benchmark-baseline.txt")
    warmupIterations = 5
    iterations = 10
    fork = 3
    threads = 1
}

tasks.jmh {
    jmhClasspath.from(sourceSets["bootClasspath"].output)
}
