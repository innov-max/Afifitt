plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
 

}

android {
    namespace = "com.example.afifit"
    compileSdk = 34


    defaultConfig {
        applicationId = "com.example.afifit"
        minSdk = 25
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        multiDexEnabled = true
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
        viewBinding = true
        aidl= true
        buildConfig = true
    }


}


dependencies {
    
    implementation ("androidx.multidex:multidex:2.0.1")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.firebase:firebase-storage:20.3.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation ("com.airbnb.android:lottie:6.3.0")
    // MPAndroidChart library
    implementation ("de.hdodenhof:circleimageview:3.1.0")
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.16.0")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")
    //apis
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.squareup.picasso:picasso:2.71828")
    //calender
    implementation ("com.applandeo:material-calendar-view:1.9.0")
    //video & voice call
    implementation ("com.github.ZEGOCLOUD:zego_uikit_prebuilt_live_streaming_android:+")
    implementation ("com.github.ZEGOCLOUD:zego_uikit_signaling_plugin_android:+")
    implementation("com.github.ZEGOCLOUD:zego_uikit_prebuilt_call_android:+")
    //firebase
    implementation("com.google.firebase:firebase-messaging:23.4.1")
    implementation("com.google.firebase:firebase-firestore:24.10.2")
    implementation("com.google.firebase:firebase-database:20.3.0")
    //graph
    implementation ("com.github.PhilJay:MPAndroidChart:v3.1.0")
    //chat
    implementation ("com.cometchat:chat-sdk-android:4.0.4")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation ("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    // messaging
    implementation ("com.sendbird.sdk:sendbird-android-sdk:3.0.141")
    //implementation ("com.sendbird.sdk:sendbird-chat-ktx:4.15.2")

    implementation ("com.quickbirdstudios:opencv-contrib:3.4.15")





}