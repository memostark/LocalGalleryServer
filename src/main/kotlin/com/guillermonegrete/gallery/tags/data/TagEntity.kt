package com.guillermonegrete.gallery.tags.data

import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorColumn
import jakarta.persistence.DiscriminatorType
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Inheritance
import jakarta.persistence.InheritanceType
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Represent a tag in the database.
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="tag_type",
    discriminatorType = DiscriminatorType.INTEGER)
sealed class TagEntity(
    @Column(nullable = false, unique = true)
    open val name: String = "",
    @Column(name = "creation_date", nullable = false)
    open val creationDate: Instant = Instant.now().truncatedTo(ChronoUnit.SECONDS), // by default the db saves in seconds, truncate to avoid having different milliseconds
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open val id: Long = 0,
){

    override fun toString(): String {
        return "{id: $id, name: $name, date: $creationDate}"
    }
}
