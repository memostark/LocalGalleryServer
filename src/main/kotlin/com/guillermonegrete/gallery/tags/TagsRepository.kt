package com.guillermonegrete.gallery.tags

import com.guillermonegrete.gallery.tags.data.TagDto
import com.guillermonegrete.gallery.tags.data.TagEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface TagsRepository: JpaRepository<TagEntity, Long>{

    fun findByName(name: String): TagEntity?

    fun findByIdIn(ids: List<Long>): List<TagEntity>

    @Query("SELECT new com.guillermonegrete.gallery.tags.data.TagDto(t.name, COUNT(t.id), t.creationDate, t.id) FROM TagEntity AS t " +
            "JOIN t.files AS f WHERE :folderId = f.folder.id GROUP BY t.id")
    fun getTagsWithFilesByFolder(folderId: Long): Set<TagDto>
}
