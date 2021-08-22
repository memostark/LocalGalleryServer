package com.guillermonegrete.gallery

import com.guillermonegrete.gallery.data.GetFolderResponse
import com.guillermonegrete.gallery.data.ImageFile
import com.guillermonegrete.gallery.data.MediaFile
import com.guillermonegrete.gallery.data.MediaFolder
import com.guillermonegrete.gallery.repository.MediaFolderRepository
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.collection.IsCollectionWithSize.hasSize
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.net.InetAddress

@WebMvcTest
@TestPropertySource(properties = [
    "base.path=dummy"
])
class FoldersControllerTest(@Autowired val mockMvc: MockMvc) {

    // For the bean in application class, don't want to run it in these tests
    @MockkBean(relaxed = true)
    private lateinit var commandLineRunner: CommandLineRunner

    @MockkBean private lateinit var foldersRepository: FoldersRepository
    @MockkBean private lateinit var mediaFolderRepository: MediaFolderRepository

    @Value("\${base.path}")
    private lateinit var path: String

    @Throws(Exception::class)
    @Test
    fun `Folders endpoint returns response model`(){
        every { foldersRepository.getFolders(path) } returns emptyList()

        val expected = GetFolderResponse(path, emptyList())
        mockMvc.perform(get("/folders")).andDo(print()).andExpect(status().isOk)
                .andExpect(jsonPath("$.name").value(expected.name))
                .andExpect(jsonPath("$.folders", `is`(expected.folders)))
    }

    @Test
    fun `Sub folder returns list of image files`(){
        val subFolder = "subFolder"
        val ipAddress = InetAddress.getLocalHost().hostAddress

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
        val ipAddress = InetAddress.getLocalHost().hostAddress
        val page: List<ImageFile> = List(21) { ImageFile("image$it", 100 + it, 100 + it) }

        every { foldersRepository.getFolders(path) } returns listOf(subFolder)

        every { foldersRepository.getImages("$path/$subFolder") } returns page

        // First check the items then total pages and total items
        mockMvc.perform(get("/folders/$subFolder").param("size", "20").param("page", "0")).andDo(print()).andExpect(status().isOk)
            .andExpect(jsonPath("$.items", hasSize<Array<Any>>(20)))
            .andExpect(jsonPath("$.items[0].url").value("http://$ipAddress/images/$subFolder/image0"))
            .andExpect(jsonPath("$.items[0].width").value(100))
            .andExpect(jsonPath("$.items[0].height").value(100))
            .andExpect(jsonPath("$.totalPages").value(2))
            .andExpect(jsonPath("$.totalItems").value(21))
    }
}