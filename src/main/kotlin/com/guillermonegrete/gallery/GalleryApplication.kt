package com.guillermonegrete.gallery

import com.guillermonegrete.gallery.data.MediaFile
import com.guillermonegrete.gallery.repository.MediaFileRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.dao.DataIntegrityViolationException


@SpringBootApplication
class GalleryApplication{
    @Value("\${base.path}")
    private lateinit var basePath: String

    @Bean
    fun demo(repository: MediaFileRepository, folderRepository: DefaultFolderRepository): CommandLineRunner {
        return CommandLineRunner {
            println("On command line runner")
            // Get all folders
            val folders = folderRepository.getFolders(basePath)
            // For each folder, extract info: filename, width and height

            for(folder in folders){
                val files = folderRepository.getImages("$basePath/$folder")
                for(file in files){
                    val mediaFile = MediaFile(file.url, file.width, file.height)
                    try {
                        repository.save(mediaFile)
                    } catch (e: DataIntegrityViolationException){

                    }
                }
                println("Processed $folder...")
            }
            println("All folders processed")
            // Store that info in the db
        }
    }

}
    
fun main(args: Array<String>) {
    runApplication<GalleryApplication>(*args)
}
