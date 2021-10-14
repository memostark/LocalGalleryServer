package com.guillermonegrete.gallery

import com.guillermonegrete.gallery.data.MediaFile
import com.guillermonegrete.gallery.data.files.ImageEntity
import com.guillermonegrete.gallery.data.files.VideoEntity
import net.bramp.ffmpeg.FFprobe
import net.bramp.ffmpeg.probe.FFmpegStream
import org.springframework.stereotype.Component
import java.io.File
import java.io.IOException
import java.lang.RuntimeException
import javax.imageio.ImageIO
import javax.imageio.stream.FileImageInputStream


@Component
class DefaultFolderRepository: FoldersRepository {

    // TODO inject
    private val ffprobe = FFprobe()

    private val supportedVideo = setOf("mp4", "webm")

    override fun getFolders(path: String): List<String> {
        return File(path).list()?.toList() ?: emptyList()
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

    fun getMediaFile(file: File): MediaFile? {
        val suffix = getSuffix(file) ?: return null

        // First try to get the image info
        val imageInfo = getImageInfo(suffix, file)
        if(imageInfo != null) return imageInfo

        // Otherwise, try to get the video info
        return if(suffix in supportedVideo) getVideoDimensions(file.absolutePath) else null
    }

    /**
     * Gets image dimensions for given file
     * @param imgFile image file
     * @return dimensions of image
     */
    @Throws(IOException::class)
    fun getImageInfo(suffix: String, imgFile: File): ImageEntity? {

        val iter = ImageIO.getImageReadersBySuffix(suffix)
        while (iter.hasNext()) {
            val reader = iter.next()
            try {
                val stream = FileImageInputStream(imgFile)
                reader.input = stream
                val width: Int = reader.getWidth(reader.minIndex)
                val height: Int = reader.getHeight(reader.minIndex)
                return ImageEntity(imgFile.name, width, height)
            } catch (e: IOException) {
                println("Error reading: ${imgFile.absoluteFile}, $e")
            } finally {
                reader.dispose()
            }
        }

        println("Not a known image file: ${imgFile.absolutePath}")
        return null
    }

    private fun getVideoDimensions(path: String): VideoEntity?{
        return try {
            val probeResult = ffprobe.probe(path)
            probeResult.streams?.forEach { stream ->
                // Add only the file name, not the full path
                if(stream.codec_type == FFmpegStream.CodecType.VIDEO) return VideoEntity(File(path).name, stream.width, stream.height, probeResult.format.duration.toInt())
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
}
