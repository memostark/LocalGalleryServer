package com.guillermonegrete.gallery.data.files

import com.guillermonegrete.gallery.data.MediaFile
import javax.persistence.DiscriminatorValue
import javax.persistence.Entity

@Entity
@DiscriminatorValue("1")
open class ImageEntity : MediaFile()