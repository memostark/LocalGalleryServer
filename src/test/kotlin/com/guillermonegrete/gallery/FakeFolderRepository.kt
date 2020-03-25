package com.guillermonegrete.gallery

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

    @VisibleForTesting
    fun addFolders(vararg folders: String) {
        for (folder in folders) {
            foldersServiceData.add(folder)
        }
    }
}