package com.zarnth.savr.link_fetcher

import androidx.annotation.IntDef

data class LinkPreviewData(
    val title: String? = null,
    val description: String? = null,
    val imageUrl: String? = null,
    val url: String? = null,
    @LinkPreviewType val linkContentType: Int = LinkPreviewType.WEBPAGE,
)

@Target(
    AnnotationTarget.PROPERTY,
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.TYPE
)
@Retention(AnnotationRetention.BINARY)
@IntDef(
    LinkPreviewType.WEBPAGE,
    LinkPreviewType.PROFILE,
    LinkPreviewType.VIDEO
)
annotation class LinkPreviewType {
    companion object {
        const val WEBPAGE = 0
        const val PROFILE = 1
        const val VIDEO = 2
    }
}
