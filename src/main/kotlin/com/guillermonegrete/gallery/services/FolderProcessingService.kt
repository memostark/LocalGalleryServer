package com.guillermonegrete.gallery.services

import com.guillermonegrete.gallery.FoldersRepository
import com.guillermonegrete.gallery.data.MediaFolder
import com.guillermonegrete.gallery.repository.MediaFileRepository
import com.guillermonegrete.gallery.repository.MediaFolderRepository
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service

/**
 * Service that saves the media files in the database of the folders in the folder path.
 */
@Service
class FolderProcessingService(
    private val folderRepository: FoldersRepository,
    private val fileEntityRepo: MediaFileRepository,
    private val folderEntityRepo: MediaFolderRepository,
    private val service: FolderFetchingService
) {

    fun processFolder(basePath: String){

        val folders = folderRepository.getFolders(basePath)
        for(folder in folders){
            var mediaFolder = service.getMediaFolder(folder)

            if(mediaFolder == null){
                mediaFolder = MediaFolder(folder)
                val savedFolder = folderEntityRepo.save(mediaFolder)
                println("Found new folder: $folder")

                val files = folderRepository.getMedia("$basePath/$folder")

                for(file in files){
                    file.folder = savedFolder
                    try {
                        fileEntityRepo.save(file)
                        println("Saved: $file")
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
                        fileEntityRepo.save(imageFile)
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
