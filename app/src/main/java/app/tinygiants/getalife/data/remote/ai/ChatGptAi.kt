package app.tinygiants.getalife.data.remote.ai

import app.tinygiants.getalife.domain.repository.AiRepository
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import javax.inject.Inject

class ChatGptAi @Inject constructor(private val openAi: OpenAI) : AiRepository {

    override suspend fun generateEmojiBy(tag: String): Result<String?> = try {
        val request = ChatCompletionRequest(
            model = ModelId("gpt-3.5-turbo"),
            messages = listOf(
                ChatMessage(
                    role = ChatRole.User,
                    content = "Suggest a single emoji for: $tag. Reply with only the emoji, nothing else."
                )
            )
        )

        val completion = openAi.chatCompletion(request)
        val emoji = completion.choices.first().message.content?.trim()
        Result.success(emoji)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun generateFinancialInsights(
        spendingPattern: String,
        userContext: String
    ): Result<String> = try {
        val request = ChatCompletionRequest(
            model = ModelId("gpt-3.5-turbo"),
            messages = listOf(
                ChatMessage(
                    role = ChatRole.User,
                    content = "Analyze this spending pattern and provide financial insights: $spendingPattern. User context: $userContext"
                )
            )
        )

        val completion = openAi.chatCompletion(request)
        val insights = completion.choices.first().message.content ?: ""
        Result.success(insights)
    } catch (e: Exception) {
        Result.failure(e)
    }
}