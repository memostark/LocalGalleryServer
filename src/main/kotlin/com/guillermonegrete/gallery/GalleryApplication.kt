package com.guillermonegrete.gallery

import com.guillermonegrete.gallery.data.MediaFile
import com.guillermonegrete.gallery.data.MediaFolder
import com.guillermonegrete.gallery.repository.MediaFileRepository
import com.guillermonegrete.gallery.repository.MediaFolderRepository
import com.guillermonegrete.gallery.services.FolderFetchingService
import org.springframework.beans.factory.annotation.Autowired
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

    @Value("\${checkfolder}")
    private var folderCheck: Boolean = false

    @Autowired lateinit var service: FolderFetchingService

    @Bean
    fun checkFolders(repository: MediaFileRepository, mediaFolderRepo: MediaFolderRepository, folderRepository: FoldersRepository): CommandLineRunner {
        return CommandLineRunner {
            println("Check folders: $folderCheck")
            if(!folderCheck) return@CommandLineRunner

            val folders = folderRepository.getFolders(basePath)
            for(folder in folders){
                var mediaFolder = service.getMediaFolder(folder)

                if(mediaFolder == null){
                    mediaFolder = MediaFolder(folder)
                    mediaFolderRepo.save(mediaFolder)
                    println("Found new folder: $folder")

                    val files = folderRepository.getImages("$basePath/$folder")

                    for(file in files){
                        val mediaFile = MediaFile(file.url, file.width, file.height, mediaFolder)
                        try {
                            repository.save(mediaFile)
                        } catch (e: DataIntegrityViolationException){
                            println("Duplicate file in database ${file.url}")
                        }
                    }
                }else{
                    val databaseFileNames = mediaFolder.files.map { it.filename }.toSet()

                    val filenames = folderRepository.getImageNames("$basePath/$folder")

                    for(filename in filenames){
                        if(databaseFileNames.contains(filename)) continue

                        val imageFile = folderRepository.getImageInfo("$basePath/$folder/$filename") ?: continue
                        val mediaFile = MediaFile(filename, imageFile.width, imageFile.height, mediaFolder)
                        try {
                            repository.save(mediaFile)
                        } catch (e: DataIntegrityViolationException){
                            println("Duplicate file in database $filename")
                        }
                    }
                }
                println("Processed $folder...")
            }
            println("All folders processed")
        }
    }

}
    
fun main(args: Array<String>) {
    runApplication<GalleryApplication>(*args)
}
