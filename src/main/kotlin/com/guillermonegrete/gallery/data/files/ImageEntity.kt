package com.guillermonegrete.gallery.data.files

import com.guillermonegrete.gallery.data.MediaFile
import java.time.Instant
import javax.persistence.DiscriminatorValue
import javax.persistence.Entity

@Entity
@DiscriminatorValue("1")
open class ImageEntity(
    filename: String = "",
    width: Int = 0,
    height: Int = 0,
    creationDate: Instant = Instant.now(),
    lastModified: Instant = Instant.now(),
) : MediaFile(filename, width, height, creationDate, lastModified)