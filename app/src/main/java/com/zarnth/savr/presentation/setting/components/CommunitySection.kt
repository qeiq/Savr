package com.zarnth.savr.presentation.setting.components

import android.content.Context
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zarnth.savr.R
import com.zarnth.savr.openChromeTab

@Composable
fun CommunitySection(context: Context) {
    Spacer(Modifier.height(12.dp))
    SectionHeader("Community")
    SettingItem(
        icon = R.drawable.github_icon,
        title = "Star on GitHub",
        onClick = { openChromeTab("https://github.com/qeiq/Savr", context) }
    )
    Spacer(Modifier.height(4.dp))
    SettingItem(
        icon = R.drawable.bug_icon,
        title = "Report Issue",
        onClick = { openChromeTab("https://github.com/qeiq/Savr/issues", context) }
    )
}
