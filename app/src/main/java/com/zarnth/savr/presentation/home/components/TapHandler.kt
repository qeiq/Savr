package com.zarnth.savr.presentation.home.components

import android.content.Context
import androidx.compose.ui.platform.Clipboard
import com.zarnth.savr.domain.model.Bookmark
import com.zarnth.savr.openChromeTab
import com.zarnth.savr.presentation.home.HomeEvents
import com.zarnth.savr.presentation.home.HomeViewModel
import com.zarnth.savr.presentation.setting.TapAction

fun handleTap(
    item: Bookmark,
    tapAction: TapAction,
    context: Context,
    clipboard: Clipboard,
    viewModel: HomeViewModel
) {
    when (tapAction) {
        TapAction.OPEN_BROWSER -> openChromeTab(item.url, context)
        TapAction.COPY_LINK -> item.url.let { clipboard.nativeClipboard.text = it }
        TapAction.SHOW_PREVIEW -> viewModel.homeEvents(HomeEvents.BookmarkPreviewClick(item))
    }
}
