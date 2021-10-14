package com.guillermonegrete.gallery

import com.guillermonegrete.gallery.services.FolderProcessingService
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
class GalleryApplication{
    @Value("\${base.path}")
    private lateinit var basePath: String

    @Value("\${checkfolder}")
    private var folderCheck: Boolean = false

    @Bean
    fun checkFolders(processingService: FolderProcessingService): CommandLineRunner {
        return CommandLineRunner {
            println("Check folders: $folderCheck")
            if(!folderCheck) return@CommandLineRunner

            processingService.processFolder(basePath)
        }
    }
}
    
fun main(args: Array<String>) {
    runApplication<GalleryApplication>(*args)
}
