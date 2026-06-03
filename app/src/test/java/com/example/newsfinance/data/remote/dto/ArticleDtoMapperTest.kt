package com.example.newsfinance.data.remote.dto

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ArticleDtoMapperTest {

    private fun dto(
        url: String? = "https://news/article",
        title: String? = "Titolo"
    ) = ArticleDto(
        source = SourceDto(name = "Fonte", url = "https://news"),
        title = title,
        description = "Descrizione",
        url = url,
        image = "https://news/img.png",
        publishedAt = "2024-01-01T00:00:00Z",
        content = "Contenuto"
    )

    @Test
    fun `valid dto maps to domain with category from caller`() {
        val article = dto().toDomain(category = "business")
        assertEquals("https://news/article", article?.id)
        assertEquals("https://news/article", article?.url)
        assertEquals("Titolo", article?.title)
        assertEquals("Fonte", article?.sourceName)
        assertEquals("business", article?.category)
    }

    @Test
    fun `null url returns null`() {
        assertNull(dto(url = null).toDomain())
    }

    @Test
    fun `null title returns null`() {
        assertNull(dto(title = null).toDomain())
    }
}
