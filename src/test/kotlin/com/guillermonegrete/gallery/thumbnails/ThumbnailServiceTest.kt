package com.guillermonegrete.gallery.thumbnails

import com.guillermonegrete.gallery.FileProvider
import com.guillermonegrete.gallery.thumbnails.ThumbnailService.Companion.THUMBNAIL_EXT
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import net.bramp.ffmpeg.FFprobe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.awt.image.BufferedImage
import java.io.File
import java.io.OutputStream
import javax.imageio.ImageIO

@ExtendWith(MockKExtension::class)
class ThumbnailServiceTest {

    private lateinit var service: ThumbnailService

    @MockK
    private lateinit var fFprobe: FFprobe

    @MockK
    private lateinit var fileProvider: FileProvider

    @MockK
    private lateinit var mockThumbnail: File
    @MockK
    private lateinit var mockOriginalFile: File

    @BeforeEach
    fun setup() {
        mockkStatic(ImageIO::class)
        service = ThumbnailService(fFprobe, fileProvider)


        val mockBaseFolder = mockk<File>()
        val mockThumbnailsBase = mockk<File>()

        // Generate the base and thumbnail folders, and the original and thumbnai file mocks
        every { fileProvider.createFromBase(DUMMY_FOLDER) } returns mockBaseFolder
        every { fileProvider.getFile(mockBaseFolder, DUMMY_FILE) } returns mockOriginalFile
        every { fileProvider.getFile(mockBaseFolder, ThumbnailService.THUMBNAILS_FOLDER) } returns mockThumbnailsBase
        every { fileProvider.getFile(mockThumbnailsBase, any()) } returns mockThumbnail

        every { mockThumbnailsBase.exists() } returns true
        every { mockOriginalFile.name } returns DUMMY_FILE // called when extracting the name without extension
        every { mockThumbnail.exists() } returns true
    }

    @AfterEach
    fun tearDown(){
        unmockkStatic(ImageIO::class)
    }

    @Test
    fun `Given existing small thumbnail, when requested, then return its byte data`(){
        val thumbBufferImage = BufferedImage(100, 200, BufferedImage.TYPE_INT_RGB)
        every { ImageIO.read(mockThumbnail) } returns thumbBufferImage
        every { ImageIO.write(thumbBufferImage, any(), any<OutputStream>()) } returns true

        service.generateThumbnail(DUMMY_FOLDER, DUMMY_FILE, ThumbnailType.Small)

        // Verify bytes created from the thumbnail
        verify { ImageIO.write(thumbBufferImage, THUMBNAIL_EXT, any<OutputStream>()) }
    }

    @Test
    fun `Given non-existing small thumbnail, when requested, then data created`(){
        every { mockThumbnail.exists() } returns false

        // Return file larger the biggest thumbnail
        val originalImage = BufferedImage(ThumbnailType.Large.size + 1, 200, BufferedImage.TYPE_INT_RGB)
        every { ImageIO.read(mockOriginalFile) } returns originalImage
        every { ImageIO.write(any(), THUMBNAIL_EXT, mockThumbnail) } returns true
        every { ImageIO.write(any(), THUMBNAIL_EXT, any<OutputStream>()) } returns true

        service.generateThumbnail(DUMMY_FOLDER, DUMMY_FILE, ThumbnailType.Small)

        // Verify bytes created and the image was written to the thumbnail file
        verify { ImageIO.write(any(), THUMBNAIL_EXT, mockThumbnail) }
        verify { ImageIO.write(any(), THUMBNAIL_EXT, any<OutputStream>()) }
    }

    @Test
    fun `Given small original image, when requested thumbnail is bigger, then return original`(){
        every { mockThumbnail.exists() } returns false

        // Return file smaller than the large thumbnail
        val originalImage = BufferedImage(ThumbnailType.Medium.size, 200, BufferedImage.TYPE_INT_RGB)
        every { ImageIO.read(mockOriginalFile) } returns originalImage
        every { ImageIO.write(originalImage, any(), any<OutputStream>()) } returns true

        service.generateThumbnail(DUMMY_FOLDER, DUMMY_FILE, ThumbnailType.Large)

        // Verify bytes created from the original image
        verify { ImageIO.write(originalImage, THUMBNAIL_EXT, any<OutputStream>()) }
    }

    companion object {
        const val DUMMY_FOLDER = "dummy"
        const val DUMMY_FILE = "file.jpg"
    }
}