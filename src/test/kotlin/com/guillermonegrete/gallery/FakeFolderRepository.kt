package com.guillermonegrete.gallery

import com.guillermonegrete.gallery.data.MediaFile
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

    override fun getMediaInfo(path: String): MediaFile? {
        TODO("Not yet implemented")
    }

    override fun getMedia(folder: String): List<MediaFile> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun createFolder(path: String): Boolean {
        TODO("Not yet implemented")
    }

    @VisibleForTesting
    fun addFolders(vararg folders: String) {
        for (folder in folders) {
            foldersServiceData.add(folder)
        }
    }
}