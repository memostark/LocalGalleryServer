package com.guillermonegrete.gallery

import com.guillermonegrete.gallery.data.MediaFile
import com.guillermonegrete.gallery.data.MediaFolder
import com.guillermonegrete.gallery.data.PagedFolderResponse
import com.guillermonegrete.gallery.data.SimplePage
import com.guillermonegrete.gallery.data.files.FileMapper
import com.guillermonegrete.gallery.repository.MediaFileRepository
import com.guillermonegrete.gallery.repository.MediaFolderRepository
import com.guillermonegrete.gallery.services.FolderFetchingService
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.hamcrest.collection.IsCollectionWithSize.hasSize
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.net.InetAddress


@WebMvcTest(controllers = [FoldersController::class])
@TestPropertySource(properties = [
    "base.path=dummy"
])
class FoldersControllerTest(@Autowired val mockMvc: MockMvc) {

    // For the bean in the application class, don't want to run it in these tests
    @MockkBean(relaxed = true)
    private lateinit var commandLineRunner: CommandLineRunner
    @MockkBean lateinit var service: FolderFetchingService

    @MockkBean private lateinit var mediaFolderRepository: MediaFolderRepository
    @MockkBean private lateinit var mediaFileRepository: MediaFileRepository

    @Value("\${base.path}")
    private lateinit var path: String

    private val ipAddress = InetAddress.getLocalHost().hostAddress

    @Throws(Exception::class)
    @Test
    fun `Folders endpoint returns paged response model`(){
        val pageable = PageRequest.of(0, 20)
        val content = List(21) { MediaFolder("name$it", listOf(MediaFile("image.jpg")), 100L + it) }
        every { mediaFolderRepository.findAll(pageable) } returns PageImpl(content.subList(0, 20), pageable, content.size.toLong())

        val expected = PagedFolderResponse(path, SimplePage(emptyList(), 0, 0))
        mockMvc.perform(get("/folders")).andDo(print()).andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value(expected.name))
            .andExpect(jsonPath("$.page.items", hasSize<Array<Any>>(20)))
            .andExpect(jsonPath("$.page.items[0].name").value("name0"))
            .andExpect(jsonPath("$.page.items[0].coverUrl").value("http://$ipAddress/images/name0/image.jpg"))
            .andExpect(jsonPath("$.page.totalPages").value(2))
            .andExpect(jsonPath("$.page.totalItems").value(21))
    }

    @Test
    fun `Sub folder returns list of image files`(){
        val subFolder = "subFolder"

        every { mediaFolderRepository.findByName(subFolder) } returns MediaFolder(subFolder, listOf(MediaFile("image1", 100, 100)))

        mockMvc.perform(get("/folders/$subFolder")).andDo(print()).andExpect(status().isOk)
                .andExpect(jsonPath("$", hasSize<Array<Any>>(1)))
                .andExpect(jsonPath("$[0].url").value("http://$ipAddress/images/$subFolder/image1"))
                .andExpect(jsonPath("$[0].width").value(100))
                .andExpect(jsonPath("$[0].height").value(100))
    }

    @Test
    fun `Sub folder returns page of image files`(){
        val subFolder = "subFolder"
        val content = List(21) { MediaFile("image$it", 100 + it, 100 + it) }

        val mediaFolder = MediaFolder(subFolder)
        val pageable = PageRequest.of(0, 20)

        every { mediaFolderRepository.findByName(subFolder) } returns mediaFolder

        every { mediaFileRepository.findAllByFolder(mediaFolder, pageable) } returns PageImpl(content.subList(0, 20), pageable, content.size.toLong())

        // First check the items then total pages and total items
        mockMvc.perform(get("/folders/$subFolder").param("size", "20").param("page", "0")).andDo(print()).andExpect(status().isOk)
            .andExpect(jsonPath("$.items", hasSize<Array<Any>>(20)))
            .andExpect(jsonPath("$.items[0].url").value("http://$ipAddress/images/$subFolder/image0"))
            .andExpect(jsonPath("$.items[0].width").value(100))
            .andExpect(jsonPath("$.items[0].height").value(100))
            .andExpect(jsonPath("$.totalPages").value(2))
            .andExpect(jsonPath("$.totalItems").value(21))
    }

    @TestConfiguration
    internal class InnerConfig{
        @Bean
        fun fileMapper() = FileMapper()
    }
}
