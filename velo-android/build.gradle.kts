import com.vanniktech.maven.publish.SonatypeHost

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.vanniktech)
    id("signing")
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)

    signAllPublications()

    val groupId = "io.github.hyunjine"
    val artifactId = project.name
    val libraryVersion: String by project
    coordinates(groupId = groupId, artifactId = artifactId, version = libraryVersion)

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

signing {
    useInMemoryPgpKeys(
        findProperty("signingKey") as String?,
        findProperty("signingPassword") as String?
    )
}

android {
    namespace = "com.hyunjine.velo_android"
    compileSdk = 36

    defaultConfig {
        minSdk = 26

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)

    implementation(project(":velo-core"))
    implementation(libs.startup)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.logging)
    implementation(libs.disklrucache)
}