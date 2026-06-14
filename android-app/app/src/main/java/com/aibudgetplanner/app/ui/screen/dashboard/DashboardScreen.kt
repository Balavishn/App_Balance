package com.aibudgetplanner.app.ui.screen.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DashboardScreen(
    uiState: DashboardUiState,
    onImportStatement: () -> Unit,
    contentPadding: PaddingValues
) {
    val snapshot = uiState.snapshot
    val prediction = uiState.prediction

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0F1D)) // Premium dark navy background matching mockup
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Dashboard",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp
            ),
            color = Color.White
        )

        if (snapshot == null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111827))
            ) {
                Text(
                    text = "Complete setup to view your budget metrics.",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
            return@Column
        }

        // Remaining Budget Premium Gradient Card with Custom Geometric Pattern
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF2B206A), // Premium Dark Indigo
                                Color(0xFF6B21A8)  // Premium Purple
                            )
                        )
                    )
            ) {
                // Background Geometric Overlay Pattern (right-aligned)
                GeometricPattern(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .fillMaxWidth(0.5f)
                        .height(160.dp)
                )

                // Foreground Content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CustomWalletIcon()
                        Text(
                            text = "Remaining Budget",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = formatCurrency(snapshot.remainingBudget, snapshot.currency),
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 34.sp
                        ),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "${formatCurrency(snapshot.dailyBudget, snapshot.currency)} / day",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }
                    Text(
                        text = "Daily Limit",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }
        }

        // Salary vs Total Spent side-by-side
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Salary Card (Dark Green Theme)
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0C1D17)),
                border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Monthly Salary",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = Color.White.copy(alpha = 0.6f)
                        )
                        CustomCashIcon()
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = formatCurrency(snapshot.salary, snapshot.currency),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color(0xFF10B981)
                        )
                    )
                }
            }

            // Spent Card (Dark Red Theme)
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF221115)),
                border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total Spent",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = Color.White.copy(alpha = 0.6f)
                        )
                        CustomCardIcon()
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = formatCurrency(snapshot.totalSpentThisMonth, snapshot.currency),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color(0xFFEF4444)
                        )
                    )
                }
            }
        }

        // Savings Progress card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0C1D17)),
            border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Savings Progress",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    CustomTrendUpIcon()
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Goal: ${formatCurrency(snapshot.savingsGoal, snapshot.currency)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                    val savedAmount = (snapshot.salary - snapshot.totalFixedExpenses - snapshot.totalSpentThisMonth).coerceAtLeast(0.0)
                    Text(
                        text = "${formatCurrency(savedAmount, snapshot.currency)} Saved",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = Color(0xFF10B981)
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
                
                // Custom percentage indicator bar matching mockup
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White.copy(alpha = 0.05f)),
                    contentAlignment = Alignment.CenterStart
                ) {
                    val progressFraction = (snapshot.savingsProgress / 100.0).toFloat().coerceIn(0f, 1f)
                    if (progressFraction > 0f) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progressFraction)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF10B981))
                        )
                    }
                    
                    // Align the progress percentage text inside a badge at the end of the filled bar
                    if (progressFraction > 0f) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progressFraction)
                                .fillMaxHeight(),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(end = 4.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFD1FAE5)) // Light green background badge matching mockup
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${snapshot.savingsProgress.toInt()}%",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = Color(0xFF065F46) // Dark green text matching mockup
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // AI Spending Predictions card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1C132E)),
            border = BorderStroke(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.2f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CustomSparklesIcon()
                    Text(
                        text = "AI Budget Insights",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }

                if (prediction != null) {
                    val riskColor = when (prediction.riskLevel.uppercase()) {
                        "HIGH" -> Color(0xFFEF4444)
                        "MEDIUM" -> Color(0xFFF59E0B)
                        else -> Color(0xFF10B981)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CustomCheckCircleIcon(color = riskColor)
                        Text(
                            text = "Spending Risk Level: ",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Text(
                            text = prediction.riskLevel,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = riskColor
                        )
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f))

                    Text(
                        text = "Predicted EOM Spend: ${formatCurrency(prediction.predictedSpending, snapshot.currency)}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = Color.White.copy(alpha = 0.9f)
                    )
                } else {
                    Text(
                        text = "Calculating insights...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Import Button with Gradient Background
        Button(
            onClick = onImportStatement,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF3B82F6),
                                Color(0xFF8B5CF6)
                            )
                        )
                    )
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CustomUploadIcon()
                    Text(
                        text = "Import Bank Statement",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun GeometricPattern(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        
        // Draw overlapping transparent polygons/triangles
        val color1 = Color.White.copy(alpha = 0.04f)
        val color2 = Color.White.copy(alpha = 0.07f)
        val color3 = Color.White.copy(alpha = 0.11f)
        
        // Triangle 1
        drawPath(
            path = Path().apply {
                moveTo(w * 0.4f, 0f)
                lineTo(w, h * 0.4f)
                lineTo(w, 0f)
                close()
            },
            color = color1
        )
        
        // Triangle 2
        drawPath(
            path = Path().apply {
                moveTo(w * 0.1f, h)
                lineTo(w * 0.8f, h * 0.2f)
                lineTo(w, h)
                close()
            },
            color = color2
        )
        
        // Diamond/Triangle 3
        drawPath(
            path = Path().apply {
                moveTo(w * 0.5f, h * 0.8f)
                lineTo(w, h * 0.3f)
                lineTo(w, h * 0.8f)
                close()
            },
            color = color3
        )

        // Diagonal accent path
        drawPath(
            path = Path().apply {
                moveTo(w * 0.2f, h * 0.5f)
                lineTo(w * 0.7f, 0f)
                lineTo(w * 0.95f, h)
                close()
            },
            color = color1
        )
    }
}

