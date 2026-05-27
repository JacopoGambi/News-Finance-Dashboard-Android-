package com.example.newsfinance.domain.usecase

import com.example.newsfinance.domain.model.Article
import com.example.newsfinance.domain.repository.NewsRepository
import com.example.newsfinance.util.Result
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLocalNewsUseCase @Inject constructor(
    private val newsRepository: NewsRepository
) {
    operator fun invoke(countryCode: String): Flow<Result<List<Article>>> =
        newsRepository.getTopHeadlines(country = countryCode, category = null)
}
