package com.guillermonegrete.gallery.data.files.dto

import com.fasterxml.jackson.annotation.JsonProperty

sealed class FileDTO(
    @get:JsonProperty("file_type")
    val type: FileType
)

data class ImageFileDTO(val url: String, val width: Int, val height: Int): FileDTO(FileType.Image)

data class VideoFileDTO(val url: String, val width: Int, val height: Int, val duration: Int): FileDTO(FileType.Video)

enum class FileType{
    Image,
    Video
}
