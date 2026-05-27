package com.example.newsfinance.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Modulo Hilt per WorkManager.
 * Il binding dei worker annotati con @HiltWorker è generato automaticamente da hilt-work.
 * L'Application implementa Configuration.Provider iniettando HiltWorkerFactory.
 */
@Module
@InstallIn(SingletonComponent::class)
object WorkerModule
