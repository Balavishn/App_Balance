package com.aibudgetplanner.app.ui.screen.security

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun AppLockGate(
    content: @Composable () -> Unit,
    viewModel: AppLockViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.unlocked || !uiState.lockEnabled) {
        content()
        return
    }

    val context = LocalContext.current
    val activity = context as? FragmentActivity

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "Unlock App", style = MaterialTheme.typography.headlineMedium)
        Text(
            text = if (uiState.hasPin) {
                "Use biometric authentication or PIN"
            } else {
                "Set a PIN to secure this app"
            }
        )

        if (canUseBiometric(context) && activity != null && uiState.hasPin) {
            Button(
                onClick = {
                    showBiometricPrompt(
                        activity = activity,
                        onSuccess = viewModel::onBiometricSuccess
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Unlock with Biometrics")
            }
        }

        OutlinedTextField(
            value = uiState.pinInput,
            onValueChange = viewModel::onPinChange,
            label = { Text(if (uiState.hasPin) "Enter PIN" else "Set PIN") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(onClick = viewModel::submitPin, modifier = Modifier.fillMaxWidth()) {
            Text(if (uiState.hasPin) "Unlock with PIN" else "Save PIN and Unlock")
        }

        uiState.errorMessage?.let { error ->
            Text(text = error, color = MaterialTheme.colorScheme.error)
        }
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
