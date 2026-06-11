package com.zarnth.savr.link_fetcher

class LinkPreviewService(
    private val networkFetcher: NetworkFetcher = NetworkFetcher(),
    private val metadataExtractor: MetadataExtractor = MetadataExtractor(),
    private val contentTypeDetector: ContentTypeDetector = ContentTypeDetector()
) {

    fun getLinkPreviewData(url: String, agent: String): LinkPreviewData? = try {
        val doc = networkFetcher.fetchDocument(url, agent)
        if (doc != null) {
            val title = metadataExtractor.extractTitle(doc)
            val description = metadataExtractor.extractDescription(doc)
            val imageUrl = metadataExtractor.extractImageUrl(doc, doc.baseUri())

            LinkPreviewData(
                title = title.trim(),
                description = description.trim(),
                imageUrl = imageUrl.trim(),
                url = url.trim(),
                linkContentType = contentTypeDetector.detectContentType(url)
            )
        } else {
            null
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
