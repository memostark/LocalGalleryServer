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
import net.bramp.ffmpeg.FFmpegExecutor
import net.bramp.ffmpeg.FFprobe
import net.bramp.ffmpeg.probe.FFmpegProbeResult
import net.bramp.ffmpeg.probe.FFmpegStream
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import java.io.OutputStream
import java.nio.file.Files
import java.nio.file.Path
import javax.imageio.ImageIO

@ExtendWith(MockKExtension::class)
class ThumbnailServiceTest {

    private lateinit var service: ThumbnailService

    @MockK
    private lateinit var fFprobe: FFprobe
    @MockK
    private lateinit var ffExecutor: FFmpegExecutor

    @MockK
    private lateinit var fileProvider: FileProvider

    @MockK
    private lateinit var mockThumbnail: File
    @MockK
    private lateinit var mockOriginalFile: File
    @MockK
    private lateinit var mockThumbnailsBase: File

    @BeforeEach
    fun setup() {
        mockkStatic(ImageIO::class)
        mockkStatic(Files::class)
        service = ThumbnailService(fFprobe, ffExecutor, fileProvider)


        val mockBaseFolder = mockk<File>()

        // Generate the base and thumbnail folders, and the original and thumbnai file mocks
        every { fileProvider.createFromBase(DUMMY_FOLDER) } returns mockBaseFolder
        every { fileProvider.getFile(mockBaseFolder, DUMMY_FILE) } returns mockOriginalFile
        every { fileProvider.getFile(mockBaseFolder, ThumbnailService.THUMBNAILS_FOLDER) } returns mockThumbnailsBase
        val thumbnailFilename = ThumbnailType.Small.filename(DUMMY_FILE_NO_EXT)
        every { fileProvider.getFile(mockThumbnailsBase, thumbnailFilename) } returns mockThumbnail

        every { mockThumbnailsBase.exists() } returns true
        every { mockOriginalFile.name } returns DUMMY_FILE // called when extracting the name without extension
        every { mockThumbnail.exists() } returns true

        every { mockOriginalFile.absolutePath } returns ""
        every { mockThumbnail.absolutePath } returns "thumbnail.webp"

        every { ffExecutor.createJob(any()) } returns mockk(relaxed = true)
    }

    @AfterEach
    fun tearDown(){
        unmockkStatic(ImageIO::class)
        unmockkStatic(Files::class)
    }

    // region Image tests

    @Test
    fun `Given existing small thumbnail, when requested, then return its byte data`(){
        val thumbBufferImage = setupBufferedImage(mockThumbnail, 100)

        service.generateThumbnail(DUMMY_FOLDER, DUMMY_FILE, ThumbnailType.Small)

        // Verify bytes created from the thumbnail
        verify { ImageIO.write(thumbBufferImage, THUMBNAIL_EXT, any<OutputStream>()) }
    }

    @Test
    fun `Given non-existing small thumbnail, when requested, then data created`(){
        every { mockThumbnail.exists() } returns false

        // Return file larger than the requested thumbnail
        val originalImage = BufferedImage(ThumbnailType.Small.size + 1, 200, BufferedImage.TYPE_INT_RGB)
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

        // Return file smaller than the requested thumbnail
        val originalImage = setupBufferedImage(mockOriginalFile,ThumbnailType.Small.size - 1)

        service.generateThumbnail(DUMMY_FOLDER, DUMMY_FILE, ThumbnailType.Small)

        // Verify bytes created from the original image
        verify { ImageIO.write(originalImage, THUMBNAIL_EXT, any<OutputStream>()) }
    }

    @Test
    fun `Given non-existing original image thumbnail, when requested, then error`(){
        createOriginalThumbnailMock(exists = false)

        every { mockOriginalFile.toPath() } returns Path.of("")
        every { Files.probeContentType(Path.of("")) } returns "image/jpeg"

        assertThrows(IOException::class.java) {
            service.generateThumbnail(DUMMY_FOLDER, DUMMY_FILE, ThumbnailType.Original)
        }
        verify(exactly = 0) { ffExecutor.createJob(any()) }
    }

