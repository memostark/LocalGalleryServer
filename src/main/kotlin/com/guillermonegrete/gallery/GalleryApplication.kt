package com.guillermonegrete.gallery

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

                    val files = folderRepository.getMedia("$basePath/$folder")

                    for(file in files){
                        file.folder = mediaFolder
                        try {
                            repository.save(file)
                        } catch (e: DataIntegrityViolationException){
                            println("Duplicate file in database ${file.filename}")
                        }
                    }
                }else{
                    val databaseFileNames = mediaFolder.files.map { it.filename }.toSet()

                    val filenames = folderRepository.getImageNames("$basePath/$folder")

                    for(filename in filenames){
                        if(databaseFileNames.contains(filename)) continue

                        val imageFile = folderRepository.getMediaInfo("$basePath/$folder/$filename") ?: continue
                        imageFile.folder = mediaFolder
                        try {
                            repository.save(imageFile)
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
