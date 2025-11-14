package com.guillermonegrete.gallery

import java.io.File

interface FileProvider {
    fun createFromBase(path: String): File

    fun getFile(parent: File, child: String): File
}

class DefaultFileProvider(private val basePath: String): FileProvider {

    override fun createFromBase(path: String) = File(basePath, path)

    override fun getFile(parent: File, child: String) = File(parent, child)
}
