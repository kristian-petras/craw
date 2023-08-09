package model

import kotlin.time.Duration

data class CrawledRecord(
    val url: String,
    val crawlTime: Duration,
    val title: String,
    val links: List<String>,
    val crawledLinks: List<String>
)