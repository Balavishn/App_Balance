package com.aibudgetplanner.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.aibudgetplanner.app.ui.navigation.AIBudgetNavHost
import com.aibudgetplanner.app.ui.theme.AIBudgetPlannerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AIBudgetPlannerTheme {
                AIBudgetNavHost()
            }
        }
    }
}
