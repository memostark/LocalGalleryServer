package com.guillermonegrete.gallery.tags

import com.guillermonegrete.gallery.data.SimplePage
import com.guillermonegrete.gallery.data.files.FileMapper
import com.guillermonegrete.gallery.data.files.dto.FileDTO
import com.guillermonegrete.gallery.repository.MediaFileRepository
import com.guillermonegrete.gallery.repository.MediaFolderRepository
import com.guillermonegrete.gallery.tags.data.TagEntity
import com.guillermonegrete.gallery.tags.data.TagRequest
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.InetAddress


@RestController
class TagsController(
    private val tagRepo: TagsRepository,
    private val filesRepo: MediaFileRepository,
    private val folderRepo: MediaFolderRepository,
    private val fileMapper: FileMapper,
) {

    private val ipAddress: String by lazy { getLocalIpAddress() }

    @GetMapping("/tags")
    fun getAllTags(): ResponseEntity<List<TagEntity>> {
        val tags = tagRepo.findAll()
        return if (tags.isEmpty()) ResponseEntity(HttpStatus.NO_CONTENT) else ResponseEntity(tags, HttpStatus.OK)
    }

    @PostMapping("/tags/add")
    fun createTag(@RequestParam name: String): ResponseEntity<TagEntity>{
        return try {
            val tag = tagRepo.save(TagEntity(name))
            ResponseEntity(tag, HttpStatus.OK)
        } catch (ex: DataIntegrityViolationException){
            println("Duplicate entry for $name")
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @PostMapping("files/{id}/tags")
    fun addTag(@PathVariable id: Long, @RequestBody tag: TagRequest): ResponseEntity<TagEntity> {
        val newTag = filesRepo.findById(id).map { file ->
            val tagId = tag.id

            if(tagId != 0L){
                val savedTag = tagRepo.findById(tagId)
                    .orElseThrow { Exception("Tag with id $tagId not found") }
                file.addTag(savedTag)
                filesRepo.save(file)
                return@map savedTag
            }

            val completeTag = tagRepo.findByName(tag.name) ?: TagEntity(tag.name, id = tag.id)
            file.addTag(completeTag)
            tagRepo.save(completeTag)
        }.orElseThrow { Exception("File with id $id not found") }
        return ResponseEntity(newTag, HttpStatus.OK)
    }

    @GetMapping("tags/{id}/files")
    fun getFilesByTag(@PathVariable id: Long, pageable: Pageable): ResponseEntity<SimplePage<FileDTO>>{
        if(!tagRepo.existsById(id)) throw Exception("Tag with id $id not found")

        val filesPage = filesRepo.findFilesByTagsId(id, pageable)
        val finalFiles = filesPage.content.map { fileMapper.toDtoWithHost(it, ipAddress) }

        val page  = SimplePage(finalFiles, filesPage.totalPages, filesPage.totalElements.toInt())
        return ResponseEntity(page, HttpStatus.OK)
    }

    @PostMapping("tags/{id}/files")
    fun addTagToFiles(@PathVariable id: Long, @RequestBody fileIds: List<Long>): ResponseEntity<List<FileDTO>> {
        val tag = tagRepo.findByIdOrNull(id) ?: throw RuntimeException("Tag id $id not found")

        val files = filesRepo.findByIdIn(fileIds)

        val updatedFiles = files.filter { it.addTag(tag) }
        filesRepo.saveAll(files)
        val fileDTOs = updatedFiles.map { fileMapper.toDtoWithHost(it, ipAddress) }
        return ResponseEntity(fileDTOs, HttpStatus.OK)
    }

    @PostMapping("files/{id}/multitag")
    fun addTagsToFile(@PathVariable id: Long, @RequestBody tagIds: List<Long>): ResponseEntity<List<TagEntity>> {
        val file = filesRepo.findByIdOrNull(id) ?: throw RuntimeException("File id $id not found")

        val tags = tagRepo.findByIdIn(tagIds)

        tags.forEach { file.addTag(it) }
        filesRepo.save(file)
        return ResponseEntity(tags, HttpStatus.OK)
    }

    @GetMapping("folders/{id}/tags")
    fun getTagsByFolder(@PathVariable id: Long): ResponseEntity<Set<TagEntity>> {
        // Another implementation of this is adding another field to the "media_tags" table for the folder id.
        // And querying that table for tags with the folder id. It may improve performance.
        val folder = folderRepo.findByIdOrNull(id) ?: throw RuntimeException("Folder id $id not found")
        val tags = mutableSetOf<TagEntity>()
        folder.files.forEach {
            tags.addAll(it.tags)
        }

        return ResponseEntity(tags, HttpStatus.OK)
    }

    @GetMapping("folders/{folderId}/tags/{tagId}")
    fun getFilesByFolderAndTag(@PathVariable folderId: Long, @PathVariable tagId: Long, pageable: Pageable): ResponseEntity<SimplePage<FileDTO>> {
        if(!tagRepo.existsById(tagId)) throw Exception("Tag with id $tagId not found")

        if(!folderRepo.existsById(folderId)) throw Exception("Folder with id $folderId not found")

        val filesPage = filesRepo.findFilesByTagsIdAndFolderId(tagId, folderId, pageable)
        val finalFiles = filesPage.content.map { fileMapper.toDtoWithHost(it, ipAddress) }

        val page  = SimplePage(finalFiles, filesPage.totalPages, filesPage.totalElements.toInt())
        return ResponseEntity(page, HttpStatus.OK)
    }

    @DeleteMapping("/files/{fileId}/tags/{tagId}")
    fun deleteTagFromTutorial(
        @PathVariable fileId: Long,
        @PathVariable tagId: Long
    ): ResponseEntity<HttpStatus> {
        val file = filesRepo.findById(fileId)
            .orElseThrow { RuntimeException("Not found Tutorial with id = $fileId") }
        file.removeTag(tagId)
        filesRepo.save(file)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }

    @DeleteMapping("/tags/{id}")
    fun deleteTag(@PathVariable("id") id: Long): ResponseEntity<HttpStatus> {
        tagRepo.deleteById(id)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }

    fun getLocalIpAddress(): String{
        val inetAddress = InetAddress.getLocalHost()
        return inetAddress.hostAddress
    }
}
