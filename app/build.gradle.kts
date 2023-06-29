import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id ("androidx.navigation.safeargs.kotlin")
    id ("kotlin-kapt")
}

val properties = Properties()
properties.load(rootProject.file("local.properties").inputStream())

val KAKAO_MAP_NATIVE_KEY = properties.getProperty("kakao_map_native_key")

android {
    compileSdkVersion(33)

    defaultConfig {
        applicationId = "com.mbj.ssassamarket"
        minSdkVersion(21)
        targetSdkVersion(33)
        versionCode = 1
        versionName = "1.0"

        buildConfigField("String", "GOOGLE_CLIENT_ID", properties.getProperty("google_client_id"))
        buildConfigField("String", "FIREBASE_BASE_URL", properties.getProperty("firebase_base_url"))
        buildConfigField("String", "KAKAO_MAP_NATIVE_KEY", properties.getProperty("kakao_map_native_key"))

        manifestPlaceholders["KAKAO_MAP_NATIVE_KEY"] = KAKAO_MAP_NATIVE_KEY

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
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    //Jetpack Navigation Graph 관련
    implementation("androidx.navigation:navigation-fragment-ktx:2.5.3")
    implementation("androidx.navigation:navigation-ui-ktx:2.5.3")

    //Firebase Bom 관련
    implementation(platform("com.google.firebase:firebase-bom:32.0.0"))
    implementation("com.google.firebase:firebase-analytics-ktx") //analytics 관련
    implementation("com.google.firebase:firebase-auth-ktx:22.0.0") //auth 관련
    implementation("com.google.firebase:firebase-storage-ktx:20.2.1") // storage 관련

    //Google Play Services 관련
    implementation("com.google.android.gms:play-services-base:18.2.0") //base 관련
    implementation("com.google.android.gms:play-services-auth:20.5.0") //auth 관련
    implementation("com.google.android.gms:play-services-location:21.0.1") //location 관련

    //Fragment ktx(ViewModel 초기화) 관련
    implementation ("androidx.fragment:fragment-ktx:1.5.7")

    //Moshi(Json Converter) 관련
    implementation ("com.squareup.moshi:moshi:1.14.0")
    implementation ("com.squareup.moshi:moshi-kotlin:1.9.3")
    kapt ("com.squareup.moshi:moshi-kotlin-codegen:1.14.0")

    //OkHttp(네트워크 통신) 관련
    implementation ("com.squareup.okhttp3:okhttp:4.9.1")
    implementation ("com.squareup.okhttp3:logging-interceptor:4.9.1") //interceptor 관련

    //네트워크 통신을 위한 retrofit + moshi(Json Converter) 관련
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-moshi:2.9.0") //retrofit의 moshi 관련

    //Lottie(애니메이션) 관련
    implementation ("com.airbnb.android:lottie:5.2.0")

    //이미지 로딩 관련
    implementation("io.coil-kt:coil:2.4.0")

    //리사이클러뷰 관련(ConcatAdapter)
    implementation ("androidx.recyclerview:recyclerview:1.3.0")

    //Kakao Map API
    implementation(fileTree(mapOf("include" to listOf("*.jar"), "dir" to "libs")))
    implementation(files("libs/libDaumMapAndroid.jar"))
}
