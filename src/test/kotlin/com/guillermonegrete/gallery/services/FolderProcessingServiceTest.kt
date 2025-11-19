package com.guillermonegrete.gallery.services

import com.guillermonegrete.gallery.FileProvider
import com.guillermonegrete.gallery.FoldersRepository
import com.guillermonegrete.gallery.data.MediaFile
import com.guillermonegrete.gallery.data.MediaFolder
import com.guillermonegrete.gallery.data.files.ImageEntity
import com.guillermonegrete.gallery.data.files.VideoEntity
import com.guillermonegrete.gallery.repository.MediaFileRepository
import com.guillermonegrete.gallery.repository.MediaFolderRepository
import com.guillermonegrete.gallery.thumbnails.THUMBNAILS_FOLDER
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.io.File

@ExtendWith(MockKExtension::class)
class FolderProcessingServiceTest{

    private lateinit var service: FolderProcessingService

    @MockK private lateinit var folderRepository: FoldersRepository
    @MockK private lateinit var fileEntityRepo: MediaFileRepository
    @MockK private lateinit var folderEntityRepo: MediaFolderRepository
    @MockK private lateinit var fetchingService: FolderFetchingService
    @MockK private lateinit var fileProvider: FileProvider

    @BeforeEach
    fun setUp(){
        service = FolderProcessingService(folderRepository, fileEntityRepo, folderEntityRepo, fileProvider, fetchingService)

        // Mocks for the thumbnails
        val mockFolder = mockk<File>()
        val mockThumbnailFolder = mockk<File>()
        val thumbnailPath = "/thumbnail"
        every { mockThumbnailFolder.absolutePath } returns thumbnailPath
        every { fileProvider.createFromBase(any()) } returns mockFolder
        every { fileProvider.getFile(mockFolder, THUMBNAILS_FOLDER) } returns mockThumbnailFolder
        every { folderRepository.createFolder(thumbnailPath) } returns false
    }

    @Test
    fun `Given a non-saved folder, when recalculate, then new folder and files saved`(){
        val path = "path"
        // Two folders in the path, one already in db and the other is not
        every { folderRepository.getFolders(path) } returns listOf("folder", "new_folder")
        every { fetchingService.getMediaFolder("folder") } returns MediaFolder("folder", listOf(MediaFile("saved", id=1)))
        every { fetchingService.getMediaFolder("new_folder") } returns null

        // Return contents of folder already in db
        every { folderRepository.getImageNames("$path/folder") } returns setOf("saved")

        // Create new folder in db
        val newFolder = MediaFolder("new_folder")
        every { folderEntityRepo.save(MediaFolder("new_folder")) } returns newFolder

        // Files of the new folder
        val newImage = ImageEntity("new_image")
        val newVideo = VideoEntity("new_video", duration = 10)
        every { folderRepository.getMedia("$path/new_folder") } returns listOf(newImage, newVideo)

        every { fileEntityRepo.save(any()) } returns mockk()

        service.processFolder(path)

        // Assert the folder was created first and then the files
        verifySequence { folderEntityRepo.save(newFolder); fileEntityRepo.save(newImage); fileEntityRepo.save(newVideo) }
    }

    @Test
    fun `Given a saved folder, when recalculate, new files saved`(){
        val path = "path"
        // One folder in the filesystem and also in teh db
        every { folderRepository.getFolders(path) } returns listOf("folder")
        every { fetchingService.getMediaFolder("folder") } returns MediaFolder("folder", listOf(MediaFile("saved", id=1)))

        // Return filesystem contents of the folder already in db
        every { folderRepository.getImageNames("$path/folder") } returns setOf("saved", "not_saved.jpg")

        val newImage = ImageEntity("not saved")
        every { folderRepository.getMediaInfo("$path/folder/not_saved.jpg") } returns newImage

        every { fileEntityRepo.save(any()) } returns mockk()

        service.processFolder(path)

        verifyAll {  fileEntityRepo.save(newImage); }
    }
}
