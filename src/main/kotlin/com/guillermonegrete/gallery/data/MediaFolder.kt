package com.guillermonegrete.gallery.data

import javax.persistence.*

@Entity
data class MediaFolder(
    @Column(unique = true)
    val name: String = "",
    @OneToMany(targetEntity=MediaFile::class, mappedBy = "folder", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var files: List<MediaFile> = emptyList(),
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long = 0,
)