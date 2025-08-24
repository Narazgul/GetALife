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

    override suspend fun generateCategorySuggestion(
        transactionPartner: String,
        description: String,
        amount: String,
        existingCategories: List<String>,
        existingGroups: List<String>
    ): Result<String> {
        val prompt = """
            Analysiere diese Transaktion und schlage eine neue Kategorie vor:
            
            Transaktion:
            - Partner: $transactionPartner
            - Beschreibung: $description
            - Betrag: $amount
            
            Bestehende Kategorien: ${existingCategories.joinToString(", ")}
            Verfügbare Gruppen: ${existingGroups.joinToString(", ")}
            
            Antwortformat (JSON):
            {
              "shouldCreateNew": true,
              "categoryName": "Name",
              "emoji": "🛒",
              "groupName": "Gruppe",
              "reasoning": "Begründung"
            }
        """.trimIndent()

        val chatCompletionRequest = ChatCompletionRequest(
            model = ModelId("gpt-3.5-turbo-0125"),
            messages = listOf(
                ChatMessage(role = ChatRole.User, content = prompt)
            )
        )

        return runCatching {
            val completion = openAi.chatCompletion(chatCompletionRequest)
            completion.choices.first().message.content ?: throw Exception("Empty response")
        }
    }

    override suspend fun calculateSemanticSimilarity(
        text1: String,
        text2: String
    ): Result<Float> {
        val prompt = """
            Compare semantic similarity between:
            Text 1: "$text1"
            Text 2: "$text2"
            
            Return only a number between 0.0 and 1.0 (no explanation).
        """.trimIndent()

        val chatCompletionRequest = ChatCompletionRequest(
            model = ModelId("gpt-3.5-turbo-0125"),
            messages = listOf(
                ChatMessage(role = ChatRole.User, content = prompt)
            )
        )

        return runCatching {
            val completion = openAi.chatCompletion(chatCompletionRequest)
            val result = completion.choices.first().message.content?.trim()?.toFloatOrNull() ?: 0f
            result.coerceIn(0f, 1f)
        }
    }

    override suspend fun generateFinancialInsights(
        spendingPattern: String,
        userContext: String
    ): Result<String> {
        val prompt = """
            Als Finanzberater: Analysiere das Ausgabenmuster und gib freundliche Tipps:
            
            Muster: $spendingPattern
            Kontext: $userContext
            
            JSON Format:
            {
              "insights": [
                {
                  "title": "Titel",
                  "description": "Beschreibung",
                  "actionSuggestion": "Vorschlag",
                  "tone": "encouraging"
                }
              ]
            }
        """.trimIndent()

        val chatCompletionRequest = ChatCompletionRequest(
            model = ModelId("gpt-3.5-turbo-0125"),
            messages = listOf(
                ChatMessage(role = ChatRole.User, content = prompt)
            )
        )

        return runCatching {
            val completion = openAi.chatCompletion(chatCompletionRequest)
            completion.choices.first().message.content ?: throw Exception("Empty response")
        }
    }
}