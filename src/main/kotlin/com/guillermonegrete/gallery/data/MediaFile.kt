package com.guillermonegrete.gallery.data

import javax.persistence.*


@Entity
data class MediaFile(
    @Column(unique=true)
    val filename: String,
    val width: Int,
    val height: Int,
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long = 0
)