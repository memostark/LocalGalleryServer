package com.guillermonegrete.gallery.data

import javax.persistence.*


@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="file_type",
    discriminatorType = DiscriminatorType.INTEGER)
@Table(uniqueConstraints=[
    UniqueConstraint(columnNames = ["filename", "folder_id"])
])
open class MediaFile(
    val filename: String = "",
    @Column(nullable = false)
    val width: Int = 0,
    @Column(nullable = false)
    val height: Int = 0,
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "folder_id")
    var folder: MediaFolder = MediaFolder(),
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long = 0
){
    override fun toString(): String {
        return "{name: $filename, width: $width, height: $height, folder: ${folder.name}}"
    }
}