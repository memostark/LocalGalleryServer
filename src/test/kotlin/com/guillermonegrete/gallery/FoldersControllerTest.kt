package com.guillermonegrete.gallery

import com.guillermonegrete.gallery.data.GetFolderResponse
import com.guillermonegrete.gallery.data.ImageFile
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

    @MockkBean
    private lateinit var foldersRepository: FoldersRepository

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

        every { foldersRepository.getFolders(path) } returns listOf(subFolder)

        every { foldersRepository.getImages("$path/$subFolder") } returns listOf(ImageFile("image1", 100, 100))

        mockMvc.perform(get("/folders/$subFolder")).andDo(print()).andExpect(status().isOk)
                .andExpect(jsonPath("$", hasSize<Array<Any>>(1)))
                .andExpect(jsonPath("$[0].url").value("http://$ipAddress/images/$subFolder/image1"))
                .andExpect(jsonPath("$[0].width").value(100))
                .andExpect(jsonPath("$[0].height").value(100))
    }
}