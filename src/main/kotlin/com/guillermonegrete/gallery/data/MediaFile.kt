package com.guillermonegrete.gallery.data

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
    @ManyToMany(targetEntity = TagEntity::class)
    open var tags: Set<MediaFile> = setOf(),
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "folder_id")
    open var folder: MediaFolder = MediaFolder(),
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    open val id: Long = 0
){
    override fun toString(): String {
        return "{name: $filename, width: $width, height: $height, folder: ${folder.name}, creation_date: $creationDate, lastModified: $lastModified}"
    }
}