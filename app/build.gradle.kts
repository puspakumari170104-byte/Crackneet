plugins { id("com.android.application") }

android {
    namespace = "com.crackneet.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.crackneet.app"
        minSdk = 23
        targetSdk = 36
        versionCode = 2
        versionName = "1.0.1"
    }

    signingConfigs {
        create("release") {
            val password = System.getenv("KEYSTORE_PASSWORD")
            val alias = System.getenv("KEY_ALIAS")
            if (password != null && alias != null) {
                storeFile = rootProject.file("crackneet-upload.jks")
                storePassword = password
                keyPassword = password
                this.keyAlias = alias
            }
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
        }
    }
}
