package com.guillermonegrete.gallery.services

import com.guillermonegrete.gallery.FoldersRepository
import com.guillermonegrete.gallery.data.MediaFile
import com.guillermonegrete.gallery.data.MediaFolder
import com.guillermonegrete.gallery.repository.MediaFileRepository
import com.guillermonegrete.gallery.repository.MediaFolderRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Service
class FolderProcessingService{

    @Autowired private lateinit var repository: MediaFileRepository
    @Autowired private lateinit var mediaFolderRepo: MediaFolderRepository
    @Autowired private lateinit var folderRepository: FoldersRepository

    @Transactional(propagation=Propagation.REQUIRED, readOnly=true, noRollbackFor=[Exception::class])
    fun processFolder(folder: String, basePath: String){
        var mediaFolder = mediaFolderRepo.findByName(folder)
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
}