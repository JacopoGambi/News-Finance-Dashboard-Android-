package com.example.newsfinance.domain.usecase

import com.example.newsfinance.domain.model.Article
import com.example.newsfinance.domain.repository.NewsRepository
import com.example.newsfinance.util.Result
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetNewsByCategoryUseCase @Inject constructor(
    private val newsRepository: NewsRepository
) {
    operator fun invoke(category: String, country: String?): Flow<Result<List<Article>>> =
        newsRepository.getTopHeadlines(country = country, category = category)
}
