package com.tohoku.cafeteria.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tohoku.cafeteria.R
import com.tohoku.cafeteria.ui.theme.CafeteriaAITheme

// Enum class for dark mode options
enum class DarkModeOption(val label: String) {
    FOLLOW_SYSTEM("Follow system"),
    LIGHT("Always off"),
    DARK("Always on")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory)
) {
    val settingsState by viewModel.settingsState.collectAsState()
    var expandedDropdown by remember { mutableStateOf(false) }

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
                trailingContent = {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Dark mode"
                    )
                },
            )
            DropdownMenu(
                expanded = expandedDropdown,
                onDismissRequest = { expandedDropdown = false },
                modifier = Modifier.width(IntrinsicSize.Min)
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
            ListItem(
                modifier = Modifier
                    .fillMaxWidth(),
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

