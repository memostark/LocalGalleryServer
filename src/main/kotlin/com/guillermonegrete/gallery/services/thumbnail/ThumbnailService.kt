package com.guillermonegrete.gallery.services.thumbnail

import net.bramp.ffmpeg.FFmpegExecutor
import net.bramp.ffmpeg.builder.FFmpegBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import javax.imageio.ImageIO

@Service
class ThumbnailService(@param:Value("\${final.path}") private val basePath: String) {

    fun generateThumbnail(folder: String, filename: String, type: ThumbnailType): ByteArray {

        val baseFolder = File(basePath, folder)
        val originalFile = File(baseFolder, filename)

        // Check if thumbnail already exists
        val thumbnailsFolder = File(baseFolder, THUMBNAILS_FOLDER)
        if (!thumbnailsFolder.exists()) thumbnailsFolder.mkdir()
        val newFilename = originalFile.nameWithoutExtension + "-" + type.name.lowercase() + "." + THUMBNAIL_EXT
        val newFile = File(thumbnailsFolder, newFilename)
        if (newFile.exists()) {
            val thumbnail = ImageIO.read(newFile)
            val baos = ByteArrayOutputStream()
            ImageIO.write(thumbnail, THUMBNAIL_EXT, baos)
            return baos.toByteArray()
        }

        // Otherwise generate new thumbnail
        val originalImage = ImageIO.read(originalFile)
        val img = if (originalImage != null) {
            val newHeight = (type.size * (originalImage.height.toFloat() / originalImage.width)).toInt()
            val buffImg = BufferedImage(type.size, newHeight, BufferedImage.TYPE_INT_RGB)
            buffImg.createGraphics().drawImage(originalImage.getScaledInstance(type.size, newHeight, BufferedImage.SCALE_SMOOTH), 0, 0, null)

            ImageIO.write(buffImg, THUMBNAIL_EXT, newFile)
            buffImg
        } else {
            val builder = FFmpegBuilder()
                .setInput(originalFile.absolutePath)
                .addOutput(newFile.absolutePath)
                .setVideoCodec("libwebp")
                .setFrames(1)
                .setVideoFilter("scale=${type.size}:-1")
                .done()
            val executor = FFmpegExecutor()
            executor.createJob(builder).run()

            ImageIO.read(newFile)
        }

        // Return byte data
        val baos = ByteArrayOutputStream()
        ImageIO.write(img, THUMBNAIL_EXT, baos)
        return baos.toByteArray()
    }

    companion object {
        const val THUMBNAILS_FOLDER = ".thumbnails"
        const val THUMBNAIL_EXT = "webp"
    }
}

enum class ThumbnailType(val size: Int) {
    Small(100),
    Medium(200),
    Large(300);

    companion object {
        fun getThumbnailType(size: String) =
            ThumbnailType.entries.find { it.name.compareTo(size, true) == 0 }
    }
}
