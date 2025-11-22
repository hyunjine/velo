import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.vanniktech)
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)

    signAllPublications()

    val groupId = "io.github.hyunjine"
    val artifactId = project.name
    val version = rootProject.extra["VERSION_NAME"].toString()
    coordinates(groupId = groupId, artifactId = artifactId, version = version)

    pom {
        name.set(project.name)
        description.set("Description for ${project.name}")
        url.set("https://github.com/hyunjine/${project.name}")

        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }

        developers {
            developer {
                id.set("hyunjine")
                name.set("HyunJin Yang")
                email.set("thevlakk1@gmail.com")
            }
        }

        scm {
            connection.set("scm:git:git://github.com/hyunjine/velo.git")
            developerConnection.set("scm:git:ssh://github.com/hyunjine/velo.git")
            url.set("https://github.com/hyunjine/velo")
        }
    }
}

android {
    namespace = "com.hyunjine.velo_core"
    compileSdk = 36

    defaultConfig {
        minSdk = 26

        testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
}

dependencies {
    testImplementation(libs.junit)
    androidTestImplementation(libs.runner)
    androidTestImplementation(libs.espresso.core)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.material3)

    implementation(libs.ktor.client.core)
}