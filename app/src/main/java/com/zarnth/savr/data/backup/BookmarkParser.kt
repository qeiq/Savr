package com.zarnth.savr.data.backup

data class ParsedBookmark(
    val url: String,
    val title: String,
    val collection: String? = null
)

data class BrowserImportResult(
    val imported: Int,
    val skipped: Int,
    val collections: Int
)

class BookmarkParser {

    fun parse(html: String): Pair<List<ParsedBookmark>, List<String>> {
        val normalized = html.replace(Regex(">\\s+<"), "><")

        data class Tag(val position: Int, val type: String, val value: String)
        val tags = mutableListOf<Tag>()

        val aRegex = Regex("<A\\s+[^>]*HREF=\"([^\"]+)\"([^>]*)>(.*?)</A>",
            setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
        val h3Regex = Regex("<H3[^>]*>(.*?)</H3>",
            setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
        val dlEndRegex = Regex("</DL>", RegexOption.IGNORE_CASE)

        for (m in h3Regex.findAll(normalized)) {
            tags.add(Tag(m.range.first, "folder", m.groupValues[1].trim()))
        }
        for (m in aRegex.findAll(normalized)) {
            tags.add(Tag(m.range.first, "bookmark", "${m.groupValues[1]}|||${m.groupValues[3]}"))
        }
        for (m in dlEndRegex.findAll(normalized)) {
            tags.add(Tag(m.range.first, "close", ""))
        }

        tags.sortBy { it.position }

        val bookmarks = mutableListOf<ParsedBookmark>()
        val folderStack = ArrayDeque<String>()
        val seenCollections = mutableSetOf<String>()
        val seenUrls = mutableSetOf<String>()

        for (tag in tags) {
            when (tag.type) {
                "folder" -> {
                    val name = tag.value.replaceHtmlEntities()
                    if (name.isNotBlank()) {
                        folderStack.addLast(name)
                        seenCollections.add(name)
                    }
                }
                "bookmark" -> {
                    val parts = tag.value.split("|||", limit = 2)
                    val url = parts[0]
                    val rawTitle = parts.getOrElse(1) { "" }
                        .replace(Regex("<[^>]+>"), "").trim().replaceHtmlEntities()
                    if (url.isNotBlank() && url !in seenUrls) {
                        seenUrls.add(url)
                        bookmarks.add(ParsedBookmark(
                            url = url,
                            title = rawTitle.ifBlank { url },
                            collection = folderStack.lastOrNull()
                        ))
                    }
                }
                "close" -> {
                    if (folderStack.isNotEmpty()) {
                        folderStack.removeLast()
                    }
                }
            }
        }

        return Pair(bookmarks, seenCollections.toList())
    }

    private fun String.replaceHtmlEntities(): String = this
        .replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&quot;", "\"")
        .replace("&#39;", "'")
        .replace("&apos;", "'")
}
