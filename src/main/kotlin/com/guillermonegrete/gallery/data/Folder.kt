package com.guillermonegrete.gallery.data

import com.guillermonegrete.gallery.repository.FolderDto

data class Folder(
    val name:String,
    val coverUrl: String,
    val coverFilename: String,
    val count: Int,
    val id: Long,
)

data class PagedFolderResponse(
    val name: String,
    val page: SimplePage<Folder>,
)

fun MediaFolder.toDto(fileName: String, ipAddress: String): Folder {
    val coverUrl = "http://$ipAddress/images/$name/$fileName"
    return Folder(name, coverUrl, fileName, files.size, id)
}

fun MediaFolder.toDto(ipAddress: String): Folder {
    val filename = coverFile?.filename ?: files.firstOrNull()?.filename ?: ""
    return Folder(name, "http://$ipAddress/images/$name/$filename", filename, files.size, id)
}

fun FolderDto.toFolder(ipAddress: String) = Folder(name, "http://$ipAddress/images/$name/$coverUrl", coverUrl ?: "", count, id)
