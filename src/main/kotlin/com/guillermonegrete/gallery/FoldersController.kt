package com.guillermonegrete.gallery

import com.guillermonegrete.gallery.data.Folder
import com.guillermonegrete.gallery.data.GetFolderResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.io.File

@RestController
class FoldersController(val repository: FoldersRepository){

    @Value("\${BASE_PATH}")
    private lateinit var basePath: String

    @GetMapping("/folders")
    fun rootFolders(): GetFolderResponse{

        val folders = repository.getFolders(basePath).map {
            val folder = repository.getFolders("$basePath/$it")
            val coverUrl = getFirstImageFile(folder)

            Folder(it, coverUrl, folder.size)
        }

        return GetFolderResponse(File(basePath).nameWithoutExtension, folders)
    }


    fun getFirstImageFile(files: List<String>): String{
        return files.firstOrNull {
            image_suffixes.contains( it.split(".").last() )
        } ?: ""
    }

    companion object{
        val image_suffixes = setOf("jpg", "jpeg", "png")
    }
}