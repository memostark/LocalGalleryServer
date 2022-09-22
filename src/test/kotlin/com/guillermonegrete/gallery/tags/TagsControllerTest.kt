package com.guillermonegrete.gallery.tags

import com.guillermonegrete.gallery.data.files.FileMapper
import com.guillermonegrete.gallery.repository.MediaFileRepository
import com.guillermonegrete.gallery.repository.MediaFolderRepository
import com.guillermonegrete.gallery.tags.data.TagEntity
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest
class TagsControllerTest(@Autowired val mockMvc: MockMvc) {

    @MockkBean(relaxed = true)
    private lateinit var commandLineRunner: CommandLineRunner

    @MockkBean private lateinit var tagsRepository: TagsRepository
    @MockkBean private lateinit var mediaFolderRepository: MediaFolderRepository
    @MockkBean private lateinit var mediaFileRepository: MediaFileRepository

    @Test
    fun `Given no tags, when add endpoint called, then create new tag`(){

        every { tagsRepository.save(any()) } returns TagEntity()

        mockMvc.perform(post("/tags/add?name=Cats"))
            .andExpect(status().isOk)
            .andExpect { content().string("Saved") }
    }

    @TestConfiguration
    internal class InnerConfig{
        @Bean
        fun fileMapper() = FileMapper()
    }
}
