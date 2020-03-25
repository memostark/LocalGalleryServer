package com.guillermonegrete.gallery

interface FoldersRepository {

    fun getFolders(path: String): List<String>
}