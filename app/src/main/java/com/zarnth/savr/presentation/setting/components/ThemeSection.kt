package com.zarnth.savr.presentation.setting.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zarnth.savr.R
import com.zarnth.savr.presentation.setting.SettingEvents
import com.zarnth.savr.presentation.setting.SettingState
import com.zarnth.savr.presentation.setting.SettingViewModel
import com.zarnth.savr.ui.theme.ThemeMode

@Composable
fun ThemeSection(state: SettingState, viewModel: SettingViewModel) {
    SectionHeader("Theme")
    SettingItem(
        icon = R.drawable.dark_m_icon,
        title = "Theme",
        subtitle = when (state.themeMode) {
            ThemeMode.LIGHT -> "Light"
            ThemeMode.DARK -> "Dark"
            ThemeMode.SYSTEM -> "System"
        },
        onClick = { viewModel.onEvent(SettingEvents.ShowThemeSheet) }
    )
    if (state.isDynamicColorSupported) {
        Spacer(Modifier.height(4.dp))
        SettingItem(
            icon = R.drawable.dynamic_one,
            title = "Dynamic color",
            subtitle = if (state.dynamicColor) "On" else "Off",
            trailing = {
                Switch(
                    checked = state.dynamicColor,
                    onCheckedChange = { viewModel.onEvent(SettingEvents.ToggleDynamicColor(it)) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            },
            onClick = { viewModel.onEvent(SettingEvents.ToggleDynamicColor(!state.dynamicColor)) }
        )
    }
}
