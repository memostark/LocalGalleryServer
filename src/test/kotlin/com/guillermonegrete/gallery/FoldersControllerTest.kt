package com.guillermonegrete.gallery

import com.guillermonegrete.gallery.data.GetFolderResponse
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.hamcrest.CoreMatchers.`is`
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest
@TestPropertySource(properties = [
    "base.path=dummy"
])
class FoldersControllerTest(@Autowired val mockMvc: MockMvc) {

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
}