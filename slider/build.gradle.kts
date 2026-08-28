plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("maven-publish")
}

android {
    namespace = "io.github.seonghwishin.lottiegradientslider"
    compileSdk = 34

    defaultConfig {
        minSdk = 23
        consumerProguardFiles("consumer-rules.pro")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.airbnb.android:lottie:6.4.1")
    implementation("com.github.bumptech.glide:glide:4.16.0")
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])

                groupId = providers.gradleProperty("GROUP").get()
                artifactId = "lottie-gradient-slider"
                version = providers.gradleProperty("VERSION_NAME").get()

                pom {
                    name.set("LottieGradientSlider")
                    description.set("An Android custom slider view with gradient, image, and Lottie backgrounds.")
                    url.set("https://github.com/SeonghwiShin/LottieGradientSlider")
                    licenses {
                        license {
                            name.set("MIT License")
                            url.set("https://opensource.org/licenses/MIT")
                        }
                    }
                    developers {
                        developer {
                            id.set("SeonghwiShin")
                            name.set("SeonghwiShin")
                        }
                    }
                    scm {
                        connection.set("scm:git:github.com/SeonghwiShin/LottieGradientSlider.git")
                        developerConnection.set("scm:git:ssh://github.com/SeonghwiShin/LottieGradientSlider.git")
                        url.set("https://github.com/SeonghwiShin/LottieGradientSlider")
                    }
                }
            }
        }
    }
}
