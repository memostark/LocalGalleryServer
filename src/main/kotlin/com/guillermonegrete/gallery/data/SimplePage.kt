package com.guillermonegrete.gallery.data

data class SimplePage<T>(
        val items: List<T>,
        val totalPages: Int,
        val totalItems: Int
)