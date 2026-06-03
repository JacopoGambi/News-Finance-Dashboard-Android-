package com.example.newsfinance.domain.repository

import com.example.newsfinance.domain.model.Article
import com.example.newsfinance.util.Result
import kotlinx.coroutines.flow.Flow

interface NewsRepository {

    fun getTopHeadlines(country: String?, category: String?): Flow<Result<List<Article>>>

    fun searchNews(query: String): Flow<Result<List<Article>>>
}
