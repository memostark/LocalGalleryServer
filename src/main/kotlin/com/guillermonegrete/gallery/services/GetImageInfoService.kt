package com.guillermonegrete.gallery.services

import org.springframework.stereotype.Service
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes
import java.time.Instant

@Service
class GetImageInfoService {

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
}