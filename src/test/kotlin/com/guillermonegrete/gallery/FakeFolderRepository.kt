package com.guillermonegrete.gallery

import com.guillermonegrete.gallery.data.ImageFile
import org.assertj.core.util.VisibleForTesting
import java.lang.RuntimeException

class FakeFolderRepository: FoldersRepository {

    var foldersServiceData = arrayListOf<String>()

    private var shouldReturnError = false

    fun setReturnError(value: Boolean) {
        shouldReturnError = value
    }

    override fun getFolders(path: String): List<String> {
        if(shouldReturnError) throw RuntimeException()
        else return foldersServiceData
    }

    override fun getImageNames(folder: String): Set<String> {
        TODO("Not yet implemented")
    }

    override fun getImageInfo(path: String): ImageFile? {
        TODO("Not yet implemented")
    }

    override fun getImages(folder: String): List<ImageFile> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    @VisibleForTesting
    fun addFolders(vararg folders: String) {
        for (folder in folders) {
            foldersServiceData.add(folder)
        }
    }
}