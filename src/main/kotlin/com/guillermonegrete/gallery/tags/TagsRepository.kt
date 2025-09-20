package com.guillermonegrete.gallery.tags

import com.guillermonegrete.gallery.tags.data.TagEntity
import com.guillermonegrete.gallery.tags.data.TagFile
import com.guillermonegrete.gallery.tags.data.TagFileDto
import com.guillermonegrete.gallery.tags.data.TagFolder
import com.guillermonegrete.gallery.tags.data.TagFolderDto
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface TagsRepository: JpaRepository<TagEntity, Long>

interface FileTagsRepository: JpaRepository<TagFile, Long>{

    fun findByName(name: String): TagFile?

    fun findByIdIn(ids: List<Long>): List<TagFile>

    @Query("SELECT new com.guillermonegrete.gallery.tags.data.TagFileDto(t.name, COUNT(t.id), t.creationDate, t.id) FROM TagEntity AS t " +
            "JOIN t.files AS f WHERE :folderId = f.folder.id GROUP BY t.id")
    fun getTagsWithFilesByFolder(folderId: Long): Set<TagFileDto>
}

interface FolderTagsRepository: JpaRepository<TagFolder, Long>{

    fun findByName(name: String): TagFolder?

    fun findByIdIn(ids: List<Long>): List<TagFolder>

    @Query("SELECT new com.guillermonegrete.gallery.tags.data.TagFolderDto(t.name, SIZE(t.folders), t.creationDate, t.id) FROM TagFolder AS t")
    fun getFolderTags(): Set<TagFolderDto>

    @Query("SELECT new com.guillermonegrete.gallery.tags.data.TagFolderDto(t.name, COUNT(t.id), t.creationDate, t.id) FROM TagEntity AS t " +
            "JOIN t.folders AS f WHERE :folderId = f.id GROUP BY t.id")
    fun getFolderTags(folderId: Long): Set<TagFolderDto>
}
