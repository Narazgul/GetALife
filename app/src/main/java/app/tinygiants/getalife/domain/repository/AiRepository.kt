package app.tinygiants.getalife.domain.repository

import com.google.ai.client.generativeai.type.GenerateContentResponse

interface AiRepository {

    suspend fun generateEmojiBy(tag: String): Result<GenerateContentResponse>
}