package com.guillermonegrete.gallery

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.guillermonegrete.gallery.config.NetworkConfig
import com.guillermonegrete.gallery.data.*
import com.guillermonegrete.gallery.data.files.FileMapper
import com.guillermonegrete.gallery.data.files.dto.ImageFileDTO
import com.guillermonegrete.gallery.repository.MediaFileRepository
import com.guillermonegrete.gallery.repository.MediaFolderRepository
import com.guillermonegrete.gallery.services.FolderFetchingService
import com.guillermonegrete.gallery.tags.TagsControllerTest.Companion.DEFAULT_PAGEABLE
import com.guillermonegrete.gallery.tags.TagsRepository
import com.guillermonegrete.gallery.thumbnails.thumbnailSizesMap
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.collection.IsCollectionWithSize.hasSize
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status


@WebMvcTest(controllers = [FoldersController::class])
@TestPropertySource(properties = [
    "base.path=dummy"
])
class FoldersControllerTest(
    @param:Autowired val mockMvc: MockMvc,
    @param:Autowired val mapper: FileMapper,
    @param:Autowired val objectMapper: ObjectMapper
) {

    // For the bean in the application class, don't want to run it in these tests
    @MockkBean(relaxed = true)
    private lateinit var commandLineRunner: CommandLineRunner
    @MockkBean lateinit var service: FolderFetchingService

    @MockkBean private lateinit var mediaFolderRepository: MediaFolderRepository
    @MockkBean private lateinit var mediaFileRepository: MediaFileRepository
    @MockkBean private lateinit var tagsRepository: TagsRepository
    @MockkBean private lateinit var networkConfig: NetworkConfig

    @Value("\${base.path}")
    private lateinit var path: String

    private val ipAddress = "dummy-address"

    @BeforeEach
    fun setUp() {
        every { networkConfig.getLocalIpAddress() } returns ipAddress
    }

    @Throws(Exception::class)
    @Test
    fun `Folders endpoint returns paged response model`(){
        val pageable = PageRequest.of(0, 20)
        val content = List(21) { MediaFolder("name$it", listOf(MediaFile("image.jpg")), id = 100L + it) }
        val subList = content.subList(0, 20)
        every { mediaFolderRepository.findAll(pageable) } returns PageImpl(subList, pageable, content.size.toLong())

        val expected = PagedFolderResponse(
            path,
            SimplePage(subList.map { Folder(it.name, "http://$ipAddress/images/${it.name}/image.jpg", 1, it.id) },
            2, content.size),
            thumbnailSizesMap,
        )
        val result = mockMvc.perform(get("/folders")).andDo(print())
            .andExpect(status().isOk)
            .andReturn()

        val resultResponse = objectMapper.readValue(result.response.contentAsString, PagedFolderResponse::class.java)
        assertEquals(expected, resultResponse)
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
        val mediaFolder = MediaFolder(subFolder)

        every { mediaFolderRepository.findByName(subFolder) } returns mediaFolder

        val pageable = PageRequest.of(0, 20)
        val content = List(21) { MediaFile("image$it", 100 + it, 100 + it, folder = mediaFolder) }
        val subList = content.subList(0, 20)

        every { mediaFileRepository.findAllByFolder(mediaFolder, pageable) } returns PageImpl(subList, pageable, content.size.toLong())

        val expected = SimplePage(subList.map { mapper.toDtoWithHost(it, ipAddress) }, 2, content.size)
        // First check the items then total pages and total items
        val result = mockMvc.perform(get("/folders/$subFolder").param("size", "20").param("page", "0")).andDo(print())
            .andExpect(status().isOk)
            .andReturn()

        val resultResponse = objectMapper.readValue(result.response.contentAsString, object: TypeReference<SimplePage<ImageFileDTO>>() {})
        assertEquals(expected, resultResponse)
    }

    @Test
    fun `Given valid tag id, when get folders by tags endpoint called, then files returned`(){
        every { tagsRepository.existsById(0) } returns true
        val folder = MediaFolder("my_folder")
        every { mediaFolderRepository.findFoldersByTagsId(0, DEFAULT_PAGEABLE) } returns PageImpl(listOf(folder), DEFAULT_PAGEABLE, 1)

        val result = mockMvc.perform(post("/folders")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""[0]"""))
            .andExpect(status().isOk)
            .andReturn()

        val resultResponse = objectMapper.readValue(result.response.contentAsString, PagedFolderResponse::class.java)
        val expected = PagedFolderResponse(path, SimplePage(listOf(folder.toDto(ipAddress)), 1, 1), thumbnailSizesMap)
        assertThat(resultResponse).isEqualTo(expected)
    }

    @TestConfiguration
    internal class InnerConfig{
        @Bean
        fun fileMapper() = FileMapper()
    }
}
