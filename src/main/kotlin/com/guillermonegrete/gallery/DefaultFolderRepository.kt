package com.guillermonegrete.gallery

import com.guillermonegrete.gallery.data.ImageFile
import org.mp4parser.IsoFile
import org.mp4parser.boxes.iso14496.part12.TrackBox
import org.springframework.stereotype.Component
import java.awt.Dimension
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.lang.RuntimeException
import javax.imageio.ImageIO
import javax.imageio.stream.FileImageInputStream


@Component
class DefaultFolderRepository: FoldersRepository {

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

        println("Not a known image file: ${imgFile.absoluteFile}")
        return if(suffix == "mp4") getVideoDimensions(imgFile) else null
    }

    private fun getVideoDimensions(file: File): Dimension?{
        return try {
            val fc = FileInputStream(file).channel
            val isoFile = IsoFile(fc)
            val moov = isoFile.movieBox
            for (box in moov.boxes) {

                if(box is TrackBox){
                    val header = box.trackHeaderBox
                    val dimension = Dimension(header.width.toInt(), header.height.toInt())
                    return if(dimension.height != 0 || dimension.width != 0) dimension else null
                }
            }
            null
        }catch (e: FileNotFoundException){
            println(e.message)
            null
        }catch (e: RuntimeException){
            println(e.message)
            null
        }
    }
}