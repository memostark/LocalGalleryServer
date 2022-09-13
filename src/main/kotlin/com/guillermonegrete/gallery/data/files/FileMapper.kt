package com.guillermonegrete.gallery.data.files

import com.guillermonegrete.gallery.data.MediaFile
import com.guillermonegrete.gallery.data.files.dto.FileDTO
import com.guillermonegrete.gallery.data.files.dto.ImageFileDTO
import com.guillermonegrete.gallery.data.files.dto.VideoFileDTO
import org.springframework.stereotype.Component

@Component
class FileMapper {

    fun toDto(e: MediaFile, url: String): FileDTO {
        return when(e){
            is VideoEntity -> VideoFileDTO(url, e.width, e.height, e.creationDate, e.lastModified, e.duration, e.tags, e.id)
            is ImageEntity -> ImageFileDTO(url, e.width, e.height, e.creationDate, e.lastModified, e.tags, e.id)
            else -> ImageFileDTO(url, e.width, e.height, e.creationDate, e.lastModified, e.tags, e.id)
        }
    }
}
