package com.tohoku.cafeteria.ui.settings

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tohoku.cafeteria.R
import com.tohoku.cafeteria.data.repository.PersonalInfo
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Enum class for dark mode options
enum class DarkModeOption(@StringRes val label: Int) {
    FOLLOW_SYSTEM(R.string.dark_mode_follow_system),
    LIGHT(R.string.dark_mode_always_off),
    DARK(R.string.dark_mode_always_on)
}

enum class BmrCalculationOption(@StringRes val label: Int) {
    DEFAULT(R.string.bmr_option_default),
    CALCULATE(R.string.bmr_option_calculate),
    CUSTOM(R.string.bmr_option_custom)
}

enum class ExerciseLevel(@StringRes val label: Int, val multiplier: Double) {
    SEDENTARY(R.string.exercise_level_sedentary, 1.2),
    LIGHT(R.string.exercise_level_lightly_active, 1.375),
    MODERATE(R.string.exercise_level_moderately_active, 1.55),
    ACTIVE(R.string.exercise_level_very_active, 1.725),
    EXTRA_ACTIVE(R.string.exercise_level_extra_active, 1.9)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory)
) {
    val settingsState by viewModel.settingsState.collectAsState()
    var expandedDropdown by remember { mutableStateOf(false) }
    var expandedBmrDropdown by remember { mutableStateOf(false) }
    var showBmrInputDialog by remember { mutableStateOf(false) }
    var showPersonalInfoDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = stringResource(R.string.tab_settings)) },
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(dimensionResource(R.dimen.padding_small))
                .fillMaxSize()
        ) {
            ListItem(
                modifier = Modifier.fillMaxWidth(),
                headlineContent = {
                    Text(
                        text = stringResource(R.string.settings_customization),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            )

            // BMR Selection
            Box {
                ListItem(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expandedBmrDropdown = true },
                    headlineContent = {
                        Text(text = stringResource(R.string.settings_bmr))
                    },
                    supportingContent = {
                        Text(
                            text = when (settingsState.bmrOption) {
                                BmrCalculationOption.DEFAULT -> stringResource(R.string.bmr_option_default)
                                BmrCalculationOption.CALCULATE -> stringResource(R.string.bmr_option_calculate)
                                BmrCalculationOption.CUSTOM -> stringResource(R.string.bmr_option_custom)
                            }
                        )
                    },
                    trailingContent = {
                        Text(text = stringResource(
                            R.string.settings_kcal_display,
                            settingsState.customBmrValue
                        ))
                    }
                )
                DropdownMenu(
                    expanded = expandedBmrDropdown,
                    onDismissRequest = { expandedBmrDropdown = false },
                    modifier = Modifier.width(IntrinsicSize.Min),
                ) {
                    BmrCalculationOption.entries.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(stringResource(option.label)) },
                            onClick = {
                                when (option) {
                                    BmrCalculationOption.DEFAULT -> {
                                        viewModel.setBmrOption(option)
                                        viewModel.setCustomBmrValue(2000)
                                    }
                                    BmrCalculationOption.CALCULATE -> {
                                        viewModel.setBmrOption(option)
                                        showPersonalInfoDialog = true
                                    }
                                    BmrCalculationOption.CUSTOM -> {
                                        viewModel.setBmrOption(option)
                                        showBmrInputDialog = true
                                    }
                                }
                                expandedBmrDropdown = false
                            }
                        )
                    }
                }
            }

            // Show the extracted dialogs when needed
            if (showBmrInputDialog) {
                CustomBmrInputDialog(
                    initialValue = settingsState.customBmrValue,
                    onSave = { viewModel.setCustomBmrValue(it) },
                    onDismiss = { showBmrInputDialog = false }
                )
            }

            if (showPersonalInfoDialog) {
                PersonalInfoBmrDialog(
                    initialPersonalInfo = settingsState.personalInfo,
                    onSave = { personalInfo, bmrValue ->
                        viewModel.setPersonalInfo(personalInfo)
                        viewModel.setCustomBmrValue(bmrValue) },
                    onDismiss = { showPersonalInfoDialog = false }
                )
            }

            ListItem(
                modifier = Modifier.fillMaxWidth(),
                headlineContent = {
                    Text(
                        text = stringResource(R.string.settings_appearance),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            )
            Box {
                ListItem(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expandedDropdown = true },
                    headlineContent = {
                        Text(text = stringResource(R.string.settings_dark_mode))
                    },
                    supportingContent = {
                        Text(
                            text = stringResource(settingsState.darkModeOption.label),
                        )
                    },
                )
                DropdownMenu(
                    expanded = expandedDropdown,
                    onDismissRequest = { expandedDropdown = false },
                    modifier = Modifier.width(IntrinsicSize.Min),
                ) {
                    DarkModeOption.entries.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(stringResource(option.label)) },
                            onClick = {
                                viewModel.setDarkModeOption(option)
                                expandedDropdown = false
                            }
                        )
                    }
                }
            }
            ListItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        viewModel.setDynamicColorEnabled(!settingsState.dynamicColorEnabled)
                    },
                headlineContent = {
                    Text(text = stringResource(R.string.settings_dynamic_color))
                },
                trailingContent = {
                    Switch(
                        checked = settingsState.dynamicColorEnabled,
                        onCheckedChange = { viewModel.setDynamicColorEnabled(it) }
                    )
                }
            )
        }
    }
}

