package com.guillermonegrete.gallery.data.files

import com.guillermonegrete.gallery.data.Folder
import com.guillermonegrete.gallery.data.MediaFile
import com.guillermonegrete.gallery.data.files.dto.*
import com.guillermonegrete.gallery.tags.data.toBaseDto
import org.springframework.stereotype.Component

@Component
class FileMapper {

    fun toDto(e: MediaFile, url: String): FileDTO {
        val base = BaseFile(url, e.filename, e.width, e.height, e.creationDate, e.lastModified, e.tags.toBaseDto(), e.id)
        return when(e){
            is VideoEntity -> VideoFileDTO(e.duration, base)
            is ImageEntity -> ImageFileDTO(base)
            else -> ImageFileDTO(base)
        }
    }

    fun toDtoWithHost(e: MediaFile, host: String): FileDTO {
        val url = "http://$host/images/${e.folder.name}/${e.filename}"
        val base = BaseFile(url, e.filename, e.width, e.height, e.creationDate, e.lastModified, e.tags.toBaseDto(), e.id)
        return when(e){
            is VideoEntity -> VideoFileDTO(e.duration, base)
            is ImageEntity -> ImageFileDTO(base)
            else -> ImageFileDTO(base)
        }
    }

    fun toSingleDto(e: MediaFile, host: String): FileDTO {
        val url = "http://$host/images/${e.folder.name}/${e.filename}"
        val folder = Folder(e.folder.name, "", "", e.folder.files.size, e.folder.id)
        val base = BaseFile(url, e.filename, e.width, e.height, e.creationDate, e.lastModified, e.tags.toBaseDto(), e.id)
        return when(e){
            is VideoEntity -> SingleVideoFile(folder, VideoFileDTO(e.duration, base))
            is ImageEntity -> SingleImageFile(folder, ImageFileDTO(base))
            else -> SingleImageFile(folder, ImageFileDTO(base))
        }
    }

}
