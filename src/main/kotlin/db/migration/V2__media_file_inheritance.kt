package db.migration

import net.bramp.ffmpeg.FFprobe
import net.bramp.ffmpeg.probe.FFmpegStream
import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import org.springframework.jdbc.core.DataClassRowMapper
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.datasource.SingleConnectionDataSource
import java.io.File
import java.io.IOException
import java.lang.RuntimeException
import javax.imageio.ImageIO
import javax.imageio.stream.FileImageInputStream

/**
 * This migration queries all the files within the folders, gets the type of the file (image or video)
 * Adds new columns for type and duration
 * Updates the database with the type and if it's a video with the duration
 */
class V2__media_file_inheritance : BaseJavaMigration() {

    private var basePath: String? = null

    private val ffprobe = FFprobe()

    @Override
    override fun migrate(context: Context) {
//        basePath = "c:\\path\\here"
        basePath = "D:\\Pictures\\tests"
        basePath ?: throw IOException("Folder path not set")
        println("Starting migration to V2")
        println("For folder path: $basePath")

        val template = JdbcTemplate(SingleConnectionDataSource(context.connection, true))
        val query: List<SimpleMediaFolder> = template
            .query(/* language=sql */ "SELECT * FROM db_gallery_test.media_folder;", DataClassRowMapper(SimpleMediaFolder::class.java))

        println("Got queries: $query")

        // Add new columns
        template.execute(/* language=sql */ "ALTER TABLE media_file ADD duration INT NULL;")
        template.execute(/* language=sql */ "ALTER TABLE media_file ADD file_type INT NULL;")

        println("Altered tables successfully")

        query.forEach { folder ->
            val folderPath = "$basePath/${folder.name}"
            println("Processing folder: $folderPath")
            val fileQuery: List<SimpleMediaFile> = template
                .query(/* language=sql */ "SELECT * FROM db_gallery_test.media_file WHERE folder_id = ?;", DataClassRowMapper(SimpleMediaFile::class.java), folder.id)

            fileQuery.forEach { mediaFile ->
                val fullPath = "$folderPath/${mediaFile.filename}"
                println("Processing file: ${mediaFile.filename}")
                val file = File(fullPath)

                // Get file extension
                val pos = file.name.lastIndexOf(".")
                if (pos == -1) {
                    println("No extension for file: ${file.absolutePath}")
                } else{

                    val suffix = file.name.substring(pos + 1)

                    // File type 0 for images and 1 for video
                    if(isImage(file, suffix)){
                        template.execute(/* language=sql */ "UPDATE db_gallery_test.media_file SET file_type = 1 WHERE id = ${mediaFile.id};")
                    } else if(suffix in setOf("mp4", "webm")) {
                        val duration = getDuration(fullPath)
                        if (duration != null)
                            template.execute(/* language=sql */ "UPDATE db_gallery_test.media_file SET file_type = 2, duration = $duration WHERE id = ${mediaFile.id};")

                    }

                }
            }
        }
    }

    @Throws(IOException::class)
    fun isImage(imgFile: File, suffix: String): Boolean {

        val iter = ImageIO.getImageReadersBySuffix(suffix)
        while (iter.hasNext()) {
            val reader = iter.next()
            try {
                val stream = FileImageInputStream(imgFile)
                reader.input = stream
                return true
            } catch (e: IOException) {
                println("Error reading: ${imgFile.absoluteFile}, $e")
            } finally {
                reader.dispose()
            }
        }
        return false
    }

    private fun getDuration(path: String): Int?{
        return try {
            val probeResult = ffprobe.probe(path)
            probeResult.streams?.forEach { stream ->
                if(stream.codec_type == FFmpegStream.CodecType.VIDEO) return probeResult.format.duration.toInt()
            }
            println("No video codec found for file: $path")
            null
        } catch (e: IOException){
            println(e.message)
            null
        } catch (e: RuntimeException){
            println(e.message)
            null
        }
    }

    data class SimpleMediaFolder(val id: Long, val name: String)
    data class SimpleMediaFile(val id: Long, val filename: String, val width: Int, val height: Int, val folder_id: Int)
}