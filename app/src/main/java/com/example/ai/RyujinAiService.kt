package com.example.ai

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Content
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

private const val TAG = "RyujinAiService"
private const val MODEL_NAME = "gemini-3.8-flash"
private const val AI_TIMEOUT_MS = 60_000L

sealed interface AiGenerationState {
    data object Idle : AiGenerationState
    data object Loading : AiGenerationState
    data class Streaming(val text: String) : AiGenerationState
    data class Success(val text: String) : AiGenerationState
    data class Error(val message: String) : AiGenerationState
}

data class AiChatEntry(
    val id: String,
    val isUser: Boolean,
    val text: String
)

data class GeneratedChapterOutline(
    val arcNumber: Int,
    val title: String,
    val summary: String,
    val openingDialogue: String
)

class RyujinAiService {

    private val generalModel by lazy {
        Firebase.ai.generativeModel(
            modelName = MODEL_NAME,
            systemInstruction = content {
                text(
                    "Kamu adalah Asisten AI resmi Ryujin Studio untuk novel epik 'Ryujin' " +
                        "(900 Bab, 3 Arc: Ryujin No Tanjou [1-300], The Mask of Angels [301-600], " +
                        "dan The Last Generation [601-900], dengan 400 karakter lintas negara dan " +
                        "gaya naskah dialog penuh). Jawablah dengan bahasa Indonesia yang elegan, " +
                        "tajam, akurat, dan informatif sesuai konteks novel."
                )
            }
        )
    }

    private val chapterOutlineSchema = Schema.obj(
        properties = mapOf(
            "arcNumber" to Schema.integer(description = "Nomor arc antara 1 hingga 3"),
            "title" to Schema.string(description = "Judul bab yang dramatis dan ringkas"),
            "summary" to Schema.string(description = "Ringkasan alur cerita bab dalam 2-3 kalimat"),
            "openingDialogue" to Schema.string(
                description = "Kutipan naskah dialog penuh pembuka adegan bergaya drama dengan nama karakter"
            )
        )
    )

    private val structuredModel by lazy {
        Firebase.ai.generativeModel(
            modelName = MODEL_NAME,
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = chapterOutlineSchema
            }
        )
    }

    fun streamPrompt(prompt: String): Flow<String> = flow {
        val accumulated = StringBuilder()
        try {
            withTimeout(AI_TIMEOUT_MS) {
                generalModel.generateContentStream(prompt).collect { chunk ->
                    val chunkText = chunk.text.orEmpty()
                    if (chunkText.isNotEmpty()) {
                        accumulated.append(chunkText)
                        emit(accumulated.toString())
                    }
                }
            }
            if (accumulated.isBlank()) {
                throw IllegalStateException("Model mengembalikan respons kosong.")
            }
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Streaming AI timed out after ${AI_TIMEOUT_MS}ms", e)
            throw e
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Firebase AI streaming failed", e)
            throw e
        }
    }

    fun streamChatReply(
        historyEntries: List<AiChatEntry>,
        userMessage: String,
        activeNovelContext: String
    ): Flow<String> = flow {
        val accumulated = StringBuilder()
        try {
            val chatHistory = mutableListOf<Content>()
            if (activeNovelContext.isNotBlank()) {
                chatHistory.add(
                    content("user") {
                        text("Konteks novel aktif di aplikasi saat ini: $activeNovelContext")
                    }
                )
                chatHistory.add(
                    content("model") {
                        text("Baik, saya siap membantu pembaca dan kreator Ryujin Studio berdasarkan konteks tersebut.")
                    }
                )
            }
            historyEntries.takeLast(10).forEach { entry ->
                chatHistory.add(
                    content(if (entry.isUser) "user" else "model") {
                        text(entry.text)
                    }
                )
            }

            val chat = generalModel.startChat(history = chatHistory)
            withTimeout(AI_TIMEOUT_MS) {
                chat.sendMessageStream(userMessage).collect { chunk ->
                    val chunkText = chunk.text.orEmpty()
                    if (chunkText.isNotEmpty()) {
                        accumulated.append(chunkText)
                        emit(accumulated.toString())
                    }
                }
            }
            if (accumulated.isBlank()) {
                throw IllegalStateException("Model mengembalikan respons chat kosong.")
            }
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "AI chat timed out", e)
            throw e
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Firebase AI chat failed", e)
            throw e
        }
    }

    suspend fun generateStructuredChapterOutline(
        novelTitle: String,
        arcNumber: Int,
        topicHint: String
    ): GeneratedChapterOutline {
        try {
            val prompt = buildString {
                append("Buatkan rancangan bab baru untuk novel '$novelTitle' pada Arc $arcNumber. ")
                if (topicHint.isNotBlank()) {
                    append("Tema atau konflik utama: $topicHint. ")
                }
                append("Berikan judul bab, ringkasan 2-3 kalimat, dan dialog pembuka penuh drama antara 2-3 karakter dalam bahasa Indonesia.")
            }
            val response = withTimeout(AI_TIMEOUT_MS) {
                structuredModel.generateContent(prompt)
            }
            val rawJson = response.text
                ?: throw IllegalStateException("Model mengembalikan respons JSON kosong.")
            val json = JSONObject(rawJson)
            return GeneratedChapterOutline(
                arcNumber = json.optInt("arcNumber", arcNumber).coerceIn(1, 3),
                title = json.getString("title"),
                summary = json.getString("summary"),
                openingDialogue = json.getString("openingDialogue")
            )
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Structured chapter generation timed out", e)
            throw e
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Structured chapter generation failed", e)
            throw e
        }
    }
}
