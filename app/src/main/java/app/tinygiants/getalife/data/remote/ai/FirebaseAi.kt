package app.tinygiants.getalife.data.remote.ai

import app.tinygiants.getalife.domain.repository.AiRepository
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.crashlytics.FirebaseCrashlytics
import javax.inject.Inject

class FirebaseAi @Inject constructor(
    private val generativeModel: GenerativeModel,
    private val crashlytics: FirebaseCrashlytics
) : AiRepository {

    override suspend fun generateEmojiBy(tag: String): Result<String?> {
        val prompt = "Return only one emoji (no text, no explanation) that best represents: $tag"

        return runCatching {
            val response = generativeModel.generateContent(prompt)
            val result = response.text?.trim()
            result
        }.recoverCatching { exception ->
            crashlytics.recordException(exception)
            null
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
            
            Falls keine bestehende Kategorie gut passt, schlage eine neue vor.
            
            Antwortformat (JSON):
            {
              "shouldCreateNew": true,
              "categoryName": "Vorgeschlagener Name",
              "emoji": "🛒",
              "groupName": "Passende Gruppe",
              "reasoning": "Warum diese Kategorie sinnvoll ist"
            }
        """.trimIndent()

        return runCatching {
            val response = generativeModel.generateContent(prompt)
            response.text ?: throw Exception("Empty AI response")
        }.recoverCatching { exception ->
            crashlytics.recordException(exception)
            throw exception
        }
    }

    override suspend fun calculateSemanticSimilarity(
        text1: String,
        text2: String
    ): Result<Float> {
        val prompt = """
            Compare the semantic similarity between these two texts:
            Text 1: "$text1"
            Text 2: "$text2"
            
            Return only a number between 0.0 and 1.0 representing similarity (1.0 = identical meaning, 0.0 = completely different).
            No explanation, just the number.
        """.trimIndent()

        return runCatching {
            val response = generativeModel.generateContent(prompt)
            val result = response.text?.trim()?.toFloatOrNull() ?: 0f
            result.coerceIn(0f, 1f)
        }.recoverCatching { exception ->
            crashlytics.recordException(exception)
            0f
        }
    }

    override suspend fun generateFinancialInsights(
        spendingPattern: String,
        userContext: String
    ): Result<String> {
        val prompt = """
            Du bist ein persönlicher Finanzberater. Analysiere das Ausgabenmuster dieses Nutzers 
            und gib 1-2 hilfreiche, nicht-bevormundende Tipps:
            
            Ausgabenmuster: $spendingPattern
            Kontext: $userContext
            
            Gib Tipps wie ein guter Freund - ermutigend, verständnisvoll, praktisch.
            Berücksichtige, dass Menschen unterschiedlich wirtschaften.
            
            Antwortformat (JSON):
            {
              "insights": [
                {
                  "title": "Kurzer Titel",
                  "description": "Freundliche Erklärung",
                  "actionSuggestion": "Konkreter, optionaler Vorschlag",
                  "tone": "encouraging"
                }
              ]
            }
        """.trimIndent()

        return runCatching {
            val response = generativeModel.generateContent(prompt)
            response.text ?: throw Exception("Empty AI response")
        }.recoverCatching { exception ->
            crashlytics.recordException(exception)
            throw exception
        }
    }
}