package app.tinygiants.getalife.data.gemini_ai

import app.tinygiants.getalife.domain.repository.AiRepository
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.GenerateContentResponse
import javax.inject.Inject

class GoogleGeminiAi @Inject constructor(private val generativeModel: GenerativeModel): AiRepository {

    override suspend fun generateEmojiBy(tag: String): Result<GenerateContentResponse> {

        val prompt = "$tag one Emoji Unicode. Answer in one row and without whitespaces. Unicode only!"
        return runCatching { generativeModel.generateContent(prompt) }
    }
}