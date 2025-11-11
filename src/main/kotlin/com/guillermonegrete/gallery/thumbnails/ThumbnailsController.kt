package com.guillermonegrete.gallery.thumbnails

import com.guillermonegrete.gallery.services.thumbnail.ThumbnailService
import com.guillermonegrete.gallery.services.thumbnail.ThumbnailType
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
class ThumbnailsController(private val thumbnailService: ThumbnailService) {

    @GetMapping("/thumbnails/{subFolder}/{filename}", produces = ["image/webp"])
    @ResponseBody
    fun getThumbnail(
        @PathVariable subFolder: String,
        @PathVariable filename: String,
        @RequestParam("size") size: String?
    ): ResponseEntity<ByteArray> {
        val type = if (size == null) ThumbnailType.Small else ThumbnailType.getThumbnailType(size) ?: ThumbnailType.Small

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType("image/webp"))
            .body(thumbnailService.generateThumbnail(subFolder, filename, type))
    }
}
