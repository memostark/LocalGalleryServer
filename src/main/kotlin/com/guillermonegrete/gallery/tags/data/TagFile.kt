package com.guillermonegrete.gallery.tags.data

import com.fasterxml.jackson.annotation.JsonIgnore
import com.guillermonegrete.gallery.data.MediaFile
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import java.time.Instant
import java.time.temporal.ChronoUnit

@Entity
@DiscriminatorValue("1")
open class TagFile(
    name: String = "",
    creationDate: Instant = Instant.now().truncatedTo(ChronoUnit.SECONDS),
    @ManyToMany
    @JoinTable(
        name = "media_tags",
        joinColumns = [JoinColumn(name = "tag_id", referencedColumnName = "id")],
        inverseJoinColumns = [JoinColumn(name = "media_id", referencedColumnName = "id")]
    )
    @JsonIgnore
    open val files: MutableSet<MediaFile> = mutableSetOf(),
    id: Long = 0
): TagEntity(name, creationDate, id)

fun TagFile.toDto() = TagDto(name, files.size.toLong(), creationDate, id)