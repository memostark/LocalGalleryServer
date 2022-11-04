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
