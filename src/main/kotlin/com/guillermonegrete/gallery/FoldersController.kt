package com.guillermonegrete.gallery

import com.guillermonegrete.gallery.data.Folder
import com.guillermonegrete.gallery.data.GetFolderResponse
import com.guillermonegrete.gallery.data.ImageFile
import com.guillermonegrete.gallery.data.SimplePage
import com.guillermonegrete.gallery.repository.MediaFileRepository
import com.guillermonegrete.gallery.repository.MediaFolderRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.io.File
import java.net.InetAddress

@RestController
class FoldersController(
    val repository: FoldersRepository,
    val mediaFolderRepo: MediaFolderRepository,
    val mediaFilesRepo: MediaFileRepository,
){

    @Value("\${base.path}")
    private lateinit var basePath: String
    private val ipAddress: String by lazy { getLocalIpAddress() }

    private var cachedFolders = emptyList<String>()

    @GetMapping("/folders")
    fun rootFolders(): GetFolderResponse{
        cachedFolders = repository.getFolders(basePath)

        val folders = cachedFolders.map {
            val folder = repository.getFolders("$basePath/$it")
            val coverFilename = getFirstImageFile(folder)
            val coverUrl = "http://$ipAddress/images/$it/$coverFilename"

            Folder(it, coverUrl, folder.size)
        }

        return GetFolderResponse(File(basePath).nameWithoutExtension, folders)
    }

    @GetMapping("/folders/{subFolder}")
    fun subFolder(@PathVariable subFolder: String): List<ImageFile>{

        val mediaFolder = mediaFolderRepo.findByName(subFolder) ?: throw RuntimeException("Folder entity for $subFolder not found")
        val subFolderPath = "http://$ipAddress/images/$subFolder"

        return mediaFolder.files.map {
            ImageFile("$subFolderPath/${it.filename}", it.width, it.height)
        }
    }

    @GetMapping("/folders/{subFolder}", params = ["page"])
    fun subFolder(@PathVariable subFolder: String, @RequestParam("page") page: Int, pageable: Pageable): SimplePage<ImageFile>{
        val mediaFolder = mediaFolderRepo.findByName(subFolder) ?: throw RuntimeException("Folder path not found")

        val filesPage = mediaFilesRepo.findAllByFolder(mediaFolder, pageable)
        val subFolderPath = "http://$ipAddress/images/$subFolder"

        val finalImages = filesPage.content.map {
            ImageFile("$subFolderPath/${it.filename}", it.width, it.height)
        }

        return SimplePage(finalImages, filesPage.totalPages, filesPage.totalElements.toInt())
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