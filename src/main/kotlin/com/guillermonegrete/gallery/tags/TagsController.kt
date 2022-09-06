package com.guillermonegrete.gallery.tags

import com.guillermonegrete.gallery.data.MediaFile
import com.guillermonegrete.gallery.repository.MediaFileRepository
import com.guillermonegrete.gallery.repository.MediaFolderRepository
import com.guillermonegrete.gallery.tags.data.TagEntity
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@RestController
class TagsController(
    private val tagRepo: TagsRepository,
    private val filesRepo: MediaFileRepository,
    private val folderRepo: MediaFolderRepository
) {

    @GetMapping("/tags")
    fun getAllTags(): ResponseEntity<List<TagEntity>> {
        val tags = tagRepo.findAll()
        return if (tags.isEmpty()) ResponseEntity(HttpStatus.NO_CONTENT) else ResponseEntity(tags, HttpStatus.OK)
    }

    @PostMapping("/tags/add")
    fun createTag(@RequestParam name: String): String{
        try {
            tagRepo.save(TagEntity(name))
        } catch (ex: DataIntegrityViolationException){
            println("Duplicate entry for $name")
        }

        return "Saved"
    }

    @PostMapping("files/{id}/tags")
    fun addTag(@PathVariable id: Long, @RequestBody tag: TagEntity): ResponseEntity<TagEntity> {
        val newTag = filesRepo.findById(id).map { file ->
            val tagId = tag.id

            if(tagId != 0L){
                val savedTag = tagRepo.findById(tagId)
                    .orElseThrow { Exception("Tag with id $tagId not found") }
                file.addTag(savedTag)
                filesRepo.save(file)
                return@map savedTag
            }

            val completeTag = tagRepo.findByName(tag.name) ?: tag
            file.addTag(completeTag)
            tagRepo.save(completeTag)
        }.orElseThrow { Exception("File with id $id not found") }
        return ResponseEntity(newTag, HttpStatus.OK)
    }

    @GetMapping("tags/{id}/files")
    fun getFilesByTag(@PathVariable id: Long): ResponseEntity<List<MediaFile>>{
        if(!tagRepo.existsById(id)) throw Exception("Tag with id $id not found")

        val files = filesRepo.findFilesByTagsId(id)
        return ResponseEntity(files, HttpStatus.OK)
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
}
