package com.guillermonegrete.gallery.data.files

import com.guillermonegrete.gallery.data.MediaFile
import com.guillermonegrete.gallery.data.files.dto.*
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

    fun toDtoWithHost(e: MediaFile, host: String): FileDTO {
        val url = "http://$host/images/${e.folder.name}/${e.filename}"
        return when(e){
            is VideoEntity -> VideoFileDTO(url, e.width, e.height, e.creationDate, e.lastModified, e.duration, e.tags, e.id)
            is ImageEntity -> ImageFileDTO(url, e.width, e.height, e.creationDate, e.lastModified, e.tags, e.id)
            else -> ImageFileDTO(url, e.width, e.height, e.creationDate, e.lastModified, e.tags, e.id)
        }
    }

    fun toSingleDto(e: MediaFile, host: String): FileDTO {
        val url = "http://$host/images/${e.folder.name}/${e.filename}"
        val folder = e.folder.name
        return when(e){
            is VideoEntity -> SingleVideoFile(folder, VideoFileDTO(url, e.width, e.height, e.creationDate, e.lastModified, e.duration, e.tags, e.id))
            is ImageEntity -> SingleImageFile(folder, ImageFileDTO(url, e.width, e.height, e.creationDate, e.lastModified, e.tags, e.id))
            else -> SingleImageFile(folder, ImageFileDTO(url, e.width, e.height, e.creationDate, e.lastModified, e.tags, e.id))
        }
    }

}
