package com.aibudgetplanner.app.ui.screen.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aibudgetplanner.app.data.repository.BudgetRepository
import com.aibudgetplanner.app.domain.model.AiInsight
import com.aibudgetplanner.app.domain.usecase.GenerateAiInsightUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InsightsViewModel @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val generateAiInsightUseCase: GenerateAiInsightUseCase
) : ViewModel() {

    private val _insight = MutableStateFlow<AiInsight?>(null)
    val insight: StateFlow<AiInsight?> = _insight.asStateFlow()

    init {
        viewModelScope.launch {
            budgetRepository.observeBudgetSnapshot("local-user").collectLatest { snapshot ->
                _insight.value = snapshot?.let(generateAiInsightUseCase::invoke)
            }
        }
    }
}
