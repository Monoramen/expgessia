package app.expgessia.domain.model

import java.util.Date

data class Note(
    val id: Long,
    val title: String,
    val content: String,
    val creationDate: Long,
    val lastModifiedDate: Long,
    val tags: List<String>,
)
