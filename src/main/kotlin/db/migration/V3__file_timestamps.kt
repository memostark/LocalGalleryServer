package db.migration

import com.guillermonegrete.gallery.services.GetImageInfoService
import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import org.springframework.jdbc.core.DataClassRowMapper
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.datasource.SingleConnectionDataSource
import java.nio.file.Paths
import java.sql.Timestamp
import java.time.Instant

/**
 * This migration adds two new fields, creation_date and last_modified.
 * The creation_date is obtained from
 */
class V3__file_timestamps: BaseJavaMigration() {

    private lateinit var basePath: String

    private val imageInfoService = GetImageInfoService()

    override fun migrate(context: Context) {
        println("On migration to v3")
        println("Placeholders: ${context.configuration.placeholders}")
        basePath = context.configuration.placeholders["base_path"]
            ?: throw ExceptionInInitializerError("Folder path not set, please set BASE_PATH environment variable or in the application properties")


        val template = JdbcTemplate(SingleConnectionDataSource(context.connection, true))
        val query: List<V2__media_file_inheritance.SimpleMediaFolder> = template
            .query(/* language=sql */ "SELECT * FROM media_folder;", DataClassRowMapper(
                V2__media_file_inheritance.SimpleMediaFolder::class.java)
            )

        // Add new columns
        template.execute(/* language=sql */ "ALTER TABLE media_file ADD creation_date timestamp NOT NULL;")
        template.execute(/* language=sql */ "ALTER TABLE media_file ADD last_modified timestamp NOT NULL;")

        query.forEach { folder ->
            val folderPath = "$basePath/${folder.name}"
            println("Processing folder: $folderPath")
            val fileQuery: List<V2__media_file_inheritance.SimpleMediaFile> = template
                .query(/* language=sql */ "SELECT * FROM media_file WHERE folder_id = ?;", DataClassRowMapper(V2__media_file_inheritance.SimpleMediaFile::class.java), folder.id)
            fileQuery.forEach { mediaFile ->
                println("Processing file: ${mediaFile.filename}")
                val fullPath = Paths.get("$folderPath/${mediaFile.filename}")

                val creationDate = imageInfoService.getCreationDate(fullPath) ?: Instant.now()
                template.execute(/* language=sql */ "UPDATE media_file SET creation_date = '${Timestamp.from(creationDate)}' WHERE id = ${mediaFile.id};")
                template.execute(/* language=sql */ "UPDATE media_file SET last_modified = NOW() WHERE id = ${mediaFile.id};")
            }
        }
    }
}
