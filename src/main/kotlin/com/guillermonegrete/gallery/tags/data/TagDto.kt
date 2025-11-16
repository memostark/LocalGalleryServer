package com.guillermonegrete.gallery.tags.data

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import java.time.Instant
import java.time.temporal.ChronoUnit

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "tag_type", visible = true)
sealed class TagDto(
    @get:JsonProperty("tag_type")
    val type: TagType
)

@JsonTypeName("File")
data class TagFileDto(
    val name: String,
    val count: Long?,
    /**
     * By default, the db saves in seconds, truncate to avoid having different milliseconds
     */
    val creationDate: Instant = Instant.now().truncatedTo(ChronoUnit.SECONDS),
    val id: Long = 0,
): TagDto(TagType.File)

@JsonTypeName("Folder")
data class TagFolderDto(
    val name: String,
    val count: Long,
    /**
     * By default, the db saves in seconds, truncate to avoid having different milliseconds
     */
    val creationDate: Instant = Instant.now().truncatedTo(ChronoUnit.SECONDS),
    val id: Long = 0,
): TagDto(TagType.Folder)

enum class TagType{
    Folder,
    File,
}
