package com.aibudgetplanner.app.sms

import com.aibudgetplanner.app.domain.model.ExpenseCategory
import java.util.Locale

object SmsExpenseParser {
    private val debitPatterns = listOf(
        Regex("(?:debited|spent|payment of|paid|withdrawn|sent)[:\\s]*(?:inr|rs\\.?|₹)?\\s*(\\d+(?:,\\d{3})*(?:\\.\\d{1,2})?)", RegexOption.IGNORE_CASE),
        Regex("(?:inr|rs\\.?|₹)\\s*(\\d+(?:,\\d{3})*(?:\\.\\d{1,2})?)\\s*(?:debited|spent|paid|withdrawn|sent)", RegexOption.IGNORE_CASE),
        Regex("(?:txn|transaction).{0,35}?(?:inr|rs\\.?|₹)\\s*(\\d+(?:,\\d{3})*(?:\\.\\d{1,2})?)", RegexOption.IGNORE_CASE),
        Regex("a/c\\s*[*xX]{2,}\\d{2,}.{0,40}?(?:inr|rs\\.?|₹)\\s*(\\d+(?:,\\d{3})*(?:\\.\\d{1,2})?)", RegexOption.IGNORE_CASE),
        Regex("(?:purchase|pos|ecom|upi txn|imps txn|neft txn).{0,40}?(?:inr|rs\\.?|₹)\\s*(\\d+(?:,\\d{3})*(?:\\.\\d{1,2})?)", RegexOption.IGNORE_CASE),
        Regex("(?:amount)\\s*(?:inr|rs\\.?|₹)\\s*(\\d+(?:,\\d{3})*(?:\\.\\d{1,2})?).{0,30}?(?:debited|paid|dr)", RegexOption.IGNORE_CASE)
    )

    private val creditMarkers = listOf("credited", "received", "refund", "reversed", "cashback", "deposited")

    fun parseTransaction(message: String): SmsExpenseCandidate? {
        val normalized = message.trim()
        if (normalized.isBlank()) return null
        if (creditMarkers.any { it in normalized.lowercase(Locale.getDefault()) }) return null

        val amount = debitPatterns
            .mapNotNull { pattern -> pattern.find(normalized)?.groupValues?.getOrNull(1) }
            .firstOrNull()
            ?.replace(",", "")
            ?.toDoubleOrNull()
            ?: return null

        val entities = SmsNlpEntityExtractor.extract(normalized)

        val description = extractDescription(normalized)
        val category = inferCategory(normalized)
        val paymentMethod = if (entities.channel == "SMS") inferPaymentMethod(normalized) else entities.channel

        return SmsExpenseCandidate(
            amount = amount,
            description = description,
            category = category,
            paymentMethod = paymentMethod,
            merchant = entities.merchant,
            accountLastDigits = entities.accountLastDigits,
            transactionReference = entities.transactionReference
        )
    }

    private fun extractDescription(message: String): String {
        val cleaned = message.replace(Regex("\\s+"), " ").trim()
        return cleaned.take(140)
    }

    private fun inferCategory(message: String): ExpenseCategory {
        val text = message.lowercase(Locale.getDefault())
        return when {
            listOf("restaurant", "food", "cafe", "coffee", "swiggy", "zomato", "grocery", "supermarket").any { it in text } -> ExpenseCategory.FOOD
            listOf("uber", "ola", "taxi", "metro", "bus", "train", "flight", "fuel", "petrol", "diesel").any { it in text } -> ExpenseCategory.TRAVEL
            listOf("amazon", "flipkart", "myntra", "shopping", "mall", "store", "retail").any { it in text } -> ExpenseCategory.SHOPPING
            listOf("electric", "bill", "gas", "water", "internet", "phone", "mobile").any { it in text } -> ExpenseCategory.BILLS
            listOf("netflix", "spotify", "prime", "movie", "cinema").any { it in text } -> ExpenseCategory.ENTERTAINMENT
            listOf("hospital", "clinic", "pharmacy", "medicine", "medical").any { it in text } -> ExpenseCategory.MEDICAL
            listOf("school", "college", "course", "education", "udemy", "coursera").any { it in text } -> ExpenseCategory.EDUCATION
            listOf("sip", "mutual fund", "stock", "investment", "broker", "nse", "bse").any { it in text } -> ExpenseCategory.INVESTMENTS
            else -> ExpenseCategory.OTHER
        }
    }

    private fun inferPaymentMethod(message: String): String {
        val text = message.lowercase(Locale.getDefault())
        return when {
            "upi" in text || "vpa" in text -> "UPI"
            "card" in text || "debit card" in text -> "Card"
            "cash" in text -> "Cash"
            "imps" in text -> "IMPS"
            "neft" in text -> "NEFT"
            "rtgs" in text -> "RTGS"
            "net banking" in text || "netbanking" in text -> "Net Banking"
            else -> "SMS"
        }
    }
}

data class SmsExpenseCandidate(
    val amount: Double,
    val description: String,
    val category: ExpenseCategory,
    val paymentMethod: String,
    val merchant: String,
    val accountLastDigits: String?,
    val transactionReference: String?
)
