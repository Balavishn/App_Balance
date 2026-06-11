package com.aibudgetplanner.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SmsTransactionReceiver : BroadcastReceiver() {

    @Inject
    lateinit var smsExpenseProcessor: com.aibudgetplanner.app.sms.SmsExpenseProcessor

    private val receiverScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (Telephony.Sms.Intents.SMS_RECEIVED_ACTION != intent.action) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        val body = messages.joinToString(separator = " ") { it.messageBody.orEmpty() }
        if (body.isBlank()) return

        receiverScope.launch {
            smsExpenseProcessor.handleSms(body)
        }
    }
}
