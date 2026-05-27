import me.champeau.jmh.JMHTask
import me.champeau.jmh.JmhParameters

plugins {
    kotlin("jvm")
    id("me.champeau.jmh")
}

tasks {
    compileJmhKotlin {
        kotlinJavaToolchain.toolchain.use(project.jmh.javaLauncher)
    }
    compileJmhJava {
        javaCompiler = project.jmh.javaCompiler
    }
    jmhCompileGeneratedClasses {
        javaCompiler = project.jmh.javaCompiler
    }
    jmhRunBytecodeGenerator {
        javaLauncher = project.jmh.javaLauncher
    }
    named<JMHTask>("jmh") {
        javaLauncher = project.jmh.javaLauncher
    }
}

val JmhParameters.javaCompiler: Provider<JavaCompiler>
    get() = javaLauncher
        .map { it.metadata.languageVersion }
        .orElse(java.toolchain.languageVersion)
        .flatMap { javaToolchains.compilerFor { languageVersion = it } }
