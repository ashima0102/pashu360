package com.pashu360.app.feature.animal.presentation

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Edit an existing animal. Reuses the AddAnimalScreen form UI via the
 * hoisted [AnimalFormHandlers] contract — only the header title, submit
 * label, and ViewModel differ.
 */
@Composable
fun EditAnimalScreen(
    viewModel: EditAnimalViewModel = hiltViewModel(),
    onSaved: () -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                AddAnimalEvent.Saved -> {
                    Toast.makeText(context, "Animal updated!", Toast.LENGTH_SHORT).show()
                    onSaved()
                }
                is AddAnimalEvent.ShowError -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // The AnimalFormScaffold in AddAnimalScreen is private, so we call
    // AddAnimalScreen indirectly by exposing a public shared entry point.
    AnimalFormPublic(
        title = "Edit Animal",
        submitLabel = "Save Changes",
        uiState = uiState,
        onBack = onBack,
        handlers = AnimalFormHandlers(
            onTagIdChanged = viewModel::onTagIdChanged,
            onNameChanged = viewModel::onNameChanged,
            onBreedChanged = viewModel::onBreedChanged,
            onGenderChanged = viewModel::onGenderChanged,
            onStatusChanged = viewModel::onStatusChanged,
            onDobChanged = viewModel::onDobChanged,
            onWeightChanged = viewModel::onWeightChanged,
            onPurchasePriceChanged = viewModel::onPurchasePriceChanged,
            onNotesChanged = viewModel::onNotesChanged,
            onSubmit = viewModel::onSubmit
        )
    )
}
