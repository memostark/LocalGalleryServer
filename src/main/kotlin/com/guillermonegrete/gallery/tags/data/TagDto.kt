package com.guillermonegrete.gallery.tags.data

import java.time.Instant
import java.time.temporal.ChronoUnit

data class TagDto(
    val name: String,
    val count: Long,
    /**
     * By default, the db saves in seconds, truncate to avoid having different milliseconds
     */
    val creationDate: Instant = Instant.now().truncatedTo(ChronoUnit.SECONDS),
    val id: Long = 0,
)
