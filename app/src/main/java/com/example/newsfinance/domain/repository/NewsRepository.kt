package com.example.newsfinance.domain.repository

import com.example.newsfinance.domain.model.Article
import com.example.newsfinance.util.Result
import kotlinx.coroutines.flow.Flow

interface NewsRepository {

    fun getTopHeadlines(country: String?, category: String?): Flow<Result<List<Article>>>

    /**
     * Ricerca articoli per keyword.
     * @param country se valorizzato (notizie locali) la ricerca è filtrata per paese
     *   e usa la lingua di quel paese; se null usa la lingua scelta dall'utente.
     */
    fun searchNews(query: String, country: String? = null): Flow<Result<List<Article>>>
}
