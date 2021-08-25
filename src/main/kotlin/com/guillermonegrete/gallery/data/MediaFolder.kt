package com.guillermonegrete.gallery.data

import javax.persistence.*

@Entity
data class MediaFolder(
    @Column(unique = true)
    val name: String = "",
    @OneToMany(mappedBy = "folder", cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    var files: List<MediaFile> = emptyList(),
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long = 0,
)