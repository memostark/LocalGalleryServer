package com.guillermonegrete.gallery.tags

import com.guillermonegrete.gallery.tags.data.TagEntity
import org.springframework.data.jpa.repository.JpaRepository

interface TagsRepository: JpaRepository<TagEntity, Long>
