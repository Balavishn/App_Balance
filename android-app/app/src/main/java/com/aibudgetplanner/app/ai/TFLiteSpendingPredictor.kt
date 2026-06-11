package com.aibudgetplanner.app.ai

import android.content.Context
import com.aibudgetplanner.app.domain.model.BudgetSnapshot
import dagger.hilt.android.qualifiers.ApplicationContext
import org.tensorflow.lite.Interpreter
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TFLiteSpendingPredictor @Inject constructor(
    @ApplicationContext private val context: Context
) : SpendingPredictor {

    private val interpreter: Interpreter? by lazy {
        runCatching {
            Interpreter(loadModelFile("models/spending_predictor.tflite"))
        }.getOrNull()
    }

    override fun predictMonthEndSpend(snapshot: BudgetSnapshot): PredictionInference {
        val featureVector = floatArrayOf(
            snapshot.salary.toFloat(),
            snapshot.totalFixedExpenses.toFloat(),
            snapshot.totalSpentThisMonth.toFloat(),
            snapshot.savingsGoal.toFloat(),
            snapshot.remainingDays.toFloat(),
            snapshot.dailyBudget.toFloat(),
            snapshot.availableBudget.toFloat(),
            java.time.LocalDate.now().dayOfMonth.toFloat()
        )

        val model = interpreter
        if (model != null) {
            val input = arrayOf(featureVector)
            val output = Array(1) { FloatArray(1) }
            runCatching {
                model.run(input, output)
                return PredictionInference(
                    predictedSpending = output[0][0].toDouble().coerceAtLeast(0.0),
                    modelUsed = "TensorFlow Lite"
                )
            }
        }

        val daysElapsed = java.time.LocalDate.now().dayOfMonth.coerceAtLeast(1)
        val remainingFutureDays = (snapshot.remainingDays - 1).coerceAtLeast(0)
        val avgDaily = snapshot.totalSpentThisMonth / daysElapsed
        val heuristic = snapshot.totalSpentThisMonth + (avgDaily * remainingFutureDays)
        return PredictionInference(
            predictedSpending = heuristic,
            modelUsed = "Heuristic Fallback"
        )
    }

    private fun loadModelFile(assetPath: String): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(assetPath)
        return fileDescriptor.use { descriptor ->
            java.io.FileInputStream(descriptor.fileDescriptor).channel.use { channel ->
                channel.map(FileChannel.MapMode.READ_ONLY, descriptor.startOffset, descriptor.declaredLength)
            }
        }
    }
}
