package com.craw.application

import org.jsoup.Jsoup

class Parser {
    fun parse(payload: String, regex: Regex): ParseResult {
        try {
            val document = Jsoup.parse(payload)
            val title = document.title()
            val links = document.select("a[href]").map { link -> link.attr("href") }
            val (matches, rest) = links.partition { regex.matches(it) }
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