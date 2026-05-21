package com.example.newsfinance.data.repository

import com.example.newsfinance.data.remote.api.NewsApiService
import com.example.newsfinance.data.remote.dto.toDomain
import com.example.newsfinance.domain.model.Article
import com.example.newsfinance.domain.repository.NewsRepository
import com.example.newsfinance.util.Constants
import com.example.newsfinance.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class NewsRepositoryImpl @Inject constructor(
    private val newsApiService: NewsApiService
) : NewsRepository {

    override fun getTopHeadlines(
        country: String,
        category: String?
    ): Flow<Result<List<Article>>> = flow {
        emit(Result.Loading)
        try {
            val response = newsApiService.getTopHeadlines(
                country = country,
                category = category,
                apiKey = Constants.NEWS_API_KEY
            )
            val articles = response.articles
                .orEmpty()
                .mapNotNull { it.toDomain(category) }
            emit(Result.Success(articles))
        } catch (t: Throwable) {
            emit(Result.Error(t.message ?: "Errore nel recupero delle notizie", t))
        }
    }

    override fun searchNews(query: String): Flow<Result<List<Article>>> = flow {
        emit(Result.Loading)
        try {
            val response = newsApiService.searchNews(
                query = query,
                apiKey = Constants.NEWS_API_KEY
            )
            val articles = response.articles
                .orEmpty()
                .mapNotNull { it.toDomain() }
            emit(Result.Success(articles))
        } catch (t: Throwable) {
            emit(Result.Error(t.message ?: "Errore nella ricerca", t))
        }
    }
}
