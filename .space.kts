/**
* JetBrains Space Automation
* This Kotlin-script file lets you automate build activities
* For more info, see https://www.jetbrains.com/help/space/automation.html
*/

job("Qodana") {
    startOn {
        gitPush {
            branchFilter {
                +"refs/heads/feature"
            }
        }
        codeReviewOpened{}
    }
    container("jetbrains/qodana-jvm-android:2022.3") {
        env["QODANA_TOKEN"] = Secrets("qodana-token")
        shellScript {
            content = """
            QODANA_REMOTE_URL="ssh://git@git.${'$'}JB_SPACE_API_URL/${'$'}JB_SPACE_PROJECT_KEY/${'$'}JB_SPACE_GIT_REPOSITORY_NAME.git" \
            QODANA_BRANCH=${'$'}JB_SPACE_GIT_BRANCH \
            QODANA_REVISION=${'$'}JB_SPACE_GIT_REVISION \
            qodana
            """.trimIndent()
        }
    }
}

job("Build and run  tests") {
    requirements { workerType = WorkerTypes.SPACE_CLOUD_UBUNTU_LTS_LARGE }

    gradlew("alvrme/alpine-android-base:latest", "build")
}