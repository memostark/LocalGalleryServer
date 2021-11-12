package com.guillermonegrete.gallery.repository

import com.guillermonegrete.gallery.data.MediaFolder
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface MediaFolderRepository: JpaRepository<MediaFolder, Long>{

    fun findByName(name: String): MediaFolder?

    fun findByNameContaining(name: String, pageable: Pageable): Page<MediaFolder>
}
