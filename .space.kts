/**
* JetBrains Space Automation
* This Kotlin-script file lets you automate build activities
* For more info, see https://www.jetbrains.com/help/space/automation.html
*/

job("Build and run  tests") {
    requirements { workerType = WorkerTypes.SPACE_CLOUD_UBUNTU_LTS_LARGE }

    gradlew("alvrme/alpine-android-base:latest", "build")
}