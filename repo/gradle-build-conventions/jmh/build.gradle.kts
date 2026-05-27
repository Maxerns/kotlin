plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(project(":utilities"))
    implementation(libs.jmh.gradle.plugin)
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:${project.bootstrapKotlinVersion}")
}
