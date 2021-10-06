package com.guillermonegrete.gallery

import com.guillermonegrete.gallery.data.ImageFile
import net.bramp.ffmpeg.FFprobe
import net.bramp.ffmpeg.probe.FFmpegStream
import org.springframework.stereotype.Component
import java.awt.Dimension
import java.io.File
import java.io.IOException
import java.lang.RuntimeException
import javax.imageio.ImageIO
import javax.imageio.stream.FileImageInputStream


@Component
class DefaultFolderRepository: FoldersRepository {

    // TODO inject
    private val ffprobe = FFprobe()

    override fun getFolders(path: String): List<String> {
        return File(path).list()?.toList() ?: emptyList()
    }

    override fun getImageNames(folder: String): Set<String> {
        return File(folder).listFiles()?.map { it.name }?.toSet() ?: emptySet()
    }

    override fun getImageInfo(path: String): ImageFile? {
        val file = File(path)
        val dimensions = getImageDimension(file) ?: return null
        return ImageFile(file.name, dimensions.width, dimensions.height)
    }

    override fun getImages(folder: String): List<ImageFile> {
        return File(folder).listFiles()?.mapNotNull { file ->
            val dimensions = getImageDimension(file)
            dimensions?.let { ImageFile(file.name, it.width, it.height) }
        } ?: emptyList()
    }

    /**
     * Gets image dimensions for given file
     * @param imgFile image file
     * @return dimensions of image
     * @throws IOException if the file is not a known image
     */
    @Throws(IOException::class)
    fun getImageDimension(imgFile: File): Dimension? {
        val pos = imgFile.name.lastIndexOf(".")
        if (pos == -1) throw IOException("No extension for file: " + imgFile.absolutePath)
        val suffix = imgFile.name.substring(pos + 1)
        val iter = ImageIO.getImageReadersBySuffix(suffix)
        while (iter.hasNext()) {
            val reader = iter.next()
            try {
                val stream = FileImageInputStream(imgFile)
                reader.input = stream
                val width: Int = reader.getWidth(reader.minIndex)
                val height: Int = reader.getHeight(reader.minIndex)
                return Dimension(width, height)
            } catch (e: IOException) {
                println("Error reading: ${imgFile.absoluteFile}, $e")
            } finally {
                reader.dispose()
            }
        }

        println("Not a known image file: ${imgFile.absolutePath}")
        return if(suffix == "mp4") getVideoDimensions(imgFile.absolutePath) else null
    }

    private fun getVideoDimensions(path: String): Dimension?{
        return try {
            val probeResult = ffprobe.probe(path)
            probeResult.streams?.forEach { stream ->
                if(stream.codec_type == FFmpegStream.CodecType.VIDEO) return Dimension(stream.width, stream.height)
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