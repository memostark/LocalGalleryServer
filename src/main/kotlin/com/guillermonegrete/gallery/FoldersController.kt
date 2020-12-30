package com.guillermonegrete.gallery

import com.guillermonegrete.gallery.data.Folder
import com.guillermonegrete.gallery.data.GetFolderResponse
import com.guillermonegrete.gallery.data.ImageFile
import com.guillermonegrete.gallery.data.SimplePage
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.io.File
import java.net.InetAddress
import kotlin.math.ceil

@RestController
class FoldersController(val repository: FoldersRepository){

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
        var localFolders = cachedFolders.toList()

        if(localFolders.isEmpty())
            localFolders = repository.getFolders(basePath)

        if(subFolder in localFolders){
            val fileNames = repository.getImages("$basePath/$subFolder")

            val subFolderPath = "http://$ipAddress/images/$subFolder"
            return fileNames.map {
                ImageFile("$subFolderPath/${it.url}", it.width, it.height)
            }
        }else{
            throw RuntimeException("Folder path not found")
        }
    }

    @GetMapping("/folders/{subFolder}", params = ["page"])
    fun subFolder(@PathVariable subFolder: String, @RequestParam("page") page: Int, pageable: Pageable): SimplePage<ImageFile>{
        var localFolders = cachedFolders.toList()

        if(localFolders.isEmpty())
            localFolders = repository.getFolders(basePath)

        if(subFolder in localFolders){
            val start = pageable.offset.toInt()

            val imageFiles = repository.getImages("$basePath/$subFolder")
            val end = (start + pageable.pageSize).coerceAtMost(imageFiles.size)

            val subFolderPath = "http://$ipAddress/images/$subFolder"
            val subList = imageFiles.subList(start, end)
            val finalImages = subList.map {
                ImageFile("$subFolderPath/${it.url}", it.width, it.height)
            }
            val totalPages = if (pageable.pageSize == 0) 1 else ceil(imageFiles.size / pageable.pageSize.toDouble()).toInt()
            return SimplePage(finalImages, totalPages, imageFiles.size)
        }else{
            throw RuntimeException("Folder path not found")
        }
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