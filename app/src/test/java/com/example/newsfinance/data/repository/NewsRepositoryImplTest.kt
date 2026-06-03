package com.example.newsfinance.data.repository

import com.example.newsfinance.FakeNewsApiService
import com.example.newsfinance.FakeUserPreferencesDataStore
import com.example.newsfinance.data.local.UserPreferences
import com.example.newsfinance.data.remote.dto.ArticleDto
import com.example.newsfinance.data.remote.dto.NewsResponseDto
import com.example.newsfinance.data.remote.dto.SourceDto
import com.example.newsfinance.util.Result
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NewsRepositoryImplTest {

    private fun article(url: String) = ArticleDto(
        source = SourceDto("Fonte", "https://news"),
        title = "Titolo",
        description = null,
        url = url,
        image = null,
        publishedAt = "2024-01-01T00:00:00Z",
        content = null
    )

    @Test
    fun `getTopHeadlines drops country filter when its language differs from user language`() = runTest {
        val api = FakeNewsApiService().apply {
            topResponse = { NewsResponseDto(1, listOf(article("https://news/world"))) }
        }
        val prefs = FakeUserPreferencesDataStore(UserPreferences(preferredLang = "en"))
        val repo = NewsRepositoryImpl(api, prefs)

        val emissions = repo.getTopHeadlines(country = "it", category = null).toList()

        assertTrue(emissions.last() is Result.Success)
        // Una sola richiesta (niente doppia chiamata) e senza vincolo di paese,
        // perché paese IT e lingua EN non coincidono.
        assertEquals(1, api.topCalls.size)
        assertEquals(null, api.topCalls.first().country)
        assertEquals("en", api.topCalls.first().lang)
    }

    @Test
    fun `getTopHeadlines keeps country filter when it matches user language`() = runTest {
        val api = FakeNewsApiService().apply {
            topResponse = { NewsResponseDto(1, listOf(article("https://news/it"))) }
        }
        val prefs = FakeUserPreferencesDataStore(UserPreferences(preferredLang = "it"))
        val repo = NewsRepositoryImpl(api, prefs)

        repo.getTopHeadlines(country = "it", category = null).toList()

        assertEquals(1, api.topCalls.size)
        assertEquals("it", api.topCalls.first().country)
        assertEquals("it", api.topCalls.first().lang)
    }

    @Test
    fun `searchNews with country uses that country language`() = runTest {
        val api = FakeNewsApiService().apply {
            searchResponse = NewsResponseDto(1, listOf(article("https://news/roma")))
        }
        val prefs = FakeUserPreferencesDataStore(UserPreferences(preferredLang = "en"))
        val repo = NewsRepositoryImpl(api, prefs)

        repo.searchNews(query = "Roma", country = "it").toList()

        val call = api.searchCalls.last()
        assertEquals("Roma", call.query)
        assertEquals("it", call.lang)     // lingua del paese, non quella utente (en)
        assertEquals("it", call.country)
    }

    @Test
    fun `searchNews without country uses user language and no country filter`() = runTest {
        val api = FakeNewsApiService().apply {
            searchResponse = NewsResponseDto(1, listOf(article("https://news/bitcoin")))
        }
        val prefs = FakeUserPreferencesDataStore(UserPreferences(preferredLang = "en"))
        val repo = NewsRepositoryImpl(api, prefs)

        repo.searchNews(query = "bitcoin").toList()

        val call = api.searchCalls.last()
        assertEquals("en", call.lang)
        assertNull(call.country)
    }
}
