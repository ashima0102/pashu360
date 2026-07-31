package com.pashu360.app.feature.finance.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pashu360.app.core.data.SessionStore
import com.pashu360.app.core.domain.model.CategoryBreakdown
import com.pashu360.app.core.domain.model.FinanceCategory
import com.pashu360.app.core.domain.model.FinancialRecord
import com.pashu360.app.core.domain.model.MonthlyPnL
import com.pashu360.app.core.domain.model.TransactionType
import com.pashu360.app.feature.finance.domain.repository.FinanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

data class FinanceUiState(
    val transactions: List<FinancialRecord> = emptyList(),
    val monthlyPnL: List<MonthlyPnL> = emptyList(),
    val categoryBreakdown: List<CategoryBreakdown> = emptyList(),
    val today: LocalDate,
    val periodStart: LocalDate,
    val isLoading: Boolean = true
) {
    val currentMonthPnL: MonthlyPnL?
        get() = monthlyPnL.lastOrNull()
    val totalIncome: Double get() = transactions.filter { it.isIncome }.sumOf { it.amount }
    val totalExpense: Double get() = transactions.filter { !it.isIncome }.sumOf { it.amount }
    val net: Double get() = totalIncome - totalExpense
    val expenseBreakdown: List<CategoryBreakdown>
        get() = categoryBreakdown.filter { it.category.type == TransactionType.EXPENSE }
}

data class AddTransactionForm(
    val show: Boolean = false,
    val type: TransactionType = TransactionType.INCOME,
    val category: FinanceCategory = FinanceCategory.MILK_SALE,
    val amount: String = "",
    val counterparty: String = "",
    val notes: String = "",
    val isSaving: Boolean = false
) {
    val isValid: Boolean get() = (amount.toDoubleOrNull() ?: 0.0) > 0.0
}

sealed class FinanceEvent {
    data class Saved(val message: String) : FinanceEvent()
    data class ShowError(val message: String) : FinanceEvent()
}

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
@HiltViewModel
class FinanceViewModel @Inject constructor(
    private val repository: FinanceRepository,
    private val sessionStore: SessionStore
) : ViewModel() {

    private val farmId get() = sessionStore.getActiveFarmId()

    private val today: LocalDate =
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

    // Last 6 months window for chart
    private val periodStart: LocalDate = today.minus(DatePeriod(months = 5))
        .let { LocalDate(it.year, it.month, 1) }

    private val _form = MutableStateFlow(AddTransactionForm())
    val form: StateFlow<AddTransactionForm> = _form.asStateFlow()

    private val _events = Channel<FinanceEvent>()
    val events = _events.receiveAsFlow()

    val uiState: StateFlow<FinanceUiState> = combine(
        repository.observeInRange(farmId, periodStart, today),
        repository.observeMonthlyPnL(farmId, periodStart, today),
        repository.observeCategoryBreakdown(farmId, periodStart, today)
    ) { transactions, monthly, breakdown ->
        FinanceUiState(
            transactions = transactions,
            monthlyPnL = monthly,
            categoryBreakdown = breakdown,
            today = today,
            periodStart = periodStart,
            isLoading = false
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        FinanceUiState(today = today, periodStart = periodStart)
    )

    // ── Form handlers ────────────────────────
    fun openAddForm(type: TransactionType = TransactionType.INCOME) {
        val defaultCategory = if (type == TransactionType.INCOME)
            FinanceCategory.MILK_SALE else FinanceCategory.FEED
        _form.value = AddTransactionForm(show = true, type = type, category = defaultCategory)
    }

    fun closeForm() {
        _form.value = AddTransactionForm()
    }

    fun onTypeChanged(type: TransactionType) {
        _form.update {
            val newCat = if (type == TransactionType.INCOME)
                FinanceCategory.MILK_SALE else FinanceCategory.FEED
            it.copy(type = type, category = newCat)
        }
    }

    fun onCategoryChanged(category: FinanceCategory) {
        _form.update { it.copy(category = category) }
    }

    fun onAmountChanged(value: String) {
        _form.update {
            it.copy(amount = value.filter { c -> c.isDigit() || c == '.' }.take(9))
        }
    }

    fun onCounterpartyChanged(value: String) {
        _form.update { it.copy(counterparty = value) }
    }

    fun onNotesChanged(value: String) {
        _form.update { it.copy(notes = value) }
    }

    fun onSave() {
        val f = _form.value
        val amount = f.amount.toDoubleOrNull() ?: return
        if (amount <= 0.0) return

        _form.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            val record = FinancialRecord(
                farmId = farmId,
                recordDate = today,
                type = f.type,
                category = f.category,
                amount = amount,
                counterparty = f.counterparty.takeIf { it.isNotBlank() },
                notes = f.notes.takeIf { it.isNotBlank() }
            )

            repository.addRecord(record)
                .onSuccess {
                    _events.send(FinanceEvent.Saved("Transaction saved"))
                    _form.value = AddTransactionForm()
                }
                .onFailure { e ->
                    _form.update { it.copy(isSaving = false) }
                    _events.send(FinanceEvent.ShowError(e.message ?: "Could not save"))
                }
        }
    }
}
