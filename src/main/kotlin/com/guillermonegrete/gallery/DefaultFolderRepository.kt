package com.guillermonegrete.gallery

import com.guillermonegrete.gallery.data.ImageFile
import org.springframework.stereotype.Component
import java.awt.Dimension
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO
import javax.imageio.stream.FileImageInputStream


@Component
class DefaultFolderRepository: FoldersRepository {

    override fun getFolders(path: String): List<String> {
        return File(path).list()?.toList() ?: emptyList()
    }

    override fun getImages(folder: String): List<ImageFile> {
        return File(folder).listFiles()?.map {
            val dimensions = getImageDimension(it)
            ImageFile(it.name, dimensions.width, dimensions.height)
        } ?: emptyList()
    }

    /**
     * Gets image dimensions for given file
     * @param imgFile image file
     * @return dimensions of image
     * @throws IOException if the file is not a known image
     */
    @Throws(IOException::class)
    fun getImageDimension(imgFile: File): Dimension {
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
        throw IOException("Not a known image file: " + imgFile.absolutePath)
    }
}