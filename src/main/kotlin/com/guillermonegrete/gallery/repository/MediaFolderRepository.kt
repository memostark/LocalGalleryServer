package com.guillermonegrete.gallery.repository

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

    fun findFoldersByTagsId(tagId: Long, pageable: Pageable): Page<MediaFolder>

    fun findFoldersByTagIds(tagIds: List<Long>, pageable: Pageable)
        = findFoldersByTagsIds(tagIds, tagIds.size, pageable)

    @Query("""select folder from MediaFolder folder 
        where :numberOfTags = (select count(tag.id) from MediaFolder folder2 
                                inner join folder2.tags tag 
                                where folder2.id = folder.id and tag.id in (:tagIds))""")
    fun findFoldersByTagsIds(tagIds: List<Long>, numberOfTags: Int, pageable: Pageable): Page<MediaFolder>

    @Query("""select folder from MediaFolder folder 
        where :numberOfTags = (select count(tag.id) from MediaFolder folder2 
                                inner join folder2.tags tag 
                                where folder2.id = folder.id and tag.id in (:tagIds)) and UPPER(folder.name) like CONCAT('%',UPPER(:name),'%')""")
    fun findFoldersByTagsIdsAndContaining(tagIds: List<Long>, numberOfTags: Int, name: String, pageable: Pageable): Page<MediaFolder>

    @Query(value = folderDtoSelect + folderContainsTags + folderAscOrder,
        countQuery = folderCountQuery,
        nativeQuery = true)
    fun findFoldersByFileCountAndTagsAsc(tagIds: List<Long>, numberOfTags: Int, pageable: Pageable): Page<FolderDto>

    @Query(value = folderDtoSelect + folderContainsTags + folderDescOrder,
        countQuery = folderCountQuery,
        nativeQuery = true)
    fun findFoldersByFileCountAndTagsDesc(tagIds: List<Long>, numberOfTags: Int, pageable: Pageable): Page<FolderDto>

    @Query(value = folderDtoSelect + folderContainsTags + folderNameContainsAnd + folderAscOrder,
        countQuery = folderCountQuery,
        nativeQuery = true)
    fun findFoldersByFileCountAndTagsAndContainingAsc(tagIds: List<Long>, numberOfTags: Int, name: String, pageable: Pageable): Page<FolderDto>

    @Query(value = folderDtoSelect + folderContainsTags + folderNameContainsAnd + folderDescOrder,
        countQuery = folderCountQuery,
        nativeQuery = true)
    fun findFoldersByFileCountAndTagsAndContainingDesc(tagIds: List<Long>, numberOfTags: Int, name: String, pageable: Pageable): Page<FolderDto>
}

private const val folderDtoSelect = "SELECT name, " +
        "IFNULL((SELECT filename FROM media_file where media_file.id = cover_file_id), (SELECT filename FROM media_file where media_file.folder_id = media_folder.id LIMIT 1)) as coverUrl, " + //
        "(SELECT count(folder_id) FROM media_file where folder_id = media_folder.id) as count, id FROM media_folder "

private const val folderAscOrder = "group by media_folder.id order by count asc, media_folder.id"

private const val folderDescOrder = "group by media_folder.id order by count desc, media_folder.id"

private const val folderNameContains = "where UPPER(media_folder.name) like CONCAT('%',UPPER(:name),'%') "

private const val folderNameContainsAnd = "AND UPPER(media_folder.name) like CONCAT('%',UPPER(:name),'%') "

private const val folderContainsTags = """
    where :numberOfTags = (select count(tag_entity.id) from tag_entity
    join folder_tags ON tag_entity.id = folder_tags.tag_id
    where media_folder.id = folder_tags.folder_id and folder_tags.tag_id in (:tagIds)) """

private const val folderCountQuery = "SELECT count(*) FROM media_folder"

interface FolderDto {
    val name:String
    val coverUrl: String?
    val count: Int
    val id: Long
}
