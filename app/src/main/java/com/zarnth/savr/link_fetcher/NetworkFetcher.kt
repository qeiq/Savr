package com.zarnth.savr.link_fetcher

import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class NetworkFetcher {

     fun fetchDocument(url: String, agent: String): Document? {
        return try {
            val client = OkHttpClient.Builder().followRedirects(true).followSslRedirects(true).build()

            val request = Request.Builder()
                .url(url)
                .header("User-Agent", agent)
                .header("Accept-Language", "en-US,en;q=0.9")
                .build()

            val response = client.newCall(request).execute()

            // Automatically follows redirects
            val finalUrl = response.request.url.toString()
            val body = response.body?.string()

            if (!body.isNullOrEmpty()) {
                val doc = Jsoup.parse(body, finalUrl) // Final URL important for resolving relative paths
                doc
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}