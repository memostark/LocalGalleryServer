package com.guillermonegrete.gallery

import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@RestController
class FoldersController(val repository: FoldersRepository){

    @Value("\${BASE_PATH}")
    private lateinit var basePath: String

    @GetMapping("/folders")
    fun rootFolders(): List<String>{
        return repository.getFolders(basePath)
    }
}