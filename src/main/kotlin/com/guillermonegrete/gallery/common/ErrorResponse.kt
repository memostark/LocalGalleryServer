package com.guillermonegrete.gallery.common

import java.time.LocalDateTime

data class ErrorResponse(
    val status: Int,
    val error: String,
    val details: Map<String, String>?,
    val timestamp: LocalDateTime = LocalDateTime.now()
)
