import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id ("androidx.navigation.safeargs.kotlin")
}

val properties = Properties()
properties.load(rootProject.file("local.properties").inputStream())

android {
    compileSdkVersion(33)

    defaultConfig {
        applicationId = "com.mbj.ssassamarket"
        minSdkVersion(21)
        targetSdkVersion(33)
        versionCode = 1
        versionName = "1.0"

        buildConfigField("String", "GOOGLE_CLIENT_ID", properties.getProperty("google_client_id"))

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
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
        viewBinding = true
        dataBinding = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.7.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.android.gms:play-services-base:18.2.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    //Jetpack Navigation Graph 관련
    implementation("androidx.navigation:navigation-fragment-ktx:2.5.3")
    implementation("androidx.navigation:navigation-ui-ktx:2.5.3")
    //Firebase Bom 관련
    implementation(platform("com.google.firebase:firebase-bom:32.0.0"))
    //Firebase analytics 관련
    implementation("com.google.firebase:firebase-analytics-ktx")
    //Firebase auth 관련
    implementation("com.google.firebase:firebase-auth-ktx:22.0.0")
    //Google Play Services의 base 관련
    implementation("com.google.android.gms:play-services-base:18.2.0")
    //Google Play Services의 auth 관련
    implementation("com.google.android.gms:play-services-auth:20.5.0")
    //Fragment ktx(ViewModel 초기화) 관련
    implementation ("androidx.fragment:fragment-ktx:1.5.7")
    //Moshi(Json Converter) 관련
    implementation ("com.squareup.moshi:moshi:1.14.0")

}
