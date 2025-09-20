package com.guillermonegrete.gallery.data

data class Folder(
        val name:String,
        val coverUrl: String,
        val count: Int,
        val id: Long,
)

data class PagedFolderResponse(
        val name: String,
        val page: SimplePage<Folder>
)

fun MediaFolder.toDto(fileName: String, ipAddress: String): Folder {
    val coverUrl = "http://$ipAddress/images/$name/$fileName"
    return Folder(name, coverUrl, files.size, id)
}

fun MediaFolder.toDto() = Folder(name, coverFile?.filename ?: files.firstOrNull()?.filename ?: "", files.size, id)
