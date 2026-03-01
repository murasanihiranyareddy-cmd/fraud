package com.fraud_detector.detection

import android.content.Context
import com.fraud_detector.ml.TFLiteModelManager
import com.fraud_detector.models.ThreatLevel
import com.fraud_detector.models.ThreatResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThreatDetectionEngine @Inject constructor(
    @ApplicationContext private val context: Context,
    private val modelManager: TFLiteModelManager,
    private val keywordAnalyzer: KeywordAnalyzer,
    private val urlAnalyzer: UrlAnalyzer
) {
    suspend fun analyzeSms(sender: String, body: String): ThreatResult =
        withContext(Dispatchers.Default) {
            val mlScore = modelManager.runSmsModel(body)
            val keywordScore = keywordAnalyzer.score(body)
            val urlScore = body.extractUrls().maxOfOrNull { urlAnalyzer.score(it) } ?: 0f
            val combined = (mlScore * 0.6f) + (keywordScore * 0.25f) + (urlScore * 0.15f)
            ThreatResult(
                sender = sender,
                body = body,
                score = combined,
                level = when {
                    combined >= 0.75f -> ThreatLevel.HIGH
                    combined >= 0.45f -> ThreatLevel.MEDIUM
                    else -> ThreatLevel.LOW
                },
                timestamp = System.currentTimeMillis()
            )
        }

    suspend fun analyzeCall(number: String, duration: Long): ThreatResult =
        withContext(Dispatchers.Default) {
            val score = modelManager.runCallModel(number, duration)
            ThreatResult(
                sender = number,
                body = "Incoming call — $duration s",
                score = score,
                level = if (score >= 0.7f) ThreatLevel.HIGH else ThreatLevel.LOW,
                timestamp = System.currentTimeMillis()
            )
        }

    private fun String.extractUrls(): List<String> =
        Regex("""https?://[^s]+""").findAll(this).map { it.value }.toList()
}