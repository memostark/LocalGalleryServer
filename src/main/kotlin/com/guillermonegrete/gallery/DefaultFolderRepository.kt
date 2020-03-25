package com.guillermonegrete.gallery

import org.springframework.stereotype.Component
import java.io.File

@Component
class DefaultFolderRepository: FoldersRepository {

    override fun getFolders(path: String): List<String> {
        return File(path).list()?.toList() ?: emptyList()
    }
}