package com.guillermonegrete.gallery.tags.data

import com.fasterxml.jackson.annotation.JsonIgnore
import com.guillermonegrete.gallery.data.MediaFolder
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import java.time.Instant
import java.time.temporal.ChronoUnit

@Entity
@DiscriminatorValue("2")
open class TagFolder(
    name: String = "",
    creationDate: Instant = Instant.now().truncatedTo(ChronoUnit.SECONDS),
    @ManyToMany
    @JoinTable(
        name = "folder_tags",
        joinColumns = [JoinColumn(name = "tag_id", referencedColumnName = "id")],
        inverseJoinColumns = [JoinColumn(name = "folder_id", referencedColumnName = "id")]
    )
    @JsonIgnore
    open val folders: MutableSet<MediaFolder> = mutableSetOf(),
    id: Long = 0,
): TagEntity(name, creationDate, id)
