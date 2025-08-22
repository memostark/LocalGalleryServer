package com.guillermonegrete.gallery.tags

import com.guillermonegrete.gallery.config.NetworkConfig
import com.guillermonegrete.gallery.data.SimplePage
import com.guillermonegrete.gallery.data.files.FileMapper
import com.guillermonegrete.gallery.data.files.dto.FileDTO
import com.guillermonegrete.gallery.repository.MediaFileRepository
import com.guillermonegrete.gallery.repository.MediaFolderRepository
import com.guillermonegrete.gallery.tags.data.TagDto
import com.guillermonegrete.gallery.tags.data.TagEntity
import com.guillermonegrete.gallery.tags.data.TagRequest
import com.guillermonegrete.gallery.tags.data.toDto
import org.springframework.beans.factory.annotation.Autowired
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

    @Autowired
    private lateinit var networkConfig: NetworkConfig
    private val ipAddress: String by lazy { networkConfig.getLocalIpAddress() }

    @GetMapping("/tags")
    fun getAllTags(): ResponseEntity<List<TagDto>> {
        val tags = tagRepo.findAll()
        val tagsDto = tags.map { it.toDto() }
        return if (tagsDto.isEmpty()) ResponseEntity(HttpStatus.NO_CONTENT) else ResponseEntity(tagsDto, HttpStatus.OK)
    }

    @PostMapping("/tags/add")
    fun createTag(@RequestParam name: String): ResponseEntity<Any> {
        if (name.isBlank()) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Tag can't be blank")

        return try {
            val tag = tagRepo.save(TagEntity(name))
            ResponseEntity(tag, HttpStatus.OK)
        } catch (ex: DataIntegrityViolationException){
            println("Duplicate entry for $name")
            ResponseEntity("Duplicate tag", HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @PostMapping("files/{id}/tags")
    fun addTag(@PathVariable id: Long, @RequestBody tag: TagRequest): ResponseEntity<TagEntity> {
        if (tag.name.isBlank()) throw Exception("Tag can't be blank")

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
        val finalFiles = filesPage.content.map { fileMapper.toSingleDto(it, ipAddress) }

        val page  = SimplePage(finalFiles, filesPage.totalPages, filesPage.totalElements.toInt())
        return ResponseEntity(page, HttpStatus.OK)
    }

    @PostMapping("tags/files")
    fun getFilesByTags(@RequestBody rawIds: List<Long>, pageable: Pageable): ResponseEntity<SimplePage<FileDTO>>{
        if(rawIds.isEmpty()) throw Exception("The tag list is empty")

        val ids = rawIds.filter { tagRepo.existsById(it) }
        if (ids.isEmpty()) return ResponseEntity(SimplePage(), HttpStatus.OK)

        val filesPage = if (ids.size == 1) filesRepo.findFilesByTagsId(ids.first(), pageable) else filesRepo.findFilesByTagsIds(ids, pageable)

        val finalFiles = filesPage.content.map { fileMapper.toSingleDto(it, ipAddress) }

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
    fun getTagsByFolder(@PathVariable id: Long): ResponseEntity<Set<TagDto>> {
        val tagsDto = tagRepo.getTagsWithFilesByFolder(id)
        return ResponseEntity(tagsDto, HttpStatus.OK)
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

    @PostMapping("folders/{folderId}/files")
    fun getFilesByFolderAndTags(@PathVariable folderId: Long, @RequestBody tagIds: List<Long>, pageable: Pageable): ResponseEntity<SimplePage<FileDTO>>{
        if(!folderRepo.existsById(folderId)) throw Exception("Folder with id $folderId not found")
        if(tagIds.isEmpty()) throw Exception("The tag list is empty")

        val ids = tagIds.filter { tagRepo.existsById(it) }
        if (ids.isEmpty()) return ResponseEntity(SimplePage(), HttpStatus.OK)

        val filesPage = if (ids.size == 1) filesRepo.findFilesByTagsIdAndFolderId(ids.first(), folderId, pageable) else filesRepo.findFilesByTagsIdsAndFolderId(ids, folderId, pageable)
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
