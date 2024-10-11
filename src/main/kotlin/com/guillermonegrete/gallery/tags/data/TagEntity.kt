package com.guillermonegrete.gallery.tags.data

import com.fasterxml.jackson.annotation.JsonIgnore
import com.guillermonegrete.gallery.data.MediaFile
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Represent a tag in the database.
 */
@Entity
open class TagEntity(
    @Column(nullable = false, unique = true)
    open val name: String = "",
    @Column(name = "creation_date", nullable = false)
    open val creationDate: Instant = Instant.now().truncatedTo(ChronoUnit.SECONDS), // by default the db saves in seconds, truncate to avoid having different milliseconds
    /**
     * A tag can be applied to many files.
     */
    @ManyToMany
    @JoinTable(
        name = "media_tags",
        joinColumns = [JoinColumn(name = "tag_id", referencedColumnName = "id")],
        inverseJoinColumns = [JoinColumn(name = "media_id", referencedColumnName = "id")]
    )
    @JsonIgnore
    open val files: MutableSet<MediaFile> = mutableSetOf(),
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open val id: Long = 0,
){


    override fun toString(): String {
        return "{id: $id, name: $name, date: $creationDate}"
    }
}

fun TagEntity.toDto() = TagDto(name, files.size.toLong(), creationDate, id)
