package com.guillermonegrete.gallery

import com.guillermonegrete.gallery.data.Folder
import com.guillermonegrete.gallery.data.MediaFolder
import com.guillermonegrete.gallery.data.PagedFolderResponse
import com.guillermonegrete.gallery.data.SimplePage
import com.guillermonegrete.gallery.data.files.FileMapper
import com.guillermonegrete.gallery.data.files.dto.FileDTO
import com.guillermonegrete.gallery.repository.MediaFileRepository
import com.guillermonegrete.gallery.repository.MediaFolderRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.io.File
import java.net.InetAddress

@RestController
class FoldersController(
    val mediaFolderRepo: MediaFolderRepository,
    val mediaFilesRepo: MediaFileRepository,
    val fileMapper: FileMapper
){

    @Value("\${base.path}")
    private lateinit var basePath: String
    private val ipAddress: String by lazy { getLocalIpAddress() }

    @GetMapping("/folders")
    fun folders(@RequestParam(required = false) query: String?, pageable: Pageable): PagedFolderResponse{
        val folders = if(query == null) getFolderPage(pageable) else getFolderPage(query, pageable)

        val finalFolders = folders.content.map { folder ->
            val firstFilename = folder.files.firstOrNull()?.filename ?: ""
            val coverUrl = "http://$ipAddress/images/${folder.name}/$firstFilename"

            Folder(folder.name, coverUrl, folder.files.size, folder.id)
        }

        return PagedFolderResponse(File(basePath).nameWithoutExtension, SimplePage(finalFolders, folders.totalPages, folders.totalElements.toInt()))
    }

    @GetMapping("/folders/{subFolder}")
    fun subFolder(@PathVariable subFolder: String): List<FileDTO>{

        val mediaFolder = mediaFolderRepo.findByName(subFolder) ?: throw RuntimeException("Folder entity for $subFolder not found")
        val subFolderPath = "http://$ipAddress/images/$subFolder"

        return mediaFolder.files.map {
            fileMapper.toDto(it, "$subFolderPath/${it.filename}")
        }
    }

    @GetMapping("/folders/{subFolder}", params = ["page"])
    fun subFolder(@PathVariable subFolder: String, @RequestParam("page") page: Int, pageable: Pageable): SimplePage<FileDTO>{
        val mediaFolder = mediaFolderRepo.findByName(subFolder) ?: throw RuntimeException("Folder path $subFolder not found")

        val filesPage = mediaFilesRepo.findAllByFolder(mediaFolder, pageable)
        val subFolderPath = "http://$ipAddress/images/$subFolder"

        val finalFiles = filesPage.content.map {
            fileMapper.toDto(it, "$subFolderPath/${it.filename}")
        }

        return SimplePage(finalFiles, filesPage.totalPages, filesPage.totalElements.toInt())
    }

    @GetMapping("/files")
    fun files(pageable: Pageable): SimplePage<FileDTO>{
        val filesPage = mediaFilesRepo.findAll(pageable)

        val finalFiles = filesPage.content.map {
            val subFolderPath = "http://$ipAddress/images/${it.folder.name}/${it.filename}"
            fileMapper.toDto(it, subFolderPath)
        }

        return SimplePage(finalFiles, filesPage.totalPages, filesPage.totalElements.toInt())
    }

    /**
     * Gets the first filename that is a image of the given list.
     */
    fun getFirstImageFile(files: List<String>): String{
        return files.firstOrNull {
            image_suffixes.contains( it.split(".").last() )
        } ?: ""
    }

    /**
     * Returns the page of folders by the given pageable.
     */
    private fun getFolderPage(pageable: Pageable): Page<MediaFolder> {
        val sort = pageable.sort.firstOrNull()
        return if(sort?.property == "count") {
            // Sorting by child count is a special case because it's not an entity column
            // Created pageable without the "count" sort field, otherwise it will produce an error
            val newPageable = PageRequest.of(pageable.pageNumber, pageable.pageSize)
            if(sort.isDescending) mediaFolderRepo.findAllMediaFolderByFileCountDesc(newPageable)  else mediaFolderRepo.findAllMediaFolderByFileCountAsc(newPageable)
        } else {
            mediaFolderRepo.findAll(pageable)
        }
    }

    /**
     * Returns the page of folders by the given pageable that contains the query in the name.
     */
    fun getFolderPage(query: String, pageable: Pageable): Page<MediaFolder> {
        val sort = pageable.sort.firstOrNull()
        return if(sort?.property == "count") {
            val newPageable = PageRequest.of(pageable.pageNumber, pageable.pageSize)
            if(sort.isDescending)
                mediaFolderRepo.findByNameContainingAndFileCountDesc(query, newPageable) else mediaFolderRepo.findByNameContainingAndFileCountAsc(query, newPageable)
        } else {
            mediaFolderRepo.findByNameContaining(query, pageable)
        }
    }

    fun getLocalIpAddress(): String{
        val inetAddress = InetAddress.getLocalHost()
        return inetAddress.hostAddress
    }

    companion object{
        val image_suffixes = setOf("jpg", "jpeg", "png")
    }
}
