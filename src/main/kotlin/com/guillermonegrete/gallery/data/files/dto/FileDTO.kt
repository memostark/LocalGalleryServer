package com.guillermonegrete.gallery.data.files.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant

sealed class FileDTO(
    @get:JsonProperty("file_type")
    val type: FileType
)

data class ImageFileDTO(val url: String, val width: Int, val height: Int, val creationDate: Instant, val lastModified: Instant): FileDTO(FileType.Image)

data class VideoFileDTO(val url: String, val width: Int, val height: Int, val creationDate: Instant, val lastModified: Instant, val duration: Int): FileDTO(FileType.Video)

enum class FileType{
    Image,
    Video
}
