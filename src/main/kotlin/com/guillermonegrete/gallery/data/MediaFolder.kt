package com.guillermonegrete.gallery.data

import com.guillermonegrete.gallery.tags.data.TagFile
import com.guillermonegrete.gallery.tags.data.TagFolder
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToMany
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne

@Entity
data class MediaFolder(
    @Column(unique = true)
    val name: String = "",
    @OneToMany(targetEntity=MediaFile::class, mappedBy = "folder", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var files: List<MediaFile> = emptyList(),
    @OneToOne
    @JoinColumn(name = "cover_file_id")
    var coverFile: MediaFile? = null,
    @ManyToMany(targetEntity = TagFolder::class, mappedBy = "folders")
    val tags: MutableSet<TagFolder> = mutableSetOf(),
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long = 0,
) {
    override fun toString(): String {
        return "{name: $name, id: $id}"
    }
}