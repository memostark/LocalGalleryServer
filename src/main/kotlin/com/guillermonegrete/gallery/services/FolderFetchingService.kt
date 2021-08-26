package com.guillermonegrete.gallery.services

import com.guillermonegrete.gallery.data.MediaFolder
import com.guillermonegrete.gallery.repository.MediaFolderRepository
import org.hibernate.Hibernate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FolderFetchingService{

    @Autowired private lateinit var mediaFolderRepo: MediaFolderRepository

    @Transactional
    fun getMediaFolder(folder: String): MediaFolder?{
        val mediaFolder = mediaFolderRepo.findByName(folder) ?: return null
        Hibernate.initialize(mediaFolder.files)
        return mediaFolder
    }
}