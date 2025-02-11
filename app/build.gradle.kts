plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
//    id("maven-publish")
}


android {
    namespace = "com.zy.socketutil"
    compileSdk = 34

    buildFeatures { viewBinding =true }

    defaultConfig {
        applicationId = "com.zy.socketutil"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

//val sourcesJar by tasks.registering(Jar::class) {
//    archiveClassifier.set("sources")
//    from(android.sourceSets.getByName("main").java.srcDirs)
//}

//publishing {
//    publications {
//        register<MavenPublication>("release") {
//            groupId = "com.github.crazyniche1"
//            artifactId = "udSocket" // 替换为你的 artifact ID
//            version = "3.0.6"
//
//            // 使用 Android 组件而不是 Java 组件
//            afterEvaluate {
//                from(components["release"])
//            }
//            artifact(sourcesJar.get())
//        }
//    }
//
//    repositories {
//        maven {
//            url = uri(layout.buildDirectory.dir("repo").get().asFile.toURI())
//            // 如果要发布到本地 Maven 仓库，可以使用以下配置：
//            // url = uri("../repo")
//        }
//    }
//}


dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
//    implementation(libs.udsocket)
//    implementation(files("libs/udsocket.aar"))
    implementation(project(":udSocket"))
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation ("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation( "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")

}