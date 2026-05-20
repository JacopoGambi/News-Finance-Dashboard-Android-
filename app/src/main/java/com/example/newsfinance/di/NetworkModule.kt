package com.example.newsfinance.di

import com.example.newsfinance.data.remote.api.CoinGeckoService
import com.example.newsfinance.data.remote.api.NewsApiService
import com.example.newsfinance.util.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

/**
 * Qualifier per distinguere le due istanze Retrofit (NewsAPI e CoinGecko).
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class NewsRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class CoinGeckoRetrofit

/**
 * Modulo Hilt per il layer di rete.
 * Fornisce OkHttpClient condiviso, due Retrofit (NewsAPI / CoinGecko) e i relativi service.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    @Provides
    @Singleton
    @NewsRetrofit
    fun provideNewsRetrofit(okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(Constants.NEWS_API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    @CoinGeckoRetrofit
    fun provideCoinGeckoRetrofit(okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(Constants.COINGECKO_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideNewsApiService(@NewsRetrofit retrofit: Retrofit): NewsApiService =
        retrofit.create(NewsApiService::class.java)

    @Provides
    @Singleton
    fun provideCoinGeckoService(@CoinGeckoRetrofit retrofit: Retrofit): CoinGeckoService =
        retrofit.create(CoinGeckoService::class.java)
}
