package com.guillermonegrete.gallery.repository

import com.guillermonegrete.gallery.data.MediaFile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MediaFileRepository : JpaRepository<MediaFile, Long>