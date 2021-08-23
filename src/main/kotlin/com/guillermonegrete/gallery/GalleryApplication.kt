package com.guillermonegrete.gallery

import com.guillermonegrete.gallery.data.MediaFile
import com.guillermonegrete.gallery.data.MediaFolder
import com.guillermonegrete.gallery.repository.MediaFileRepository
import com.guillermonegrete.gallery.repository.MediaFolderRepository
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
    fun checkFolders(repository: MediaFileRepository, mediaFolderRepo: MediaFolderRepository, folderRepository: FoldersRepository): CommandLineRunner {
        return CommandLineRunner {
            println("On command line runner")
            // Get all folders
            val folders = folderRepository.getFolders(basePath)
            // For each folder, extract info: filename, width and height

            for(folder in folders){
                var mediaFolder = mediaFolderRepo.findByName(folder)
                if(mediaFolder == null){
                    mediaFolder = MediaFolder(folder)
                    mediaFolderRepo.save(mediaFolder)
                    println("Found new folder: $folder")
                }

                val files = folderRepository.getImages("$basePath/$folder")

                val databaseFileNames = mediaFolder.files.map { it.filename }.toSet()
                for(file in files){
                    if(databaseFileNames.contains(file.url)) continue
                    val mediaFile = MediaFile(file.url, file.width, file.height, mediaFolder)
                    try {
                        repository.save(mediaFile)
                    } catch (e: DataIntegrityViolationException){
                        // No need to print error, logs are already printed
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
