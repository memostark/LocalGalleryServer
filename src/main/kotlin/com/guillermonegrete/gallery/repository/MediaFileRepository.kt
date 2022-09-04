package com.guillermonegrete.gallery.repository

import com.guillermonegrete.gallery.data.MediaFile
import com.guillermonegrete.gallery.data.MediaFolder
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MediaFileRepository : JpaRepository<MediaFile, Long>{
    fun findAllByFolder(folder: MediaFolder, pageable: Pageable): Page<MediaFile>

    fun findFilesByTagsId(tagId: Long): List<MediaFile>
}
