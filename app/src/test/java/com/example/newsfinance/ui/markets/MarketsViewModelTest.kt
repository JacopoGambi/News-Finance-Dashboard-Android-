package com.example.newsfinance.ui.markets

import com.example.newsfinance.FakeAlertRepository
import com.example.newsfinance.FakeCryptoRepository
import com.example.newsfinance.FakeFavoritesRepository
import com.example.newsfinance.FakeUserPreferencesDataStore
import com.example.newsfinance.MainDispatcherRule
import com.example.newsfinance.data.local.UserPreferences
import com.example.newsfinance.domain.model.Crypto
import com.example.newsfinance.domain.usecase.GetCryptoMarketsUseCase
import com.example.newsfinance.util.Result
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class MarketsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val sampleCrypto = Crypto(
        id = "bitcoin",
        symbol = "BTC",
        name = "Bitcoin",
        imageUrl = null,
        currentPrice = 50000.0,
        marketCap = null,
        marketCapRank = 1,
        priceChangePercentage24h = 2.0,
        lastUpdated = null
    )

    private fun viewModel(marketsResult: Result<List<Crypto>>, currency: String) =
        MarketsViewModel(
            GetCryptoMarketsUseCase(FakeCryptoRepository(marketsResult)),
            FakeFavoritesRepository(),
            FakeAlertRepository(),
            FakeUserPreferencesDataStore(UserPreferences(preferredCurrency = currency))
        )

    @Test
    fun `uiState exposes cryptos and currency from preferences on success`() = runTest {
        val vm = viewModel(Result.Success(listOf(sampleCrypto)), currency = "eur")

        val state = vm.uiState.first { !it.isLoading }

        assertEquals(1, state.cryptos.size)
        assertEquals("bitcoin", state.cryptos.first().id)
        assertEquals("eur", state.selectedCurrency)
    }

    @Test
    fun `uiState surfaces error message on failure`() = runTest {
        val vm = viewModel(Result.Error("boom"), currency = "usd")

        val state = vm.uiState.first { it.error != null }

        assertEquals("boom", state.error)
    }
}
