package com.guillermonegrete.gallery

import com.guillermonegrete.gallery.services.FolderProcessingService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
class GalleryApplication{
    @Value("\${base.path}")
    private lateinit var basePath: String

    @Autowired lateinit var service: FolderProcessingService

    @Bean
    fun checkFolders(folderRepository: FoldersRepository): CommandLineRunner {
        return CommandLineRunner {
            val folders = folderRepository.getFolders(basePath)
            for(folder in folders){
                service.processFolder(folder, basePath)
            }
            println("All folders processed")
        }
    }

}
    
fun main(args: Array<String>) {
    runApplication<GalleryApplication>(*args)
}
