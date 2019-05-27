# android-baidu-tts

[![Build Status](https://cloud.drone.io/api/badges/v7lin/android-baidu-tts/status.svg)](https://cloud.drone.io/v7lin/android-baidu-tts)
[![GitHub tag](https://img.shields.io/github/tag/v7lin/android-baidu-tts.svg)](https://github.com/v7lin/android-baidu-tts/releases)
[![API](https://img.shields.io/badge/API-14%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=14)

### docs

### snapshot

````
ext {
    latestVersion = '2.3.5-SNAPSHOT'
}

allprojects {
    repositories {
        ...
        maven {
            url 'https://oss.jfrog.org/artifactory/oss-snapshot-local'
        }
        ...
    }
}
````

### release

````
ext {
    latestVersion = '2.3.5'
}

allprojects {
    repositories {
        ...
        jcenter()
        ...
    }
}
````

### usage

* android

````
...
dependencies {
    ...
    implementation "io.github.v7lin:baidu-tts-android:${latestVersion}"
    ...
}
...
````

### example

[android example](./app/src/main/java/io/github/v7lin/baidutts/MainActivity.java)
