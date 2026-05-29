package com.example.newsfinance.di

import com.example.newsfinance.data.repository.AlertRepositoryImpl
import com.example.newsfinance.data.repository.CryptoRepositoryImpl
import com.example.newsfinance.data.repository.FavoritesRepositoryImpl
import com.example.newsfinance.data.repository.NewsRepositoryImpl
import com.example.newsfinance.domain.repository.AlertRepository
import com.example.newsfinance.domain.repository.CryptoRepository
import com.example.newsfinance.domain.repository.FavoritesRepository
import com.example.newsfinance.domain.repository.NewsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Modulo Hilt che collega le interfacce repository del layer domain
 * alle implementazioni nel layer data.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindNewsRepository(impl: NewsRepositoryImpl): NewsRepository

    @Binds
    @Singleton
    abstract fun bindCryptoRepository(impl: CryptoRepositoryImpl): CryptoRepository

    @Binds
    @Singleton
    abstract fun bindFavoritesRepository(impl: FavoritesRepositoryImpl): FavoritesRepository

    @Binds
    @Singleton
    abstract fun bindAlertRepository(impl: AlertRepositoryImpl): AlertRepository
}
