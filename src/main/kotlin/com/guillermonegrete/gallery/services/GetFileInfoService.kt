package com.guillermonegrete.gallery.services

import net.bramp.ffmpeg.FFprobe
import net.bramp.ffmpeg.probe.FFmpegStream
import org.springframework.stereotype.Service
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes
import java.time.Instant
import javax.imageio.ImageIO
import javax.imageio.stream.FileImageInputStream

@Service
class GetFileInfoService {

    // TODO inject
    private val ffprobe = FFprobe()

    fun getCreationDate(path: Path): Instant? {
        return try {
            val  attr = Files.readAttributes(path, BasicFileAttributes::class.java)
            val creationTime = attr.creationTime().toInstant()
            val modifiedTime = attr.lastModifiedTime().toInstant()

            if(creationTime.isBefore(modifiedTime)) creationTime else modifiedTime
        } catch (ex: IOException) {
            println("Error when getting image creation time: ${ex.message}")
            ex.printStackTrace()
            null
        }
    }

    fun getImageSize(suffix: String, imgFile: File): Size? {

        return when(suffix){
            WEBP_FORMAT -> getWebPDimensions(imgFile)
            else -> getImageDimensions(suffix, imgFile)
        }
    }

    private fun getImageDimensions(suffix: String, imgFile: File): Size? {

        val iter = ImageIO.getImageReadersBySuffix(suffix)
        while (iter.hasNext()) {
            val reader = iter.next()
            try {
                val stream = FileImageInputStream(imgFile)
                reader.input = stream
                val width: Int = reader.getWidth(reader.minIndex)
                val height: Int = reader.getHeight(reader.minIndex)
                stream.close()
                return Size(width, height)
            } catch (e: IOException) {
                println("Error reading: ${imgFile.absoluteFile}, $e")
            } finally {
                reader.dispose()
            }
        }

        println("Not a known image file: ${imgFile.absolutePath}")
        return null
    }

    /**
     * Java's ImageIO has no support for WebP. For this format the dimensions can be extracted from the header.
     * Support only for WebP Extended File Format (VP8X)
     * More information: https://stackoverflow.com/questions/64548364/parsing-webp-file-header-in-kotlin-to-get-its-height-and-width-but-getting-unex
     */
    private fun getWebPDimensions(imgFile: File): Size {
        val stream = FileInputStream(imgFile)
        val data = stream.readNBytes(30)
        val width = 1 + (get24bit(data, 24))
        val height = 1 + (get24bit(data, 27))
        return Size(width, height)
    }

    fun getVideoDimensions(path: String): VideoInfo? {
        return try {
            val probeResult = ffprobe.probe(path)
            probeResult.streams?.forEach { stream ->
                // Add only the file name, not the full path
                if(stream.codec_type == FFmpegStream.CodecType.VIDEO)
                    return VideoInfo(stream.width, stream.height, probeResult.format.duration.toInt())
            }
            null
        }catch (e: IOException){
            println(e.message)
            null
        }catch (e: RuntimeException){
            println(e.message)
            null
        }
    }

    private fun get24bit(data: ByteArray, index: Int): Int {
        return data[index].toInt() and 0xFF or (data[index + 1].toInt() and 0xFF shl 8) or (data[index + 2].toInt() and 0xFF shl 16)
    }

    data class Size(val width: Int, val height: Int)

    data class VideoInfo(val width: Int, val height: Int, val duration: Int)

    companion object {
        const val WEBP_FORMAT = "webp"
    }
}
