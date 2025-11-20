package com.guillermonegrete.gallery.thumbnails

import com.guillermonegrete.gallery.FileProvider
import com.guillermonegrete.gallery.thumbnails.ThumbnailService.Companion.THUMBNAIL_EXT
import net.bramp.ffmpeg.FFmpegExecutor
import net.bramp.ffmpeg.FFprobe
import net.bramp.ffmpeg.builder.FFmpegBuilder
import net.bramp.ffmpeg.probe.FFmpegStream
import org.springframework.stereotype.Service
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.nio.file.Files
import javax.imageio.ImageIO

@Service
class ThumbnailService(
    private val ffProbe: FFprobe,
    private val ffExecutor: FFmpegExecutor,
    private val fileProvider: FileProvider,
) {

    fun generateThumbnail(folder: String, filename: String, type: ThumbnailType): ByteArray {

        val baseFolder = fileProvider.createFromBase(folder)
        val originalFile = fileProvider.getFile(baseFolder, filename)

        // Check if thumbnail already exists
        val thumbnailsFolder = fileProvider.getFile(baseFolder, THUMBNAILS_FOLDER)
        if (!thumbnailsFolder.exists()) thumbnailsFolder.mkdir()
        val newFilename = type.filename(originalFile.nameWithoutExtension)
        val newFile = fileProvider.getFile(thumbnailsFolder, newFilename)
        if (newFile.exists()) {
            val thumbnail = ImageIO.read(newFile)
            val baos = ByteArrayOutputStream()
            ImageIO.write(thumbnail, THUMBNAIL_EXT, baos)
            return baos.toByteArray()
        }

        // Return original size thumbnail (for videos only)
        if (type == ThumbnailType.Original) {
            val baos = ByteArrayOutputStream()
            ImageIO.write(generateVideoThumbnail(originalFile, newFile), THUMBNAIL_EXT, baos)
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

        val stream = ffProbe.probe(originalFile.absolutePath)?.streams?.find { it.codec_type == FFmpegStream.CodecType.VIDEO }
        val originalIsBigger = stream != null && stream.width > newSize

        var targetFile = newFile
        if (!originalIsBigger) {
            targetFile = fileProvider.getFile(thumbnailsFolder, ThumbnailType.Original.filename(originalFile.nameWithoutExtension))
            if (targetFile.exists()) return ImageIO.read(targetFile)
        }

        val builder = FFmpegBuilder()
            .setInput(originalFile.absolutePath)
            .addOutput(targetFile.absolutePath)
            .setVideoCodec("libwebp")
            .setFrames(1)

        if (originalIsBigger) builder.setVideoFilter("scale=${newSize}:-1")

        ffExecutor.createJob(builder.done()).run()
        return ImageIO.read(targetFile)
    }

    fun generateVideoThumbnail(originalFile: File, newFile: File): BufferedImage {
        val mimeType = Files.probeContentType(originalFile.toPath())
        if (!mimeType.startsWith("video/")) throw IOException("File is not a video, original size is only available for videos")

        val builder = FFmpegBuilder()
            .setInput(originalFile.absolutePath)
            .addOutput(newFile.absolutePath)
            .setVideoCodec("libwebp")
            .setFrames(1)

        ffExecutor.createJob(builder.done()).run()
        return ImageIO.read(newFile)
    }

    companion object {
        const val THUMBNAIL_EXT = "webp"
    }
}

const val THUMBNAILS_FOLDER = ".thumbnails"

enum class ThumbnailType(val size: Int) {
    Original(0),
    Small(100),
    Medium(350),
    Large(600),
    ExtraLarge(850);

    companion object {
        fun getThumbnailType(size: String) =
            entries.find { it.name.compareTo(size, true) == 0 }
    }
}

fun ThumbnailType.filename(nameNoExt: String) = "$nameNoExt-${name.lowercase()}.$THUMBNAIL_EXT"
