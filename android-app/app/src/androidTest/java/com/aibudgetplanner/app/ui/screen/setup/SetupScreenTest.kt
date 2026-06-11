package com.aibudgetplanner.app.ui.screen.setup

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

class SetupScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun setupScreen_showsFields_andContinueButtonClickable() {
        var continueClicked = false

        composeRule.setContent {
            SetupScreen(
                uiState = SetupUiState(),
                onSalaryChange = {},
                onSavingsGoalChange = {},
                onSalaryDateChange = {},
                onCurrencyChange = {},
                onGoalsChange = {},
                onContinue = { continueClicked = true },
                contentPaddingTop = androidx.compose.ui.unit.Dp(0f)
            )
        }

        composeRule.onNodeWithText("Setup Your Budget").assertIsDisplayed()
        composeRule.onNodeWithText("Monthly Salary").assertIsDisplayed()
        composeRule.onNodeWithText("Continue").performClick()

        assert(continueClicked)
    }
}
