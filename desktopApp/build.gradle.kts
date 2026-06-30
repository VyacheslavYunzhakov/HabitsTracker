import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
}

kotlin {
    jvm()

    sourceSets {
        jvmMain {
            dependencies {
                implementation(project(":shared"))
                implementation(project(":core:data"))
                implementation(project(":core:domain"))
                implementation(project(":feature:home"))
                implementation(compose.desktop.currentOs)
                implementation(libs.koin.core)
                implementation(libs.sqlite.bundled)
                implementation(libs.kotlinx.coroutines.swing)
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "HabitsTracker"
            packageVersion = "1.0.0"

            macOS {
                bundleID = "compose.project.habitstracker"
            }
        }
    }
}
