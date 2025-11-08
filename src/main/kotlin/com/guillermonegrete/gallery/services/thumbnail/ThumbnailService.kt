package com.guillermonegrete.gallery.services.thumbnail

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import javax.imageio.ImageIO

@Service
class ThumbnailService(@Value("\${final.path}") private val basePath: String) {

    fun generateThumbnail(folder: String, filename: String, type: ThumbnailType): ByteArray {

        // Generate scaled image
        val baseFolder = File(basePath, folder)
        val originalFile = File(baseFolder, filename)
        val originalImage = ImageIO.read(originalFile)
        val newHeight = (type.size * (originalImage.height.toFloat() / originalImage.width)).toInt()
        val buffImg = BufferedImage(type.size, newHeight, BufferedImage.TYPE_INT_RGB)
        buffImg.createGraphics().drawImage(originalImage.getScaledInstance(type.size, newHeight, BufferedImage.SCALE_SMOOTH), 0, 0, null)

        // Write to file
        val thumbnailsFolder = File(baseFolder, THUMBNAILS_FOLDER)
        if (!thumbnailsFolder.exists()) thumbnailsFolder.mkdir()
        val newFilename = originalFile.nameWithoutExtension + "-" + type.name.lowercase() + "." + originalFile.extension
        val newFile = File(thumbnailsFolder, newFilename)
        ImageIO.write(buffImg, THUMBNAIL_EXT, newFile)

        // Return byte data
        val baos = ByteArrayOutputStream()
        ImageIO.write(buffImg, THUMBNAIL_EXT, baos)
        return baos.toByteArray()
    }

    companion object {
        const val THUMBNAILS_FOLDER = ".thumbnails"
        const val THUMBNAIL_EXT = "webp"
    }

    enum class ThumbnailType(val size: Int) {
        Small(100),
        Medium(200),
        Large(300)
    }
}
