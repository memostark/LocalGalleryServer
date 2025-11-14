package com.guillermonegrete.gallery.thumbnails

import com.guillermonegrete.gallery.FileProvider
import net.bramp.ffmpeg.FFmpegExecutor
import net.bramp.ffmpeg.FFprobe
import net.bramp.ffmpeg.builder.FFmpegBuilder
import net.bramp.ffmpeg.probe.FFmpegStream
import org.springframework.stereotype.Service
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import javax.imageio.ImageIO

@Service
class ThumbnailService(
    private val ffprobe: FFprobe,
    private val fileProvider: FileProvider,
) {

    fun generateThumbnail(folder: String, filename: String, type: ThumbnailType): ByteArray {

        val baseFolder = fileProvider.createFromBase(folder)
        val originalFile = fileProvider.getFile(baseFolder, filename)

        // Check if thumbnail already exists
        val thumbnailsFolder = fileProvider.getFile(baseFolder, THUMBNAILS_FOLDER)
        if (!thumbnailsFolder.exists()) thumbnailsFolder.mkdir()
        val newFilename = originalFile.nameWithoutExtension + "-" + type.name.lowercase() + "." + THUMBNAIL_EXT
        val newFile = fileProvider.getFile(thumbnailsFolder, newFilename)
        if (newFile.exists()) {
            val thumbnail = ImageIO.read(newFile)
            val baos = ByteArrayOutputStream()
            ImageIO.write(thumbnail, THUMBNAIL_EXT, baos)
            return baos.toByteArray()
        }

        // Otherwise generate new thumbnail
        val originalImage = ImageIO.read(originalFile)
        val img = if (originalImage != null) {
            if (originalImage.width <= type.size) {
                originalImage
            } else {
                val newHeight = (type.size * (originalImage.height.toFloat() / originalImage.width)).toInt()
                val buffImg = BufferedImage(type.size, newHeight, BufferedImage.TYPE_INT_RGB)
                buffImg.createGraphics().drawImage(originalImage.getScaledInstance(type.size, newHeight, BufferedImage.SCALE_SMOOTH), 0, 0, null)

                ImageIO.write(buffImg, THUMBNAIL_EXT, newFile)
                buffImg
            }
        } else {
            generateVideoThumbnail(originalFile, newFile, type.size, thumbnailsFolder)
        }

        // Return byte data
        val baos = ByteArrayOutputStream()
        ImageIO.write(img, THUMBNAIL_EXT, baos)
        return baos.toByteArray()
    }

    private fun generateVideoThumbnail(
        originalFile: File,
        newFile: File,
        newSize: Int,
        thumbnailsFolder: File
    ): BufferedImage? {

        val stream = ffprobe.probe(originalFile.absolutePath)?.streams?.find { it.codec_type == FFmpegStream.CodecType.VIDEO }
        val originalIsBigger = stream != null && stream.width > newSize

        val defaultThumbnailFile = fileProvider.getFile(thumbnailsFolder,   originalFile.nameWithoutExtension + "-default." + THUMBNAIL_EXT)
        if (!originalIsBigger && defaultThumbnailFile.exists()) {
            return ImageIO.read(defaultThumbnailFile)
        }

        val targetFile = if (originalIsBigger) newFile else defaultThumbnailFile
        val builder = FFmpegBuilder()
            .setInput(originalFile.absolutePath)
            .addOutput(targetFile.absolutePath)
            .setVideoCodec("libwebp")
            .setFrames(1)

        if (originalIsBigger) builder.setVideoFilter("scale=${newSize}:-1")

        val executor = FFmpegExecutor()
        executor.createJob(builder.done()).run()
        return ImageIO.read(targetFile)
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
            entries.find { it.name.compareTo(size, true) == 0 }
    }
}
