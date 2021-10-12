package com.guillermonegrete.gallery

import com.guillermonegrete.gallery.data.MediaFile

interface FoldersRepository {

    fun getFolders(path: String): List<String>

    fun getImageNames(folder: String): Set<String>

    fun getMediaInfo(path: String): MediaFile?

    fun getMedia(folder: String): List<MediaFile>
}