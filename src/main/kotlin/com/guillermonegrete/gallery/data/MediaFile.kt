package com.guillermonegrete.gallery.data

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.guillermonegrete.gallery.tags.data.TagFile
import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorColumn
import jakarta.persistence.DiscriminatorType
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Inheritance
import jakarta.persistence.InheritanceType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToMany
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.Instant


@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="file_type",
    discriminatorType = DiscriminatorType.INTEGER)
@Table(uniqueConstraints=[
    UniqueConstraint(columnNames = ["filename", "folder_id"])
])
open class MediaFile(
    open val filename: String = "",
    @Column(nullable = false)
    open val width: Int = 0,
    @Column(nullable = false)
    open val height: Int = 0,
    @Column(name = "creation_date", nullable = false)
    open val creationDate: Instant = Instant.now(),
    @Column(name = "last_modified", nullable = false)
    open val lastModified: Instant = Instant.now(),
    @ManyToMany(targetEntity = TagFile::class, mappedBy = "files")
    open val tags: MutableSet<TagFile> = mutableSetOf(),
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "folder_id")
    @JsonIgnoreProperties("files")
    open var folder: MediaFolder = MediaFolder(),
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    open val id: Long = 0
){
    override fun toString(): String {
        return "{name: $filename, width: $width, height: $height, folder: ${folder.name}, creation_date: $creationDate, lastModified: $lastModified}"
    }

    /**
     * Returns true if the tag wasn't already applied, false otherwise.
     */
    fun addTag(tag: TagFile): Boolean {
        tag.files.add(this)
        return tags.add(tag)
    }

    fun removeTag(tagId: Long) {
        val tag = tags.firstOrNull { it.id == tagId } ?: return
        tags.remove(tag)
        tag.files.remove(this)
    }
}
