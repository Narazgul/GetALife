/**
* JetBrains Space Automation
* This Kotlin-script file lets you automate build activities
* For more info, see https://www.jetbrains.com/help/space/automation.html
*/

job("Build and run  tests") {
    gradlew("alvrme/alpine-android-base:latest", "build")
}