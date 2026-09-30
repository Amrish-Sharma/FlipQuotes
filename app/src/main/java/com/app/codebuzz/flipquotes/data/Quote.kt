package com.app.codebuzz.flipquotes.data

data class Quote(
    val quote: String,
    val author: String,
    val theme: String
)

// Stable identifier used for bookmarks
val Quote.key: String get() = "${quote}_${author}"