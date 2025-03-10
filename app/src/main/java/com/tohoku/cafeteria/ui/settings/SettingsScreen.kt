package com.tohoku.cafeteria.ui.settings

import android.app.Person
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tohoku.cafeteria.R
import com.tohoku.cafeteria.data.repository.PersonalInfo
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Enum class for dark mode options
enum class DarkModeOption(val label: String) {
    FOLLOW_SYSTEM("Follow system"),
    LIGHT("Always off"),
    DARK("Always on")
}

enum class BmrCalculationOption(val label: String) {
    DEFAULT("Default BMR"),
    CALCULATE("Calculate from personal info"),
    CUSTOM("Custom value")
}

enum class ExerciseLevel(val label: String, val multiplier: Double) {
    SEDENTARY("Sedentary (little or no exercise)", 1.2),
    LIGHT("Lightly active (light exercise 1-3 days/week)", 1.375),
    MODERATE("Moderately active (moderate exercise 3-5 days/week)", 1.55),
    ACTIVE("Very active (hard exercise 6-7 days/week)", 1.725),
    EXTRA_ACTIVE("Extra active (very hard exercise & physical job)", 1.9)
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
                .padding(8.dp)
                .fillMaxSize()
        ) {
            ListItem(
                modifier = Modifier.fillMaxWidth(),
                headlineContent = {
                    Text(
                        text = "Customization",
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
                        Text(text = "Basal Metabolic Rate (BMR)")
                    },
                    supportingContent = {
                        Text(
                            text = when (settingsState.bmrOption) {
                                BmrCalculationOption.DEFAULT -> "Default"
                                BmrCalculationOption.CALCULATE -> "Calculated from personal info"
                                BmrCalculationOption.CUSTOM -> "Custom value"
                            }
                        )
                    },
                    trailingContent = {
                        Text(text = "${settingsState.customBmrValue} kcal")
                    }
                )
                DropdownMenu(
                    expanded = expandedBmrDropdown,
                    onDismissRequest = { expandedBmrDropdown = false },
                    modifier = Modifier.width(IntrinsicSize.Min),
                ) {
                    BmrCalculationOption.entries.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.label) },
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
                        text = "Appearance",
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
                        Text(text = "Dark mode")
                    },
                    supportingContent = {
                        Text(
                            text = settingsState.darkModeOption.label,
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
                            text = { Text(option.label) },
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
                    Text(text = "Dynamic Color")
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
        title = { Text("Enter Custom BMR") },
        text = {
            OutlinedTextField(
                value = bmrInput,
                onValueChange = { bmrInput = it },
                label = { Text("Calories (kcal)") },
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
                                bmrInput = bmrInput.copy(selection = TextRange(0, bmrInput.text.length))
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
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Cancel")
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
    var isMale by remember { mutableStateOf(initialPersonalInfo.isMale) }
    var selectedExerciseLevel by remember { mutableStateOf(initialPersonalInfo.exerciseLevel) }

    val coroutineScope = rememberCoroutineScope()
    val ageFocus = remember { FocusRequester() }
    val weightFocus = remember { FocusRequester() }
    val heightFocus = remember { FocusRequester() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Calculate BMR") },
        text = {
            Column(modifier = Modifier.padding(8.dp)) {
                // Gender selection
                Text(
                    text = "Gender",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Row {
                    RadioButton(
                        selected = isMale,
                        onClick = { isMale = true }
                    )
                    Text(
                        text = "Male",
                        modifier = Modifier
                            .clickable { isMale = true }
                            .padding(start = 4.dp, end = 16.dp, top = 12.dp)
                    )
                    RadioButton(
                        selected = !isMale,
                        onClick = { isMale = false }
                    )
                    Text(
                        text = "Female",
                        modifier = Modifier
                            .clickable { isMale = false }
                            .padding(start = 4.dp, top = 12.dp)
                    )
                }

                // Age input
                OutlinedTextField(
                    value = age,
                    onValueChange = { age = it },
                    label = { Text("Age (years)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
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
                    label = { Text("Weight (kg)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .focusRequester(weightFocus)
                        .onFocusChanged {
                            if (it.isFocused) {
                                // Use LaunchedEffect to delay the selection until after composition
                                coroutineScope.launch {
                                    delay(10) // Small delay to ensure the TextField is ready
                                    weight = weight.copy(selection = TextRange(0, weight.text.length))
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
                    label = { Text("Height (cm)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .focusRequester(heightFocus)
                        .onFocusChanged {
                            if (it.isFocused) {
                                // Use LaunchedEffect to delay the selection until after composition
                                coroutineScope.launch {
                                    delay(10) // Small delay to ensure the TextField is ready
                                    height = height.copy(selection = TextRange(0, height.text.length))
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
                            value = selectedExerciseLevel.label,
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Exercise Level") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                            },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        )

                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                        ) {
                            ExerciseLevel.entries.forEach { level ->
                                DropdownMenuItem(
                                    text = { Text(level.label) },
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
                Text("Calculate")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Cancel")
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
