package com.example.newsfinance.domain.usecase

import com.example.newsfinance.domain.model.Article
import com.example.newsfinance.domain.repository.NewsRepository
import com.example.newsfinance.util.Result
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchNewsUseCase @Inject constructor(
    private val newsRepository: NewsRepository
) {
    operator fun invoke(query: String): Flow<Result<List<Article>>> =
        newsRepository.searchNews(query = query)
}
