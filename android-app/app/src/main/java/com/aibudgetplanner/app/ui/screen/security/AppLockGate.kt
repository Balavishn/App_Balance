package com.aibudgetplanner.app.ui.screen.security

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun AppLockGate(
    content: @Composable () -> Unit
) {
    val viewModel: AppLockViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.unlocked || !uiState.lockEnabled) {
        content()
        return
    }

    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val focusRequester = remember { FocusRequester() }

    val bgBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFF1F1A44), // Deep Purple/Indigo
            Color(0xFF090E1A)  // Navy Black
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgBrush)
            .safeDrawingPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 24.dp)
            ) {
                Text(
                    text = "Unlock App",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp
                    ),
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = if (uiState.hasPin) {
                        "Use biometric authentication or PIN"
                    } else {
                        "Set a PIN to secure this app"
                    },
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )
            }

            // Fingerprint Central Graphic
            CustomFingerprintGraphic()

            // Input and Actions Section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                // Biometrics Button (if supported and PIN exists)
                if (canUseBiometric(context) && activity != null && uiState.hasPin) {
                    Button(
                        onClick = {
                            showBiometricPrompt(
                                activity = activity,
                                onSuccess = viewModel::onBiometricSuccess
                            )
                        },
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.05f)),
                        border = BorderStroke(
                            width = 1.5.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFF3B82F6), Color(0xFF10B981))
                            )
                        ),
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CustomMiniFingerprintIcon()
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Unlock with Biometrics",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }

                // PIN Input Dots Pill
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .clickable { focusRequester.requestFocus() },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(4) { index ->
                            val isFilled = index < uiState.pinInput.length
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(if (isFilled) Color.White else Color.White.copy(alpha = 0.25f))
                            )
                        }
                        if (uiState.pinInput.length < 4) {
                            Text(
                                text = "|",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Light
                            )
                        }
                    }

                    // Invisible input field to trigger software keyboard
                    BasicTextField(
                        value = uiState.pinInput,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() } && input.length <= 4) {
                                viewModel.onPinChange(input)
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        modifier = Modifier
                            .fillMaxSize()
                            .focusRequester(focusRequester)
                            .alpha(0.01f)
                    )

                    LaunchedEffect(Unit) {
                        focusRequester.requestFocus()
                    }
                }

                // Unlock PIN Button
                Button(
                    onClick = viewModel::submitPin,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF232363))
                ) {
                    Text(
                        text = if (uiState.hasPin) "Unlock with PIN" else "Save PIN and Unlock",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                // Error Message if any
                uiState.errorMessage?.let { error ->
                    Text(
                        text = error,
                        color = Color(0xFFEF4444),
                        modifier = Modifier.padding(top = 4.dp),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Cancel Link
                TextButton(
                    onClick = { viewModel.onPinChange("") }
                ) {
                    Text(
                        text = "Cancel",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun CustomFingerprintGraphic(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.size(170.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Outer Concentric Ring
            drawCircle(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF3B82F6), Color(0xFF10B981))
                ),
                radius = w * 0.45f,
                style = Stroke(width = 1.5.dp.toPx())
            )

            // High-tech Arc Segments
            drawArc(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF10B981), Color(0xFF3B82F6))
                ),
                startAngle = -60f,
                sweepAngle = 120f,
                useCenter = false,
                style = Stroke(width = 3.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
            )
            drawArc(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF3B82F6), Color(0xFF10B981))
                ),
                startAngle = 120f,
                sweepAngle = 120f,
                useCenter = false,
                style = Stroke(width = 3.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
            )

            // Inner Translucent Circular Backdrop
            drawCircle(
                color = Color.White.copy(alpha = 0.04f),
                radius = w * 0.38f
            )
        }

        // Fingerprint Ridge Custom Canvas Drawing
        Canvas(modifier = Modifier.size(68.dp)) {
            val w = size.width
            val h = size.height
            val ridgesColor = Color(0xFF10B981)
            val strokeWidth = 2.8.dp.toPx()

            // Center Loop
            drawRoundRect(
                color = ridgesColor,
                topLeft = androidx.compose.ui.geometry.Offset(w * 0.4f, h * 0.35f),
                size = androidx.compose.ui.geometry.Size(w * 0.2f, h * 0.38f),
                cornerRadius = CornerRadius(w * 0.1f),
                style = Stroke(width = strokeWidth)
            )

            // Loop 1
            drawPath(
                path = Path().apply {
                    moveTo(w * 0.25f, h * 0.8f)
                    quadraticTo(w * 0.25f, h * 0.22f, w * 0.5f, h * 0.22f)
                    quadraticTo(w * 0.75f, h * 0.22f, w * 0.75f, h * 0.8f)
                },
                color = ridgesColor,
                style = Stroke(width = strokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            )

            // Loop 2
            drawPath(
                path = Path().apply {
                    moveTo(w * 0.12f, h * 0.88f)
                    quadraticTo(w * 0.1f, h * 0.08f, w * 0.5f, h * 0.08f)
                    quadraticTo(w * 0.9f, h * 0.08f, w * 0.88f, h * 0.88f)
                },
                color = ridgesColor.copy(alpha = 0.6f),
                style = Stroke(width = strokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            )

            // Loop 3 (Inner-most partial)
            drawPath(
                path = Path().apply {
                    moveTo(w * 0.5f, h * 0.48f)
                    quadraticTo(w * 0.5f, h * 0.32f, w * 0.5f, h * 0.32f)
                },
                color = ridgesColor,
                style = Stroke(width = strokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            )
        }
    }
}

@Composable
fun CustomMiniFingerprintIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(18.dp)) {
        val w = size.width
        val h = size.height
        val color = Color.White
        val strokeWidth = 1.5.dp.toPx()

        drawRoundRect(
            color = color,
            topLeft = androidx.compose.ui.geometry.Offset(w * 0.38f, h * 0.38f),
            size = androidx.compose.ui.geometry.Size(w * 0.24f, h * 0.36f),
            cornerRadius = CornerRadius(w * 0.12f),
            style = Stroke(width = strokeWidth)
        )
        drawPath(
            path = Path().apply {
                moveTo(w * 0.18f, h * 0.82f)
                quadraticTo(w * 0.18f, h * 0.18f, w * 0.5f, h * 0.18f)
                quadraticTo(w * 0.82f, h * 0.18f, w * 0.82f, h * 0.82f)
            },
            color = color,
            style = Stroke(width = strokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Round)
        )
    }
}

private fun canUseBiometric(context: android.content.Context): Boolean {
    val biometricManager = BiometricManager.from(context)
    val result = biometricManager.canAuthenticate(
        BiometricManager.Authenticators.BIOMETRIC_WEAK
    )
    return result == BiometricManager.BIOMETRIC_SUCCESS
}

private fun showBiometricPrompt(
    activity: FragmentActivity,
    onSuccess: () -> Unit
) {
    val executor = ContextCompat.getMainExecutor(activity)
    val callback = object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            onSuccess()
        }
    }

    val prompt = BiometricPrompt(activity, executor, callback)
    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Unlock AI Budget Planner")
        .setSubtitle("Authenticate to continue")
        .setNegativeButtonText("Cancel")
        .build()

    prompt.authenticate(promptInfo)
}
