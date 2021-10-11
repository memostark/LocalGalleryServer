package com.guillermonegrete.gallery.data.files

import com.guillermonegrete.gallery.data.MediaFile
import javax.persistence.DiscriminatorValue
import javax.persistence.Entity

@Entity
@DiscriminatorValue("2")
open class VideoEntity(val duration: Int = 0) : MediaFile()