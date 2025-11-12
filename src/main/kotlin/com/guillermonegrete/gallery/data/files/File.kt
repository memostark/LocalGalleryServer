package com.guillermonegrete.gallery.data.files

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonUnwrapped
import com.guillermonegrete.gallery.data.SimplePage
import com.guillermonegrete.gallery.data.files.dto.FileDTO
import com.guillermonegrete.gallery.thumbnails.thumbnailSizesMap

data class PagedFileResponse(
    @get:JsonUnwrapped
    val page: SimplePage<FileDTO>,
    @get:JsonProperty("thumbnail_sizes")
    val thumbnailSizes: Map<String, Int> = thumbnailSizesMap,
)
