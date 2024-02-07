## VPNCore Android Package
**This repo is a wrapper of the OpenVPN library so you can easily add it to your project via gradle**

### Installation
Create a personal access token from your github account, and add it to **root level** build.gradle file, you can read [here](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-gradle-registry#using-a-published-package) for more information
```groovy
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/vpncore/vpncore-android")
        credentials {
            username = project.findProperty("gpr.user") ?: System.getenv("USERNAME")
            password = project.findProperty("gpr.key") ?: System.getenv("TOKEN")
        }
   }
}
```

**build.gradle (app level)**
```groovy
implementation("org.vpncore:vpncore_android:1.1.0")
```

### Usage
**Please see the sample app**