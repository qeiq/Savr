package com.zarnth.savr.presentation.setting


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zarnth.savr.R
import com.zarnth.savr.openChromeTab
import com.zarnth.savr.presentation.setting.components.SettingItem
import com.zarnth.savr.ui.theme.ThemeMode
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    viewModel: SettingViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    val themeLabel = when (state.themeMode) {
        ThemeMode.LIGHT -> "Light"
        ThemeMode.DARK -> "Dark"
        ThemeMode.SYSTEM -> "System"
    }

    val versionName = runCatching {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName
    }.getOrDefault("")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        SectionHeader("General")
        SettingItem(
            icon = R.drawable.theme_moon,
            title = "Theme",
            subtitle = themeLabel,
            onClick = { viewModel.onEvent(SettingEvents.ShowThemeSheet) }
        )

        Spacer(Modifier.height(12.dp))

        SectionHeader("Community")
        SettingItem(
            icon = R.drawable.star_icon,
            title = "Star on GitHub",
            onClick = { openChromeTab("https://github.com/qeiq/Savr", context) }
        )
        Spacer(Modifier.height(4.dp))
        SettingItem(
            icon = R.drawable.bug_icon,
            title = "Report Issue",
            onClick = { openChromeTab("https://github.com/qeiq/Savr/issues", context) }
        )

        Spacer(Modifier.height(12.dp))

        SectionHeader("About")
        SettingItem(
            icon = R.drawable.about_icon,
            title = "App version",
            subtitle = versionName,
            onClick = { }
        )

        Spacer(Modifier.height(12.dp))
    }

    if (state.showThemeSheet) {
        ThemeSheet(
            current = state.themeMode,
            onSelect = { viewModel.onEvent(SettingEvents.SelectTheme(it)) },
            onDismiss = { viewModel.onEvent(SettingEvents.HideThemeSheet) }
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 24.dp, top = 16.dp, bottom = 4.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemeSheet(
    current: ThemeMode,
    onSelect: (ThemeMode) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Text(
            text = "Choose Theme",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 24.dp, top = 8.dp, bottom = 8.dp)
        )
        Column(Modifier.selectableGroup()) {
            ThemeOption("Light", ThemeMode.LIGHT, current, onSelect)
            ThemeOption("Dark", ThemeMode.DARK, current, onSelect)
            ThemeOption("System default", ThemeMode.SYSTEM, current, onSelect)
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun ThemeOption(
    label: String,
    mode: ThemeMode,
    current: ThemeMode,
    onSelect: (ThemeMode) -> Unit
) {
    val isSelected = mode == current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .clip(MaterialTheme.shapes.extraLarge)
            .selectable(
                selected = isSelected,
                onClick = { onSelect(mode) },
                role = Role.RadioButton
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = null,
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.primary,
                unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}