@Composable
fun CustomWalletIcon(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(38.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(20.dp)) {
            val w = size.width
            val h = size.height
            // Wallet body
            drawRoundRect(
                color = Color.White,
                topLeft = androidx.compose.ui.geometry.Offset(0f, h * 0.1f),
                size = androidx.compose.ui.geometry.Size(w, h * 0.8f),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
                style = Stroke(width = 1.5.dp.toPx())
            )
            // Wallet clasp flap
            drawRoundRect(
                color = Color.White,
                topLeft = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.3f),
                size = androidx.compose.ui.geometry.Size(w * 0.5f, h * 0.4f),
                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
            )
            // Clasp dot
            drawCircle(
                color = Color(0xFF2563EB),
                radius = 1.5.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(w * 0.75f, h * 0.5f)
            )
        }
        Text(
            text = "₹",
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 7.dp, top = 2.dp)
        )
    }
}

@Composable
fun CustomCashIcon(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(32.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF10B981).copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(18.dp)) {
            val w = size.width
            val h = size.height
            // Bill border
            drawRoundRect(
                color = Color(0xFF10B981),
                size = size,
                cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx()),
                style = Stroke(width = 1.5.dp.toPx())
            )
            // Bill center circle
            drawCircle(
                color = Color(0xFF10B981),
                radius = 2.5.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(w / 2, h / 2)
            )
            // Small dots at four corners
            drawCircle(color = Color(0xFF10B981), radius = 1.dp.toPx(), center = androidx.compose.ui.geometry.Offset(2.5.dp.toPx(), 2.5.dp.toPx()))
            drawCircle(color = Color(0xFF10B981), radius = 1.dp.toPx(), center = androidx.compose.ui.geometry.Offset(w - 2.5.dp.toPx(), 2.5.dp.toPx()))
            drawCircle(color = Color(0xFF10B981), radius = 1.dp.toPx(), center = androidx.compose.ui.geometry.Offset(2.5.dp.toPx(), h - 2.5.dp.toPx()))
            drawCircle(color = Color(0xFF10B981), radius = 1.dp.toPx(), center = androidx.compose.ui.geometry.Offset(w - 2.5.dp.toPx(), h - 2.5.dp.toPx()))
        }
    }
}

@Composable
fun CustomCardIcon(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(32.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFEF4444).copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(18.dp)) {
            val w = size.width
            val h = size.height
            // Card body outline
            drawRoundRect(
                color = Color(0xFFEF4444),
                size = size,
                cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx()),
                style = Stroke(width = 1.5.dp.toPx())
            )
            // Stripe line
            drawRect(
                color = Color(0xFFEF4444),
                topLeft = androidx.compose.ui.geometry.Offset(0f, h * 0.25f),
                size = androidx.compose.ui.geometry.Size(w, h * 0.18f)
            )
            // Chip icon
            drawRect(
                color = Color(0xFFEF4444),
                topLeft = androidx.compose.ui.geometry.Offset(w * 0.15f, h * 0.55f),
                size = androidx.compose.ui.geometry.Size(w * 0.2f, h * 0.2f)
            )
        }
    }
}

