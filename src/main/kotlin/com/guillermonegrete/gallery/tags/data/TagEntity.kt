package com.guillermonegrete.gallery.tags.data

import com.guillermonegrete.gallery.data.MediaFile
import java.time.Instant
import javax.persistence.*

/**
 * Represent a tag in the database.
 */
@Entity
open class TagEntity {

    @Column(name = "creation_date", nullable = false)
    open val creationDate: Instant = Instant.now()
    @Column(nullable = false)
    open val name: String = ""
    /**
     * A tag can be applied to many files.
     */
    @ManyToMany(targetEntity = MediaFile::class)
    open var files: Set<MediaFile> = setOf()
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    open val id: Long = 0
}
