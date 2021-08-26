package com.guillermonegrete.gallery

import com.guillermonegrete.gallery.data.ImageFile

interface FoldersRepository {

    fun getFolders(path: String): List<String>

    fun getImageNames(folder: String): Set<String>

    fun getImageInfo(path: String): ImageFile?

    fun getImages(folder: String): List<ImageFile>
}