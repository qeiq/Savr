package com.zarnth.savr.presentation.setting.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zarnth.savr.R
import com.zarnth.savr.presentation.setting.SettingEvents
import com.zarnth.savr.presentation.setting.SettingState
import com.zarnth.savr.presentation.setting.SettingViewModel
import com.zarnth.savr.presentation.setting.TapAction
import com.zarnth.savr.presentation.setting.ViewMode

@Composable
fun GeneralSection(state: SettingState, viewModel: SettingViewModel) {
    Spacer(Modifier.height(12.dp))
    SectionHeader("General")
    SettingItem(
        icon = R.drawable.touch_icon,
        title = "Default tap action",
        subtitle = when (state.tapAction) {
            TapAction.SHOW_PREVIEW -> "Preview"
            TapAction.OPEN_BROWSER -> "Open in browser"
            TapAction.COPY_LINK -> "Copy link"
        },
        onClick = { viewModel.onEvent(SettingEvents.ShowTapActionSheet) }
    )
    Spacer(Modifier.height(4.dp))
    SettingItem(
        icon = if (state.viewMode == ViewMode.GRID) R.drawable.grid_icon else R.drawable.list_icon,
        title = "View mode",
        subtitle = if (state.viewMode == ViewMode.GRID) "Grid" else "List",
        onClick = { viewModel.onEvent(SettingEvents.ShowViewModeSheet) }
    )
}
