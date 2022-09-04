package com.guillermonegrete.gallery.tags

import com.guillermonegrete.gallery.data.MediaFile
import com.guillermonegrete.gallery.repository.MediaFileRepository
import com.guillermonegrete.gallery.tags.data.TagEntity
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@RestController
class TagsController(private val tagRepo: TagsRepository, private val filesRepo: MediaFileRepository) {

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

            file.addTag(tag)
            tagRepo.save(tag)
        }.orElseThrow { Exception("File with id $id not found") }
        return ResponseEntity(newTag, HttpStatus.OK)
    }

    @GetMapping("tags/{id}/files")
    fun getFilesByTag(@PathVariable id: Long): ResponseEntity<List<MediaFile>>{
        if(!tagRepo.existsById(id)) throw Exception("Tag with id $id not found")

        val files = filesRepo.findFilesByTagsId(id)
        return ResponseEntity(files, HttpStatus.OK)
    }
}
