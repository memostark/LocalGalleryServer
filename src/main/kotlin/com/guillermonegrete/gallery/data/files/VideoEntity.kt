package com.guillermonegrete.gallery.data.files

import com.guillermonegrete.gallery.data.MediaFile
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import java.time.Instant

@Entity
@DiscriminatorValue("2")
open class VideoEntity(
    filename: String = "",
    width: Int = 0,
    height: Int = 0,
    creationDate: Instant = Instant.now(),
    lastModified: Instant = Instant.now(),
    open val duration: Int = 0
) : MediaFile(filename, width, height, creationDate, lastModified)