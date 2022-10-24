package com.livebarn.android.sreelibrary.model

data class Category(
    val key: String,
    val title: String,
    val gifs: MutableList<String> = mutableListOf(),
    var path: String? = null
)
