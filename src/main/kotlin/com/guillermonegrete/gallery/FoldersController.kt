package com.guillermonegrete.gallery

import com.guillermonegrete.gallery.data.Folder
import com.guillermonegrete.gallery.data.GetFolderResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.io.File
import java.net.InetAddress

@RestController
class FoldersController(val repository: FoldersRepository){

    @Value("\${BASE_PATH}")
    private lateinit var basePath: String
    private val ipAddress: String by lazy { getLocalIpAddress() }

    @GetMapping("/folders")
    fun rootFolders(): GetFolderResponse{

        val folders = repository.getFolders(basePath).map {
            val folder = repository.getFolders("$basePath/$it")
            val coverFilename = getFirstImageFile(folder)
            val coverUrl = "http://$ipAddress/images/$it/$coverFilename"

            Folder(it, coverUrl, folder.size)
        }

        return GetFolderResponse(File(basePath).nameWithoutExtension, folders)
    }


    /**
     * Gets the first filename that is a image of the given list.
     */
    fun getFirstImageFile(files: List<String>): String{
        return files.firstOrNull {
            image_suffixes.contains( it.split(".").last() )
        } ?: ""
    }


    fun getLocalIpAddress(): String{
        val inetAddress = InetAddress.getLocalHost()
        return inetAddress.hostAddress
    }

    companion object{
        val image_suffixes = setOf("jpg", "jpeg", "png")
    }
}