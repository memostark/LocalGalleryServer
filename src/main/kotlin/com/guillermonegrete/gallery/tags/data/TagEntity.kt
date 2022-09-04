package com.guillermonegrete.gallery.tags.data

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.guillermonegrete.gallery.data.MediaFile
import java.time.Instant
import javax.persistence.*

/**
 * Represent a tag in the database.
 */
@Entity
open class TagEntity(
    @Column(nullable = false, unique = true)
    open val name: String = "",
    @Column(name = "creation_date", nullable = false)
    open val creationDate: Instant = Instant.now(),
    /**
     * A tag can be applied to many files.
     */
    @ManyToMany
    @JoinTable(
        name = "media_tags",
        joinColumns = [JoinColumn(name = "tag_id", referencedColumnName = "id")],
        inverseJoinColumns = [JoinColumn(name = "media_id", referencedColumnName = "id")]
    )
    @JsonIgnoreProperties("tags")
    open val files: MutableList<MediaFile> = mutableListOf(),
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    open val id: Long = 0,
){


    override fun toString(): String {
        return "id: $id, name: $name"
    }

    override fun hashCode(): Int {
        return id.toInt()
    }
}
