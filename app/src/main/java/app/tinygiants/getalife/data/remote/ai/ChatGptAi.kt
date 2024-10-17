package app.tinygiants.getalife.data.remote.ai

import app.tinygiants.getalife.domain.repository.AiRepository
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import javax.inject.Inject

class ChatGptAi @Inject constructor(private val openAi: OpenAI) : AiRepository {

    override suspend fun generateEmojiBy(tag: String): Result<String?> {

        val prompt = "Return one Emoji for this tag: $tag"
        val chatCompletionRequest = ChatCompletionRequest(
            model = ModelId("gpt-3.5-turbo-0125"),
            messages = listOf(
                ChatMessage(
                    role = ChatRole.User,
                    content = prompt
                )
            )
        )

        return runCatching {
            val completion = openAi.chatCompletion(chatCompletionRequest)
            completion.choices.first().message.content
        }
    }
}