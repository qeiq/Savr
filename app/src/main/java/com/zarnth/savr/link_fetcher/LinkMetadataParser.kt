package com.zarnth.savr.link_fetcher

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

 class LinkMetadataParser {

    companion object {
        private val AGENTS = arrayOf(
            "WhatsApp/2.24.8.76 A",
            "Mozilla/5.0",
            "facebookexternalhit/1.1", // Simple Facebook crawler - often faster
            "Twitterbot/1.0", // Twitter's crawler - good for onelink and other services
            "facebookexternalhit/1.1 (+http://www.facebook.com/externalhit_uatext.php)",
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.45 Safari/537.36",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/66.0.3359.181 Safari/537.36"
        )
    }

    suspend fun parse(url: String): LinkPreviewData? = withContext(Dispatchers.IO) {
        return@withContext try {
            try {
                var linkPreviewEntity: LinkPreviewData? = null
                AGENTS.forEach {
                    linkPreviewEntity = LinkPreviewService().getLinkPreviewData(
                        url, it
                    )
                    val isResultNull = checkNullParserResult(linkPreviewEntity)
                    if (!isResultNull) {
                        return@withContext linkPreviewEntity
                    }
                }
                return@withContext linkPreviewEntity
            } catch (e: Exception) {
                e.printStackTrace()
                return@withContext null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun checkNullParserResult(linkPreviewData: LinkPreviewData?): Boolean {
        return (linkPreviewData?.title.isNullOrEmpty() || linkPreviewData?.title == "null") && (linkPreviewData?.description.isNullOrEmpty() || linkPreviewData?.description == "null")
    }
}
