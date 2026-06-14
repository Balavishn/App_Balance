package com.aibudgetplanner.app.ui.screen.profile

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import androidx.compose.foundation.layout.PaddingValues

@Composable
fun ProfileScreen(
    contentPadding: PaddingValues,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    var readSmsGranted by remember { mutableStateOf(hasPermission(Manifest.permission.READ_SMS)) }
    var receiveSmsGranted by remember { mutableStateOf(hasPermission(Manifest.permission.RECEIVE_SMS)) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        readSmsGranted = result[Manifest.permission.READ_SMS] == true || hasPermission(Manifest.permission.READ_SMS)
        receiveSmsGranted = result[Manifest.permission.RECEIVE_SMS] == true || hasPermission(Manifest.permission.RECEIVE_SMS)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(text = "Profile", style = MaterialTheme.typography.headlineMedium)
        Text(text = "Backup, export, and settings modules are scaffolded for next implementation.")

        Card(modifier = Modifier.padding(top = 8.dp)) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "Cloud Sync", style = MaterialTheme.typography.titleMedium)
                Button(
                    onClick = viewModel::syncNow,
                    enabled = !uiState.isSyncing,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = if (uiState.isSyncing) "Syncing..." else "Sync Now")
                }

                if (uiState.isSyncing) {
                    CircularProgressIndicator()
                }

                uiState.lastSyncSummary?.let { summary ->
                    Text(text = summary)
                }

                uiState.errorMessage?.let { error ->
                    Text(text = error, color = MaterialTheme.colorScheme.error)
                }
            }
        }

        Card(modifier = Modifier.padding(top = 8.dp)) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "SMS Expense Reader", style = MaterialTheme.typography.titleMedium)
                Text(text = "The app listens for bank transaction SMS messages, parses debit alerts, and auto-creates expense records.")
                Text(text = "Required permissions: READ_SMS and RECEIVE_SMS")

                Text(
                    text = "Permission status: " +
                        if (readSmsGranted && receiveSmsGranted) "Granted" else "Not granted"
                )

                Button(
                    onClick = {
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.READ_SMS,
                                Manifest.permission.RECEIVE_SMS
                            )
                        )
                    },
                    enabled = !(readSmsGranted && receiveSmsGranted)
                ) {
                    Text(
                        text = if (readSmsGranted && receiveSmsGranted) {
                            "SMS Permissions Granted"
                        } else {
                            "Grant SMS Permissions"
                        }
                    )
                }
            }
        }
    }
}
