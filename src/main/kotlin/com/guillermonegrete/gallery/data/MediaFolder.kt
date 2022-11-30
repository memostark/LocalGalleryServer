package com.guillermonegrete.gallery.data

import javax.persistence.*

@Entity
data class MediaFolder(
    @Column(unique = true)
    val name: String = "",
    @OneToMany(targetEntity=MediaFile::class, mappedBy = "folder", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var files: List<MediaFile> = emptyList(),
    @OneToOne
    @MapsId
    @JoinColumn(name = "cover_file_id")
    var coverFile: MediaFile? = null,
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long = 0,
) {
    override fun toString(): String {
        return "{name: $name, id: $id}"
    }
}