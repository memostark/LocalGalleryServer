package com.guillermonegrete.gallery.data

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.guillermonegrete.gallery.tags.data.TagEntity
import java.time.Instant
import javax.persistence.*


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
    @ManyToMany(targetEntity = TagEntity::class, mappedBy = "files")
    open val tags: MutableSet<TagEntity> = mutableSetOf(),
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "folder_id")
    @JsonIgnoreProperties("files")
    open var folder: MediaFolder = MediaFolder(),
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    open val id: Long = 0
){
    override fun toString(): String {
        return "{name: $filename, width: $width, height: $height, folder: ${folder.name}, creation_date: $creationDate, lastModified: $lastModified}"
    }

    /**
     * Returns true if the tag wasn't already applied, false otherwise.
     */
    fun addTag(tag: TagEntity): Boolean {
        tag.files.add(this)
        return tags.add(tag)
    }

    fun removeTag(tagId: Long) {
        val tag = tags.firstOrNull { it.id == tagId } ?: return
        tags.remove(tag)
        tag.files.remove(this)
    }
}
