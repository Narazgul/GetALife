package app.tinygiants.getalife.domain.repository

interface AiRepository {

    suspend fun generateEmojiBy(tag: String): Result<String?>

}