    // endregion

    // region Video tests

    @Test
    fun `Given non-existing small video thumbnail, when requested, then data created`(){
        val videoWidth = ThumbnailType.ExtraLarge.size + 1
        setupForVideo(videoWidth)

        val bufferedThumbnail = setupBufferedImage(mockThumbnail, ThumbnailType.Small.size)

        service.generateThumbnail(DUMMY_FOLDER, DUMMY_FILE, ThumbnailType.Small)

        // Verify bytes created from the thumbnail
        verify { ImageIO.write(bufferedThumbnail, THUMBNAIL_EXT, any<OutputStream>()) }
        verify { ffExecutor.createJob(any()) }
    }

    @Test
    fun `Given small original video, when requested thumbnail is bigger, then return original`(){
        val videoWidth = ThumbnailType.Small.size - 1
        setupForVideo(videoWidth)

        val mockSourceThumbnail = createOriginalThumbnailMock()
        val bufferedThumbnail = setupBufferedImage(mockSourceThumbnail, videoWidth)

        service.generateThumbnail(DUMMY_FOLDER, DUMMY_FILE, ThumbnailType.Small)

        // Verify bytes created from the original size thumbnail
        verify { ImageIO.write(bufferedThumbnail, THUMBNAIL_EXT, any<OutputStream>()) }
        verify(exactly = 0) { ffExecutor.createJob(any()) }
    }

    @Test
    fun `Given non-existing original video thumbnail, when requested, then data created`(){
        every { mockOriginalFile.toPath() } returns Path.of("")
        every { Files.probeContentType(Path.of("")) } returns "video/mp4"

        val mockSourceThumbnail = createOriginalThumbnailMock(exists = false)
        val bufferedThumbnail = setupBufferedImage(mockSourceThumbnail, 200)

        service.generateThumbnail(DUMMY_FOLDER, DUMMY_FILE, ThumbnailType.Original)

        // Verify bytes created from the thumbnail
        verify { ImageIO.write(bufferedThumbnail, THUMBNAIL_EXT, any<OutputStream>()) }
        verify { ffExecutor.createJob(any()) }
    }

    // endregion

    /**
     * Setups the buffered image that will be used to generate the output ByteArray.
     */
    private fun setupBufferedImage(file: File, width: Int): BufferedImage {
        val bufferedThumbnail = BufferedImage(width, 200, BufferedImage.TYPE_INT_RGB)
        every { ImageIO.read(file) } returns bufferedThumbnail
        every { ImageIO.write(bufferedThumbnail, THUMBNAIL_EXT, any<OutputStream>()) } returns true
        return bufferedThumbnail
    }

    private fun createOriginalThumbnailMock(exists: Boolean = true): File {
        val mockSourceThumbnail = mockk<File>()
        every { mockSourceThumbnail.exists() } returns exists
        val thumbnailFilename = ThumbnailType.Original.filename(DUMMY_FILE_NO_EXT)
        every { mockSourceThumbnail.absolutePath } returns thumbnailFilename
        every { fileProvider.getFile(mockThumbnailsBase, thumbnailFilename) } returns mockSourceThumbnail

        return mockSourceThumbnail
    }

    private fun setupForVideo(videoWidth: Int) {
        every { mockThumbnail.exists() } returns false

        every { ImageIO.read(mockOriginalFile) } returns null
        val stream = FFmpegStream()
        stream.codec_type = FFmpegStream.CodecType.VIDEO
        stream.width = videoWidth

        val probeMock = mockk<FFmpegProbeResult>()
        every { probeMock.getStreams() } returns listOf(stream)
        every { fFprobe.probe("") } returns probeMock
    }

    companion object {
        const val DUMMY_FOLDER = "dummy"
        const val DUMMY_FILE = "file.jpg"
        const val DUMMY_FILE_NO_EXT = "file"
    }
}