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
    @Query(value = "SELECT * FROM media_folder " +
            "group by media_folder.id order by (SELECT count(folder_id) FROM media_file where folder_id = media_folder.id) asc, media_folder.id",
        countQuery = "SELECT count(*) FROM media_folder",
        nativeQuery = true)
    fun findAllMediaFolderByFileCountAsc(pageable: Pageable): Page<MediaFolder>

    @Query(value = "SELECT * FROM media_folder " +
            "group by media_folder.id order by (SELECT count(folder_id) FROM media_file where folder_id = media_folder.id) desc, media_folder.id",
        countQuery = "SELECT count(*) FROM media_folder",
        nativeQuery = true)
    fun findAllMediaFolderByFileCountDesc(pageable: Pageable): Page<MediaFolder>

    /**
     * Query is the same as the regular one with an added WHERE clause that returns only the folders containing the query.
     */
    @Query(
        value = "select f from MediaFolder f join f.files fi where UPPER(f.name) like CONCAT('%',UPPER(:name),'%') group by f Order By COUNT(fi) asc",
        countQuery = "select count(f) from MediaFolder f"
    )
    fun findByNameContainingAndFileCountAsc(name: String, pageable: Pageable): Page<MediaFolder>

    @Query(
        value = "select f from MediaFolder f join f.files fi where UPPER(f.name) like CONCAT('%',UPPER(:name),'%') group by f Order By COUNT(fi) desc",
        countQuery = "select count(f) from MediaFolder f"
    )
    fun findByNameContainingAndFileCountDesc(name: String, pageable: Pageable): Page<MediaFolder>
}
