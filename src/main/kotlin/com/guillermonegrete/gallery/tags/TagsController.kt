package com.guillermonegrete.gallery.tags

import com.guillermonegrete.gallery.tags.data.TagEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(path= ["/tags"])
class TagsController(val tagRepo: TagsRepository) {

    @PostMapping("/add")
    fun addTag(@RequestParam name: String): String{
        tagRepo.save(TagEntity(name))
        return "Saved"
    }
}
