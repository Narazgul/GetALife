package app.tinygiants.getalife.data.remote.ai

import app.tinygiants.getalife.domain.repository.AiRepository
import com.google.ai.client.generativeai.GenerativeModel
import javax.inject.Inject

class GoogleGeminiAi @Inject constructor(private val generativeModel: GenerativeModel): AiRepository {

    override suspend fun generateEmojiBy(tag: String): Result<String?> {

        val prompt = "Return one Emoji for this tag: $tag"
        return runCatching {
            generativeModel.generateContent(prompt).text
        }
    }
}