package com.guillermonegrete.gallery.data.files

import com.guillermonegrete.gallery.data.MediaFile
import com.guillermonegrete.gallery.data.files.dto.FileDTO
import com.guillermonegrete.gallery.data.files.dto.ImageFileDTO
import com.guillermonegrete.gallery.data.files.dto.VideoFileDTO
import org.springframework.stereotype.Component

@Component
class FileMapper {

    fun toDto(entity: MediaFile, url: String): FileDTO {
        return when(entity){
            is VideoEntity -> VideoFileDTO(url, entity.width, entity.height, entity.creationDate, entity.lastModified, entity.duration)
            is ImageEntity -> ImageFileDTO(url, entity.width, entity.height, entity.creationDate, entity.lastModified)
            else -> ImageFileDTO(url, entity.width, entity.height, entity.creationDate, entity.lastModified)
        }
    }
}
