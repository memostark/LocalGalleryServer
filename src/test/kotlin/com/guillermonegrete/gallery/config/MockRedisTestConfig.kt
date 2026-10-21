package com.guillermonegrete.gallery.config

import io.mockk.every
import io.mockk.mockk
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.ValueOperations

@TestConfiguration
class MockRedisTestConfig {
    @Bean
    fun stringRedisTemplate(): StringRedisTemplate {
        val template = mockk<StringRedisTemplate>(relaxed = true)
        val valueOps = mockk<ValueOperations<String, String>>(relaxed = true)

        every { valueOps.get(any()) } returns "4" // random default number
        every { template.opsForValue() } returns valueOps

        return template
    }
}
