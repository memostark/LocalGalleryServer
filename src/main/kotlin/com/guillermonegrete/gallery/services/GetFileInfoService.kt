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
import java.util.*
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
    private fun getWebPDimensions(imgFile: File): Size? {
        val stream = FileInputStream(imgFile)
        val data = stream.readNBytes(30)
        // All formats consist of a file header (12 bytes) and a ChunkHeader (8 bytes)
        // The first four ChunkHeader bytes contain the 4 characters of the format (12 to 15):
        val imageFormat = String(Arrays.copyOfRange(data, 12, 16)) // exclusive range
        val width: Int
        val height: Int
        when(imageFormat) {
            "VP8 " -> { // last character is a space
                // Simple File Format (Lossy)
                // The data is in the VP8 specification and the decoding guide explains how to get the dimensions: https://datatracker.ietf.org/doc/html/rfc6386#section-19.1
                // The formats consists of the frame_tag (3 bytes), start code (3 bytes), horizontal_size_code (2 bytes) and vertical_size_code (2 bytes)
                // The size is 14 bits, use a mask to remove the last two digits
                width = get16bit(data, 26) and 0x3FFF
                height = get16bit(data, 28) and 0x3FFF
            }
            "VP8X" -> {
                // Extended File Format, size position specified here: https://developers.google.com/speed/webp/docs/riff_container#extended_file_format
                // The width starts 4 bytes after the ChunkHeader with a size of 3 bytes, the height comes after.
                width = 1 + (get24bit(data, 24))
                height = 1 + (get24bit(data, 27))
            }
            "VP8L" -> {
                // Simple File Format (Lossless), specification here: https://developers.google.com/speed/webp/docs/webp_lossless_bitstream_specification#3_riff_header
                // The format consists of a signature (1 byte), 14 bit width (2 bytes) and 14 bit height (2 bytes)
                // The width and height are in consecutive bits
                val firstBytes = get16bit(data, 21)
                width = 1 + (firstBytes and 0x3FFF)
                val lastTwoDigits =  (firstBytes and 0xC000) shr 14 // the last 2 bits correspond to the first 2 bits of the height
                // Extract the remaining 12 bits and shift them to add space for the two digits
                height = 1 + ((get16bit(data, 23) and 0xFFF shl 2) or lastTwoDigits)
            }
            else -> return null
        }
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
