package com.guillermonegrete.gallery

import com.guillermonegrete.gallery.config.NetworkConfig
import com.guillermonegrete.gallery.data.Folder
import com.guillermonegrete.gallery.data.MediaFolder
import com.guillermonegrete.gallery.data.PagedFolderResponse
import com.guillermonegrete.gallery.data.SimplePage
import com.guillermonegrete.gallery.data.files.FileMapper
import com.guillermonegrete.gallery.data.files.dto.FileDTO
import com.guillermonegrete.gallery.repository.MediaFileRepository
import com.guillermonegrete.gallery.repository.MediaFolderRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.io.File


@RestController
class FoldersController(
    val mediaFolderRepo: MediaFolderRepository,
    val mediaFilesRepo: MediaFileRepository,
    val fileMapper: FileMapper
){

    @Value("\${base.path}")
    private lateinit var basePath: String
    @Autowired
    private lateinit var networkConfig: NetworkConfig
    private val ipAddress: String by lazy { networkConfig.getLocalIpAddress() }

    @GetMapping("/folders")
    fun folders(@RequestParam(required = false) query: String?, pageable: Pageable): PagedFolderResponse{
        val folders = if(query == null) getFolderPage(pageable) else getFolderPage(query, pageable)

        val finalFolders = folders.content.map { folder ->
            val firstFilename = folder.coverUrl
            val coverUrl = "http://$ipAddress/images/${folder.name}/$firstFilename"

            folder.copy(coverUrl = coverUrl)
        }

        return PagedFolderResponse(getFolderName(), SimplePage(finalFolders, folders.totalPages, folders.totalElements.toInt()))
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
            fileMapper.toSingleDto(it, ipAddress)
        }

        return SimplePage(finalFiles, filesPage.totalPages, filesPage.totalElements.toInt())
    }

    @PatchMapping("/folder/{id}/cover/{fileId}")
    fun updateFolderCover(@PathVariable("id") id: Long, @PathVariable("fileId") fileId: Long): ResponseEntity<Folder> {
        val folder = mediaFolderRepo.findByIdOrNull(id) ?: throw RuntimeException("Folder id $id not found")
        val file = mediaFilesRepo.findByIdOrNull(fileId) ?: throw RuntimeException("File id $fileId not found")

        folder.coverFile = file

        val savedFolder = mediaFolderRepo.save(folder).toDto(file.filename)
        return ResponseEntity(savedFolder, HttpStatus.OK)
    }

    /**
     * Returns the page of folders by the given pageable.
     */
    private fun getFolderPage(pageable: Pageable): Page<Folder> {
        val sort = pageable.sort.firstOrNull()
        return if(sort?.property == "count") {
            // Sorting by child count is a special case because it's not an entity column
            // Created pageable without the "count" sort field, otherwise it will produce an error
            val newPageable = PageRequest.of(pageable.pageNumber, pageable.pageSize)
            val result = if(sort.isDescending) mediaFolderRepo.findAllMediaFolderByFileCountDesc(newPageable) else mediaFolderRepo.findAllMediaFolderByFileCountAsc(newPageable)
            result.map { Folder(it.name, it.coverUrl ?: "", it.count, it.id) }
        } else {
            mediaFolderRepo.findAll(pageable).map { Folder(it.name, it.getCover(), it.files.size, it.id) }
        }
    }

    /**
     * Returns the page of folders by the given pageable that contains the query in the name.
     */
    fun getFolderPage(query: String, pageable: Pageable): Page<Folder> {
        val sort = pageable.sort.firstOrNull()
        return if(sort?.property == "count") {
            val newPageable = PageRequest.of(pageable.pageNumber, pageable.pageSize)
            val result = if(sort.isDescending)
                mediaFolderRepo.findByNameContainingAndFileCountDesc(query, newPageable) else mediaFolderRepo.findByNameContainingAndFileCountAsc(query, newPageable)
            result.map { Folder(it.name, it.coverUrl ?: "", it.count, it.id) }
        } else {
            mediaFolderRepo.findByNameContaining(query, pageable).map { Folder(it.name, it.getCover(), it.files.size, it.id)  }
        }
    }

    fun MediaFolder.toDto(fileName: String): Folder {
        val coverUrl = "http://$ipAddress/images/$name/$fileName"
        return Folder(name, coverUrl, files.size, id)
    }

    fun getFolderName(): String {
        val folder = File(basePath)
        return if (folder.exists()) {
            folder.nameWithoutExtension
        } else {
            val paths = basePath.split("\\", "/")
            paths.last()
        }
    }

    private fun MediaFolder.getCover() = coverFile?.filename ?: files.firstOrNull()?.filename ?: ""
}
