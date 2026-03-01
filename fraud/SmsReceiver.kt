package com.fraud_detector.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.fraud_detector.detection.ThreatDetectionEngine
import com.fraud_detector.models.ThreatLevel
import com.fraud_detector.utils.NotificationHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class SmsReceiver : BroadcastReceiver() {

    @Inject lateinit var detectionEngine: ThreatDetectionEngine
    @Inject lateinit var notificationHelper: NotificationHelper

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return
        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        val sender = messages.firstOrNull()?.originatingAddress ?: return
        val body = messages.joinToString("") { it.messageBody }

        val pending = goAsync()
        scope.launch {
            try {
                val result = detectionEngine.analyzeSms(sender, body)
                if (result.level != ThreatLevel.LOW) {
                    notificationHelper.sendThreatAlert(result)
                }
            } finally {
                pending.finish()
            }
        }
    }
}