package com.guillermonegrete.gallery

import com.guillermonegrete.gallery.data.MediaFile
import com.guillermonegrete.gallery.data.files.ImageEntity
import com.guillermonegrete.gallery.data.files.VideoEntity
import com.guillermonegrete.gallery.services.GetFileInfoService
import org.springframework.stereotype.Component
import java.io.File
import java.io.IOException
import java.nio.file.Paths
import java.time.Instant


@Component
class DefaultFolderRepository(private val infoService: GetFileInfoService): FoldersRepository {

    private val supportedVideo = setOf("mp4", "webm")

    override fun getFolders(path: String): List<String> {
        return File(path).listFiles { file -> file.isDirectory }?.map { it.name } ?: emptyList()
    }

    override fun getImageNames(folder: String): Set<String> {
        return File(folder).listFiles()?.map { it.name }?.toSet() ?: emptySet()
    }


    override fun getMediaInfo(path: String): MediaFile? {
        val file = File(path)
       return getMediaFile(file)
    }

    override fun getMedia(folder: String): List<MediaFile> {
        return File(folder).listFiles()?.mapNotNull { file -> getMediaFile(file) } ?: emptyList()
    }

    private fun getSuffix(imgFile: File): String?{
        val pos = imgFile.name.lastIndexOf(".")
        if (pos == -1) {
            println("No extension for file: ${imgFile.absolutePath}")
            return null
        }
        return imgFile.name.substring(pos + 1)
    }

    private fun getMediaFile(file: File): MediaFile? {
        val suffix = getSuffix(file) ?: return null

        // First handle the supported video
        if(suffix in supportedVideo) {
            val videoInfo = infoService.getVideoDimensions(file.absolutePath) ?: return null
            val creationDate = infoService.getCreationDate(Paths.get(file.absolutePath)) ?: Instant.now()
            return VideoEntity(file.name, videoInfo.width, videoInfo.height, creationDate, duration = videoInfo.duration)
        }

        // Otherwise assume it's an image
        val imageInfo = getImageInfo(file)
        return imageInfo
    }

    /**
     * Gets image dimensions for given file
     * @param imgFile image file
     * @return dimensions of image
     */
    @Throws(IOException::class)
    private fun getImageInfo(imgFile: File): ImageEntity? {
        val creationDate = infoService.getCreationDate(Paths.get(imgFile.absolutePath)) ?: Instant.now()
        val size = infoService.getImageSize(imgFile)

        return if(size != null) ImageEntity(imgFile.name, size.width, size.height, creationDate) else null
    }
}
