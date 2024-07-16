package com.guillermonegrete.gallery

import com.guillermonegrete.gallery.data.files.ImageEntity
import com.guillermonegrete.gallery.services.GetFileInfoService
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.file.Paths
import java.sql.Timestamp
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class DefaultFolderRepositoryTest{

    private lateinit var repo: FoldersRepository

    @MockK private lateinit var infoService: GetFileInfoService
    private val clock = Clock.fixed(Instant.parse("2021-10-29T00:36:14.56Z"), ZoneId.systemDefault())

    @BeforeEach
    fun setUp(){
        MockKAnnotations.init(this)
        repo = DefaultFolderRepository(infoService, clock)
    }

    @Test
    fun `Given image file, when getMedia, then return info`() {
        val path = "c:\\to\\path\\file.jpg"
        val creationDate = Timestamp.valueOf("2019-03-18 19:25:42").toInstant()

        every { infoService.getCreationDate(Paths.get(path)) } returns creationDate
        every { infoService.getImageSize(File(path)) } returns GetFileInfoService.Size(100, 300)

        val media = repo.getMediaInfo(path)

        val expected = ImageEntity("file.jpg", 100, 300, creationDate = creationDate, lastModified = clock.instant())
        assertImageEntity(expected, media)
    }

    private fun assertImageEntity(expected: ImageEntity, actual: Any?){
        assertTrue(actual is ImageEntity)
        if(actual !is ImageEntity) throw Exception("Object is not ImageEntity")
        assertEquals(expected.width, actual.width)
        assertEquals(expected.height, actual.height)
        assertEquals(expected.creationDate, actual.creationDate)
        assertEquals(expected.lastModified, actual.lastModified)
        assertEquals(expected.filename, actual.filename)
        assertEquals(expected.folder, actual.folder)
    }
}
