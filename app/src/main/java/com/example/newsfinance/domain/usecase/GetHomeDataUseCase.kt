package com.example.newsfinance.domain.usecase

import com.example.newsfinance.domain.model.Article
import com.example.newsfinance.domain.model.Crypto
import com.example.newsfinance.domain.repository.CryptoRepository
import com.example.newsfinance.domain.repository.NewsRepository
import com.example.newsfinance.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class GetHomeDataUseCase @Inject constructor(
    private val newsRepository: NewsRepository,
    private val cryptoRepository: CryptoRepository
) {
    operator fun invoke(country: String): Flow<Pair<Result<List<Article>>, Result<List<Crypto>>>> =
        combine(
            newsRepository.getTopHeadlines(country = country, category = null),
            cryptoRepository.getMarkets(vsCurrency = "usd")
        ) { newsResult, cryptoResult ->
            Pair(newsResult, cryptoResult)
        }
}
