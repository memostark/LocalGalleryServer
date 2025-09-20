package com.guillermonegrete.gallery.tags

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.guillermonegrete.gallery.config.NetworkConfig
import com.guillermonegrete.gallery.data.Folder
import com.guillermonegrete.gallery.data.MediaFile
import com.guillermonegrete.gallery.data.MediaFolder
import com.guillermonegrete.gallery.data.SimplePage
import com.guillermonegrete.gallery.data.files.FileMapper
import com.guillermonegrete.gallery.data.files.ImageEntity
import com.guillermonegrete.gallery.data.files.dto.ImageFileDTO
import com.guillermonegrete.gallery.data.toDto
import com.guillermonegrete.gallery.repository.MediaFileRepository
import com.guillermonegrete.gallery.repository.MediaFolderRepository
import com.guillermonegrete.gallery.tags.data.TagDto
import com.guillermonegrete.gallery.tags.data.TagEntity
import com.guillermonegrete.gallery.tags.data.TagFile
import com.guillermonegrete.gallery.tags.data.TagFileDto
import com.guillermonegrete.gallery.tags.data.TagFolder
import com.guillermonegrete.gallery.tags.data.TagFolderDto
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.Instant
import java.util.*

@WebMvcTest
class TagsControllerTest(
    @Autowired val mockMvc: MockMvc,
    @Autowired val mapper: FileMapper,
    @Autowired val objectMapper: ObjectMapper,
) {

    @MockkBean(relaxed = true)
    private lateinit var commandLineRunner: CommandLineRunner

    @MockkBean private lateinit var tagsRepository: TagsRepository
    @MockkBean private lateinit var fileTagsRepository: FileTagsRepository
    @MockkBean private lateinit var folderTagsRepository: FolderTagsRepository
    @MockkBean private lateinit var mediaFolderRepository: MediaFolderRepository
    @MockkBean private lateinit var mediaFileRepository: MediaFileRepository
    @MockkBean private lateinit var networkConfig: NetworkConfig

    private val ipAddress = "dummy-address"

    @BeforeEach
    fun setUp() {
        every { networkConfig.getLocalIpAddress() } returns ipAddress
    }

    @Test
    fun `Given tags, when get all endpoint called, then return them`(){

        val tags = listOf(TagFile("my_tag"), TagFolder("my_folder_tag"))
        every { tagsRepository.findAll() } returns tags

        val result = mockMvc.perform(get("/tags"))
            .andDo(print())
            .andExpect(status().isOk)
            .andReturn()

        val resultResponse = objectMapper.readValue(result.response.contentAsString, object: TypeReference<List<TagEntity>>() {})

        assertThat(resultResponse).hasSize(2)
        assertTagEqual(tags.first(), resultResponse.first())
        assertTagEqual(tags[1], resultResponse[1])
    }

    //region File tag tests
    @Test
    fun `Given no tags, when add endpoint called, then create new tag`(){

        val tag = TagFile("my_tag")
        every { fileTagsRepository.save(any()) } returns tag

        val result = mockMvc.perform(post("/tags/add").param("name", "Cats"))
            .andExpect(status().isOk)
            .andReturn()

        val resultResponse = objectMapper.readValue(result.response.contentAsString, TagEntity::class.java)
        assertTagEqual(tag, resultResponse)
    }

    @Test
    fun `Given valid file id, when add tag endpoint called, then add tag`(){

        every { mediaFileRepository.findById(0) } returns Optional.of(ImageEntity("saved_image.jpg"))
        val savedTag = TagFile("my_tag")
        every { fileTagsRepository.findByName("my_tag") } returns savedTag

        every { fileTagsRepository.save(savedTag) } returns savedTag

        val result = mockMvc.perform(post("/files/{id}/tags", 0)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"name": "my_tag"}"""))
            .andExpect(status().isOk)
            .andReturn()

        val resultResponse = objectMapper.readValue(result.response.contentAsString, TagEntity::class.java)
        assertTagEqual(savedTag, resultResponse)
    }

    @Test
    fun `Given valid file id, when add tags endpoint, then return updated tags`(){
        val fileId = 1L
        val date = Instant.now()
        val savedFile = MediaFile("file_1", creationDate = date, lastModified = date)
        every { mediaFileRepository.findById(fileId) } returns Optional.of(savedFile)

        val files = listOf(
            TagFile("tag_1", date),
            TagFile("tag_2", date),
            TagFile("tag_3", date),
        )
        every { fileTagsRepository.findByIdIn(listOf(2,3,4)) } returns files

        every { mediaFileRepository.save(any()) } returns savedFile

        // Using string comparison instead of
        val expectedResponse = """[{"name":"tag_1","creationDate":"$date","id":0},{"name":"tag_2","creationDate":"$date","id":0},{"name":"tag_3","creationDate":"$date","id":0}]"""

        mockMvc.perform(post("/files/{id}/multitag", fileId)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""[2,3,4]"""))
            .andExpect(status().isOk)
            .andExpect(content().string(expectedResponse))
    }

    @Test
    fun `Given valid tag id, when add tag to files endpoint, then return updated files`(){
        val tagId = 1L
        val date = Instant.now()
        val savedTag = TagFile("my_tag", date)
        every { fileTagsRepository.findById(tagId) } returns Optional.of(savedTag)

        val files = listOf(
            MediaFile("file_1", creationDate = date, lastModified = date),
            MediaFile("file_2", creationDate = date, lastModified = date),
            MediaFile("file_3", creationDate = date, lastModified = date),
        )
        every { mediaFileRepository.findByIdIn(listOf(2,3,4)) } returns files

        every { mediaFileRepository.saveAll<MediaFile>(any()) } returns listOf()

        // Using string comparison instead of
        val expectedResponse =
            """[{"url":"http://$ipAddress/images//file_1","width":0,"height":0,"creationDate":"$date","lastModified":"$date","tags":[{"name":"my_tag","creationDate":"$date","id":0}],"id":0,"file_type":"Image"},""" +
            """{"url":"http://$ipAddress/images//file_2","width":0,"height":0,"creationDate":"$date","lastModified":"$date","tags":[{"name":"my_tag","creationDate":"$date","id":0}],"id":0,"file_type":"Image"},""" +
            """{"url":"http://$ipAddress/images//file_3","width":0,"height":0,"creationDate":"$date","lastModified":"$date","tags":[{"name":"my_tag","creationDate":"$date","id":0}],"id":0,"file_type":"Image"}]"""

        mockMvc.perform(post("/tags/{id}/files", tagId)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""[2,3,4]"""))
            .andExpect(status().isOk)
            .andExpect(content().string(expectedResponse))
    }

    @Test
    fun `Given valid tag id, when get files by tag endpoint called, then files returned`(){

        every { fileTagsRepository.existsById(0) } returns true
        val file = MediaFile("my_file.jpg", folder = MediaFolder("my_folder"))
        every { mediaFileRepository.findFilesByFileTagsIds(listOf(0), DEFAULT_PAGEABLE) } returns PageImpl(listOf(file), DEFAULT_PAGEABLE, 1)

        val result = mockMvc.perform(post("/tags/filesall")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"fileTagIds": [0]}"""))
            .andExpect(status().isOk)
            .andReturn()

        val resultResponse = objectMapper.readValue(result.response.contentAsString, object: TypeReference<SimplePage<ImageFileDTO>>() {})
        val expected = SimplePage(listOf(mapper.toDtoWithHost(file, ipAddress)), 1, 1)
        assertThat(resultResponse).isEqualTo(expected)
    }

    @Test
    fun `Given valid folder id, when get tags of folder endpoint called, then tags returned`(){
        val folderId = 0L
        val tag = TagFileDto("new", 12)
        val tags = setOf(tag)
        every { fileTagsRepository.getTagsWithFilesByFolder(folderId) } returns tags
        val folderTag = TagFolderDto("new_folder", 5)
        every { folderTagsRepository.getFolderTags(folderId) } returns setOf(folderTag)

        val result = mockMvc.perform(get("/folders/{id}/tags", 0))
            .andExpect(status().isOk)
            .andReturn()

        val resultResponse = objectMapper.readValue(result.response.contentAsString, object: TypeReference<List<TagDto>>() {})
        assertThat(resultResponse).hasSize(2)
        val tagResult = resultResponse.first()
        assertThat(tagResult).isEqualTo(tag)
        assertThat(resultResponse[1]).isEqualTo(folderTag)
    }

    @Test
    fun `Given valid folder and tag ids, when get files by folder and tag endpoint called, then files returned`(){
        val tagId = 3L
        every { tagsRepository.existsById(tagId) } returns true
        val folderId = 2L
        every { mediaFolderRepository.existsById(folderId) } returns true
        val files = listOf(MediaFile("image.jpg"))
        every { mediaFileRepository.findFilesByTagsIdAndFolderId(tagId, folderId, DEFAULT_PAGEABLE) } returns PageImpl(files, DEFAULT_PAGEABLE, files.size.toLong())

        val result = mockMvc.perform(get("/folders/{folderId}/tags/{tagId}", folderId, tagId))
            .andExpect(status().isOk)
            .andReturn()

        val resultResponse = objectMapper.readValue(result.response.contentAsString, object: TypeReference<SimplePage<ImageFileDTO>>() {})
        val expected = SimplePage(files.map { mapper.toDtoWithHost(it, ipAddress) }, 1, 1)
        assertThat(resultResponse).isEqualTo(expected)
    }

    //endregion

    //region Folder tag tests

    @Test
    fun `Given no folder tags, when add endpoint called, then create new tag`(){

        val tag = TagFolder("my_folder_tag")
        every { folderTagsRepository.save(any()) } returns tag

        val result = mockMvc.perform(post("/tags/folders/add").param("name", "Cats"))
            .andExpect(status().isOk)
            .andReturn()

        val resultResponse = objectMapper.readValue(result.response.contentAsString, TagEntity::class.java)
        assertTagEqual(tag, resultResponse)
    }

    @Test
    fun `Given valid folder id, when add tag endpoint called, then tag added`(){

        every { mediaFolderRepository.findById(0) } returns Optional.of(MediaFolder("my_folder"))
        val savedTag = TagFolder("my_tag")
        every { folderTagsRepository.findByName("my_tag") } returns savedTag

        every { folderTagsRepository.save(savedTag) } returns savedTag

        val result = mockMvc.perform(post("/folders/{id}/tags", 0)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"name": "my_tag"}"""))
            .andExpect(status().isOk)
            .andReturn()

        val resultResponse = objectMapper.readValue(result.response.contentAsString, TagEntity::class.java)
        assertTagEqual(savedTag, resultResponse)
    }

    @Test
    fun `Given valid tag id, when get folders by tags endpoint called, then files returned`(){
        every { tagsRepository.existsById(0) } returns true
        val folder = MediaFolder("my_folder")
        every { mediaFolderRepository.findFoldersByTagsId(0, DEFAULT_PAGEABLE) } returns PageImpl(listOf(folder), DEFAULT_PAGEABLE, 1)

        val result = mockMvc.perform(post("/tags/folders")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""[0]"""))
            .andExpect(status().isOk)
            .andReturn()

        val resultResponse = objectMapper.readValue(result.response.contentAsString, object: TypeReference<SimplePage<Folder>>() {})
        val expected = SimplePage(listOf(folder.toDto()), 1, 1)
        assertThat(resultResponse).isEqualTo(expected)
    }

    //endregion

    private fun assertTagEqual(expected: TagEntity, actual: TagEntity){
        assertThat(actual.id).isEqualTo(expected.id)
        assertThat(actual.name).isEqualTo(expected.name)
        assertThat(actual.creationDate).isEqualTo(expected.creationDate)
    }

    @TestConfiguration
    internal class InnerConfig{
        @Bean
        fun fileMapper() = FileMapper()
    }

    companion object {
        val DEFAULT_PAGEABLE = PageRequest.of(0, 20)
    }
}
