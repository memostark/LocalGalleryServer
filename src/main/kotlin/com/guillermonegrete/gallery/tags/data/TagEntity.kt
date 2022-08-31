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
    @ManyToMany
    @JoinTable(
        name = "media_tags",
        joinColumns = [JoinColumn(name = "tag_id", referencedColumnName = "id")],
        inverseJoinColumns = [JoinColumn(name = "media_id", referencedColumnName = "id")]
    )
    open val files: Set<MediaFile> = setOf()
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    open val id: Long = 0
}
