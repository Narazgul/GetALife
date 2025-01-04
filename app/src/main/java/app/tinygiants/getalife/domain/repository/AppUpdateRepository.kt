package app.tinygiants.getalife.domain.repository

interface AppUpdateRepository {

    suspend fun checkForUpdateAvailability(): Boolean
    fun startUpdateFlow()
}