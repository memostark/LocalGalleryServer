package com.guillermonegrete.gallery.repository

import com.guillermonegrete.gallery.data.MediaFile
import com.guillermonegrete.gallery.data.MediaFolder
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface MediaFolderRepository: JpaRepository<MediaFolder, Long>{

    fun findByName(name: String): MediaFolder?

    /**
     * Returns folders whose name contains the given string
     */
    fun findByNameContaining(name: String, pageable: Pageable): Page<MediaFolder>

    /**
     * Returns all media folders by the count of their children files.
     */
    @Query(value = folderDtoSelect + folderAscOrder,
        countQuery = folderCountQuery,
        nativeQuery = true)
    fun findAllMediaFolderByFileCountAsc(pageable: Pageable): Page<FolderDto>

    @Query(value = folderDtoSelect + folderDescOrder,
        countQuery = folderCountQuery,
        nativeQuery = true)
    fun findAllMediaFolderByFileCountDesc(pageable: Pageable): Page<FolderDto>

    /**
     * Query is the same as the regular one with an added WHERE clause that returns only the folders containing the query.
     */
    @Query(
        value = folderDtoSelect + folderNameContains + folderAscOrder,
        countQuery = folderCountQuery,
        nativeQuery = true
    )
    fun findByNameContainingAndFileCountAsc(name: String, pageable: Pageable): Page<FolderDto>

    @Query(
        value = folderDtoSelect + folderNameContains + folderDescOrder ,
        countQuery = folderCountQuery,
        nativeQuery = true
    )
    fun findByNameContainingAndFileCountDesc(name: String, pageable: Pageable): Page<FolderDto>

    fun findByIdIn(ids: List<Long>): List<MediaFolder>
}

private const val folderDtoSelect = "SELECT name, " +
        "IFNULL((SELECT filename FROM media_file where media_file.id = cover_file_id), (SELECT filename FROM media_file where media_file.folder_id = media_folder.id LIMIT 1)) as coverUrl, " + //
        "(SELECT count(folder_id) FROM media_file where folder_id = media_folder.id) as count, id FROM media_folder "

private const val folderAscOrder = "group by media_folder.id order by count asc, media_folder.id"

private const val folderDescOrder = "group by media_folder.id order by count desc, media_folder.id"

private const val folderNameContains = "where UPPER(media_folder.name) like CONCAT('%',UPPER(:name),'%') "

private const val folderCountQuery = "SELECT count(*) FROM media_folder"

interface FolderDto {
    val name:String
    val coverUrl: String?
    val count: Int
    val id: Long
}
