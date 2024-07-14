package com.guillermonegrete.gallery.services

import net.bramp.ffmpeg.FFprobe
import net.bramp.ffmpeg.probe.FFmpegStream
import org.springframework.stereotype.Service
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes
import java.time.Instant

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

    fun getImageSize(imgFile: File): Size? {
        return try {
            val probeResult = ffprobe.probe(imgFile.absolutePath)
            val stream = probeResult?.streams?.firstOrNull()
            if (stream == null) {
                println("Not a known image file: ${imgFile.absolutePath}")
                return null
            }
            return Size(stream.width, stream.height)
        }catch (e: IOException){
            println(e.message)
            null
        }catch (e: RuntimeException){
            println(e.message)
            null
        }
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

    private fun get16bit(data: ByteArray, index: Int): Int {
        // The mask (0xFF) converts the byte from signed (this is how java originally reads the byte) to unsigned
        return data[index].toInt() and 0xFF or (data[index + 1].toInt() and 0xFF shl 8)
    }

    private fun get24bit(data: ByteArray, index: Int): Int {
        return get16bit(data, index) or (data[index + 2].toInt() and 0xFF shl 16)
    }

    data class Size(val width: Int, val height: Int)

    data class VideoInfo(val width: Int, val height: Int, val duration: Int)

    companion object {
        const val WEBP_FORMAT = "webp"
    }
}
