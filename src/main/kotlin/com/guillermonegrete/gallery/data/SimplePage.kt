package com.guillermonegrete.gallery.data

data class SimplePage<T>(
    val items: List<T> = emptyList(),
    val totalPages: Int = 0,
    val totalItems: Int = 0,
)