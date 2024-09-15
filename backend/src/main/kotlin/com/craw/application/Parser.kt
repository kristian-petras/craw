package com.craw.application

import io.ktor.http.Url
import io.ktor.http.toURI
import org.jsoup.Jsoup

class Parser {
    fun parse(
        requestUrl: Url,
        payload: String,
        regex: Regex,
        cache: Set<String>,
    ): ParseResult {
        try {
            val document = Jsoup.parse(payload)
            val title = document.title()
            val links =
                document.select("a[href]")
                    .asSequence()
                    .map { link -> link.attr("href") }
                    .map {
                        if (it.startsWith("http://") || it.startsWith("https://")) {
                            it.toString()
                        } else {
                            val base = requestUrl.toURI()
                            "${base.scheme}://${base.authority}/$it"
                        }
                    }
                    .filter { it.isNotBlank() }
                    .filterNot { it == requestUrl.toString() }
                    .filterNot { cache.contains(it) }
                    .distinct()
                    .toList()

            val (matches, rest) = links.partition { regex.containsMatchIn(it) }
            return ParseResult.Success(title, matches, rest)
        } catch (e: Exception) {
            return ParseResult.Failure(500, e.message ?: "Failed to parse payload")
        }
    }

    sealed interface ParseResult {
        data class Success(val title: String?, val matches: List<String>, val rest: List<String>) : ParseResult

        data class Failure(val status: Int, val message: String) : ParseResult
    }
}
