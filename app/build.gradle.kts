plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")


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
        mlModelBinding = true
    }


}




dependencies {
    
    implementation ("androidx.multidex:multidex:2.0.1")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.firebase:firebase-storage:21.0.0")
    implementation("org.tensorflow:tensorflow-lite-support:0.1.0")
    implementation("org.tensorflow:tensorflow-lite-metadata:0.1.0")
    implementation("com.google.firebase:firebase-auth:23.0.0")
    implementation("com.google.android.gms:play-services-auth:21.2.0")
    implementation("com.google.firebase:firebase-inappmessaging-display:21.0.0")
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
    implementation ("com.github.prolificinteractive:material-calendarview:2.0.0")
    implementation ("com.jakewharton.threetenabp:threetenabp:1.4.4")

    //video & voice call
    implementation ("com.github.ZEGOCLOUD:zego_uikit_prebuilt_live_streaming_android:+")
    implementation ("com.github.ZEGOCLOUD:zego_uikit_signaling_plugin_android:+")
    implementation("com.github.ZEGOCLOUD:zego_uikit_prebuilt_call_android:+")
    //firebase
    implementation("com.google.firebase:firebase-messaging:23.4.1")
    implementation("com.google.firebase:firebase-database:20.3.1")
    implementation ("com.google.firebase:firebase-analytics:21.6.1")
    implementation ("com.google.firebase:firebase-storage:20.3.0")
    implementation ("com.google.firebase:firebase-auth:23.0.0")
    implementation ("com.google.android.gms:play-services-auth:21.2.0")


    //graph
    implementation ("com.github.PhilJay:MPAndroidChart:v3.1.0")
    //chat
    implementation ("com.cometchat:chat-sdk-android:4.0.4")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation ("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    // messaging
    implementation ("com.sendbird.sdk:sendbird-android-sdk:3.0.141")
    //implementation ("com.sendbird.sdk:sendbird-chat-ktx:4.15.2")

    //computervision
    implementation ("org.tensorflow:tensorflow-lite-support:0.1.0")
    implementation ("org.tensorflow:tensorflow-lite-metadata:0.1.0")
    implementation ("com.quickbirdstudios:opencv-contrib:3.4.15")
    // Barcode model
    implementation ("com.google.mlkit:barcode-scanning:17.2.0")
    implementation ("com.google.mlkit:object-detection:17.0.1")
    implementation ("com.google.mlkit:object-detection-custom:17.0.1")
    implementation ("com.google.mlkit:face-detection:16.1.6")
    implementation ("com.google.android.gms:play-services-mlkit-text-recognition:19.0.0")
    implementation ("com.google.mlkit:image-labeling-automl:16.2.1")
    implementation ("com.google.mlkit:image-labeling:17.0.8")
    implementation ("com.google.mlkit:image-labeling-custom:17.0.2")
    //camera
    implementation ("androidx.camera:camera-camera2:1.0.0-beta04")
    implementation ("androidx.camera:camera-lifecycle:1.0.0-beta04")
    implementation ("androidx.camera:camera-view:1.0.0-alpha11")
    //progress bar
    implementation ("com.mikhaellopez:circularprogressbar:3.1.0")
    implementation ("com.github.hadibtf:SemiCircleArcProgressBar:1.1.1")






}