@Composable
fun CustomTrendUpIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(18.dp)) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(w * 0.1f, h * 0.9f)
            lineTo(w * 0.9f, h * 0.1f)
            // Arrow tip lines
            moveTo(w * 0.45f, h * 0.1f)
            lineTo(w * 0.9f, h * 0.1f)
            lineTo(w * 0.9f, h * 0.55f)
        }
        drawPath(
            path = path,
            color = Color(0xFF10B981),
            style = Stroke(
                width = 2.dp.toPx(),
                cap = androidx.compose.ui.graphics.StrokeCap.Round,
                join = androidx.compose.ui.graphics.StrokeJoin.Round
            )
        )
    }
}

@Composable
fun CustomSparklesIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(20.dp)) {
        val w = size.width
        val h = size.height
        
        // Large Sparkle
        val path1 = Path().apply {
            val cx = w * 0.35f
            val cy = h * 0.35f
            val rx = w * 0.3f
            val ry = h * 0.3f
            moveTo(cx, cy - ry)
            quadraticBezierTo(cx, cy, cx + rx, cy)
            quadraticBezierTo(cx, cy, cx, cy + ry)
            quadraticBezierTo(cx, cy, cx - rx, cy)
            quadraticBezierTo(cx, cy, cx, cy - ry)
            close()
        }
        drawPath(path1, color = Color(0xFFF59E0B))

        // Small Sparkle
        val path2 = Path().apply {
            val cx = w * 0.75f
            val cy = h * 0.7f
            val rx = w * 0.18f
            val ry = h * 0.18f
            moveTo(cx, cy - ry)
            quadraticBezierTo(cx, cy, cx + rx, cy)
            quadraticBezierTo(cx, cy, cx, cy + ry)
            quadraticBezierTo(cx, cy, cx - rx, cy)
            quadraticBezierTo(cx, cy, cx, cy - ry)
            close()
        }
        drawPath(path2, color = Color(0xFFF59E0B))
    }
}

@Composable
fun CustomCheckCircleIcon(modifier: Modifier = Modifier, color: Color) {
    Canvas(modifier = modifier.size(18.dp)) {
        val w = size.width
        val h = size.height
        // Border circle
        drawCircle(
            color = color,
            radius = w / 2,
            style = Stroke(width = 1.5.dp.toPx())
        )
        // Checkmark path
        val path = Path().apply {
            moveTo(w * 0.3f, h * 0.5f)
            lineTo(w * 0.45f, h * 0.65f)
            lineTo(w * 0.7f, h * 0.35f)
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(
                width = 1.8.dp.toPx(),
                cap = androidx.compose.ui.graphics.StrokeCap.Round,
                join = androidx.compose.ui.graphics.StrokeJoin.Round
            )
        )
    }
}

@Composable
fun CustomUploadIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(16.dp)) {
        val w = size.width
        val h = size.height
        // Tray path
        val trayPath = Path().apply {
            moveTo(w * 0.1f, h * 0.55f)
            lineTo(w * 0.1f, h * 0.85f)
            lineTo(w * 0.9f, h * 0.85f)
            lineTo(w * 0.9f, h * 0.55f)
        }
        drawPath(
            path = trayPath,
            color = Color.White,
            style = Stroke(
                width = 1.8.dp.toPx(),
                cap = androidx.compose.ui.graphics.StrokeCap.Round,
                join = androidx.compose.ui.graphics.StrokeJoin.Round
            )
        )
        // Arrow path
        val arrowPath = Path().apply {
            moveTo(w * 0.5f, h * 0.65f)
            lineTo(w * 0.5f, h * 0.15f)
            // Arrow head
            moveTo(w * 0.25f, h * 0.4f)
            lineTo(w * 0.5f, h * 0.15f)
            lineTo(w * 0.75f, h * 0.4f)
        }
        drawPath(
            path = arrowPath,
            color = Color.White,
            style = Stroke(
                width = 1.8.dp.toPx(),
                cap = androidx.compose.ui.graphics.StrokeCap.Round,
                join = androidx.compose.ui.graphics.StrokeJoin.Round
            )
        )
    }
}

private fun formatCurrency(amount: Double, currencyCode: String): String {
    val symbol = when (currencyCode.uppercase()) {
        "USD" -> "$"
        "EUR" -> "€"
        "GBP" -> "£"
        "INR" -> "₹"
        "JPY" -> "¥"
        "CAD" -> "CA$"
        "AUD" -> "A$"
        else -> "$currencyCode "
    }
    return String.format("%s%,.2f", symbol, amount)
}
