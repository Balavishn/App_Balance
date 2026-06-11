package com.aibudgetplanner.app.sms

import java.util.Locale

object SmsNlpEntityExtractor {
    private val merchantPatterns = listOf(
        Regex("(?:at|to|towards|merchant)\\s+([A-Za-z0-9&\\-._ ]{2,40})", RegexOption.IGNORE_CASE),
        Regex("(?:on)\\s+([A-Za-z0-9&\\-._ ]{2,40})\\s+(?:txn|transaction|ref|avl)", RegexOption.IGNORE_CASE)
    )

    private val accountPattern = Regex("(?:a/c|acct|account)\\s*(?:no\\.?|number)?\\s*[:xX*]*\\s*(\\d{2,6})", RegexOption.IGNORE_CASE)
    private val referencePattern = Regex("(?:utr|ref(?:erence)?|txn(?: id)?)\\s*[:#-]?\\s*([A-Za-z0-9]{6,20})", RegexOption.IGNORE_CASE)

    fun extract(message: String): SmsEntities {
        val normalized = message.replace(Regex("\\s+"), " ").trim()

        val merchant = merchantPatterns
            .asSequence()
            .mapNotNull { it.find(normalized)?.groupValues?.getOrNull(1) }
            .map { it.trim(' ', '.', ',', ';', ':') }
            .firstOrNull { it.length >= 2 }
            ?: "Unknown Merchant"

        val accountLastDigits = accountPattern.find(normalized)?.groupValues?.getOrNull(1)
        val transactionReference = referencePattern.find(normalized)?.groupValues?.getOrNull(1)
        val channel = inferChannel(normalized)

        return SmsEntities(
            merchant = merchant.take(40),
            accountLastDigits = accountLastDigits,
            transactionReference = transactionReference,
            channel = channel
        )
    }

    private fun inferChannel(message: String): String {
        val text = message.lowercase(Locale.getDefault())
        return when {
            "upi" in text || "vpa" in text -> "UPI"
            "debit card" in text || "credit card" in text || "card" in text -> "Card"
            "imps" in text -> "IMPS"
            "neft" in text -> "NEFT"
            "rtgs" in text -> "RTGS"
            "atm" in text -> "ATM"
            "net banking" in text || "netbanking" in text -> "Net Banking"
            else -> "SMS"
        }
    }
}

data class SmsEntities(
    val merchant: String,
    val accountLastDigits: String?,
    val transactionReference: String?,
    val channel: String
)