@Composable
fun CustomBmrInputDialog(
    initialValue: Int,
    onSave: (Int) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var bmrInput by remember { mutableStateOf(TextFieldValue(initialValue.toString())) }
    val bmrFocus = remember { FocusRequester() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.enter_custom_bmr)) },
        text = {
            OutlinedTextField(
                value = bmrInput,
                onValueChange = { bmrInput = it },
                label = { Text(stringResource(R.string.enter_custom_bmr_hint)) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        bmrInput.text.toIntOrNull()?.let { value ->
                            if (value > 0) {
                                onSave(value)
                            }
                        }
                        onDismiss()
                    }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(bmrFocus)
                    .onFocusChanged {
                        if (it.isFocused) {
                            // Use LaunchedEffect to delay the selection until after composition
                            coroutineScope.launch {
                                delay(10) // Small delay to ensure the TextField is ready
                                bmrInput =
                                    bmrInput.copy(selection = TextRange(0, bmrInput.text.length))
                            }
                        }
                    },
                maxLines = 1
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    bmrInput.text.toIntOrNull()?.let { value ->
                        if (value > 0) {
                            onSave(value)
                        }
                    }
                    onDismiss()
                }
            ) {
                Text(stringResource(R.string.save_button))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(stringResource(R.string.cancel_button))
            }
        },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalInfoBmrDialog(
    initialPersonalInfo: PersonalInfo,
    onSave: (PersonalInfo, Int) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var age by remember { mutableStateOf(TextFieldValue(initialPersonalInfo.age.toString())) }
    var weight by remember { mutableStateOf(TextFieldValue(initialPersonalInfo.weight.toString())) }
    var height by remember { mutableStateOf(TextFieldValue(initialPersonalInfo.height.toString())) }
    var selectedExerciseLevel by remember { mutableStateOf(initialPersonalInfo.exerciseLevel) }

    var isMale by remember { mutableStateOf(initialPersonalInfo.isMale) }
    val genderOptions = listOf(stringResource(R.string.male), stringResource(R.string.female))
    var genderSelectedIndex by remember { mutableIntStateOf(if (isMale) 0 else 1) }

    val coroutineScope = rememberCoroutineScope()
    val ageFocus = remember { FocusRequester() }
    val weightFocus = remember { FocusRequester() }
    val heightFocus = remember { FocusRequester() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.calculate_bmr)) },
        text = {
            Column {
                // Gender selection
                Text(
                    text = stringResource(R.string.gender),
                    style = MaterialTheme.typography.bodyMedium,
                )
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = dimensionResource(R.dimen.padding_xsmall))
                ) {
                    genderOptions.forEachIndexed { index, label ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = genderOptions.size
                            ),
                            colors = SegmentedButtonDefaults.colors(
                                activeContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                inactiveContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                            ),
                            onClick = {
                                genderSelectedIndex = index
                                isMale = genderSelectedIndex == 0 },
                            selected = index == genderSelectedIndex,
                            label = { Text(text = label) }
                        )
                    }
                }
                // Age input
                OutlinedTextField(
                    value = age,
                    onValueChange = { age = it },
                    label = { Text(stringResource(R.string.calculate_bmr_age_hint)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = dimensionResource(R.dimen.padding_xsmall))
                        .focusRequester(ageFocus)
                        .onFocusChanged {
                            if (it.isFocused) {
                                // Use LaunchedEffect to delay the selection until after composition
                                coroutineScope.launch {
                                    delay(10) // Small delay to ensure the TextField is ready
                                    age = age.copy(selection = TextRange(0, age.text.length))
                                }
                            }
                        },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { weightFocus.requestFocus() }
                    ),
                    maxLines = 1
                )

                // Weight input
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text(stringResource(R.string.calculate_bmr_weight_hint)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = dimensionResource(R.dimen.padding_xsmall))
                        .focusRequester(weightFocus)
                        .onFocusChanged {
                            if (it.isFocused) {
                                // Use LaunchedEffect to delay the selection until after composition
                                coroutineScope.launch {
                                    delay(10) // Small delay to ensure the TextField is ready
                                    weight =
                                        weight.copy(selection = TextRange(0, weight.text.length))
                                }
                            }
                        },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { heightFocus.requestFocus() }
                    ),
                    maxLines = 1
                )

                // Height input
                OutlinedTextField(
                    value = height,
                    onValueChange = { height = it },
                    label = { Text(stringResource(R.string.calculate_bmr_height_hint)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = dimensionResource(R.dimen.padding_xsmall))
                        .focusRequester(heightFocus)
                        .onFocusChanged {
                            if (it.isFocused) {
                                // Use LaunchedEffect to delay the selection until after composition
                                coroutineScope.launch {
                                    delay(10) // Small delay to ensure the TextField is ready
                                    height =
                                        height.copy(selection = TextRange(0, height.text.length))
                                }
                            }
                        },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    maxLines = 1
                )

                Box(modifier = Modifier.fillMaxWidth()) {
                    var expanded by remember { mutableStateOf(false) }

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it }
                    ) {
                        OutlinedTextField(
                            value = stringResource(selectedExerciseLevel.label),
                            onValueChange = { },
                            readOnly = true,
                            label = { Text(stringResource(R.string.calculate_bmr_exercise_level_hint)) },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                            },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = dimensionResource(R.dimen.padding_xsmall))
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        )

                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                        ) {
                            ExerciseLevel.entries.forEach { level ->
                                DropdownMenuItem(
                                    text = { Text(stringResource(level.label)) },
                                    onClick = {
                                        selectedExerciseLevel = level
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    // Calculate BMR using Mifflin-St Jeor Equation
                    val ageVal = age.text.toIntOrNull() ?: 30
                    val weightVal = weight.text.toIntOrNull() ?: 70
                    val heightVal = height.text.toIntOrNull() ?: 170

                    // Calculate base BMR
                    val baseBmr = if (isMale) {
                        (10 * weightVal) + (6.25 * heightVal) - (5 * ageVal) + 5
                    } else {
                        (10 * weightVal) + (6.25 * heightVal) - (5 * ageVal) - 161
                    }

                    // Apply activity multiplier to get TDEE (Total Daily Energy Expenditure)
                    val tdee = (baseBmr * selectedExerciseLevel.multiplier).toInt()

                    onSave(
                        PersonalInfo(
                            isMale = isMale,
                            age = ageVal,
                            weight = weightVal,
                            height = heightVal,
                            exerciseLevel = selectedExerciseLevel
                        ),
                        tdee
                    )
                    onDismiss()
                }
            ) {
                Text(stringResource(R.string.calculate_button))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(stringResource(R.string.cancel_button))
            }
        },
        modifier = modifier
    )
}

// Preview functions
@Preview(showBackground = true)
@Composable
fun CustomBmrInputDialogPreview() {
    MaterialTheme {
        CustomBmrInputDialog(
            initialValue = 2000,
            onSave = {},
            onDismiss = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PersonalInfoBmrDialogPreview() {
    MaterialTheme {
        PersonalInfoBmrDialog(
            initialPersonalInfo = PersonalInfo(),
            onSave = {_, _ -> },
            onDismiss = {}
        )
    }
}
