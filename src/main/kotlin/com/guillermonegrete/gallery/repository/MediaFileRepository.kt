package com.guillermonegrete.gallery.repository

import com.guillermonegrete.gallery.data.MediaFile
import com.guillermonegrete.gallery.data.MediaFolder
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface MediaFileRepository : JpaRepository<MediaFile, Long>{
    fun findAllByFolder(folder: MediaFolder, pageable: Pageable): Page<MediaFile>

    fun findFilesByTagsId(tagId: Long, pageable: Pageable): Page<MediaFile>

    /**
     * Gets a page of all the files that have all the specified tags applied.
     */
    fun findFilesByTagsIds(tagIds: List<Long>, pageable: Pageable): Page<MediaFile>
       = findFilesByTagsIds(tagIds, tagIds.size, pageable)

    @Query("""select file from MediaFile file 
        where :numberOfTags = (select count(tag.id) from MediaFile file2 
                                inner join file2.tags tag 
                                where file2.id = file.id and tag.id in (:tagIds))""")
    fun findFilesByTagsIds(tagIds: List<Long>, numberOfTags: Int, pageable: Pageable): Page<MediaFile>

    /**
     * Gets a page of all the files that have all the specified tags applied for the specified folder.
     */
    fun findFilesByTagsIdsAndFolderId(tagIds: List<Long>, folderId: Long, pageable: Pageable): Page<MediaFile>
            = findFilesByTagsIdsAndFolderId(tagIds, tagIds.size, folderId,  pageable)

    @Query("""select file from MediaFile file 
        where file.folder.id = :folderId AND 
        :numberOfTags = (select count(tag.id) from MediaFile file2 
                        inner join file2.tags tag 
                        where file2.id = file.id and tag.id in (:tagIds))""")
    fun findFilesByTagsIdsAndFolderId(tagIds: List<Long>, numberOfTags: Int, folderId: Long, pageable: Pageable): Page<MediaFile>

    fun findFilesByTagsIdAndFolderId(tagId: Long, folderId: Long, pageable: Pageable): Page<MediaFile>

    fun findByIdIn(ids: List<Long>): List<MediaFile>
}
