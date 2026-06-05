# News & Finance Dashboard

Applicazione Android che riunisce in un'unica dashboard mobile le **notizie di attualità** e il **monitoraggio in tempo reale delle principali criptovalute**.

Progetto d'esame per il corso *Laboratorio di Programmazione di Sistemi Mobili* (a.a. 2025/26) — Università di Bologna, Corso di Laurea in Tecnologie dei Sistemi Informatici.

> **Nota** La fase di brainstorming e progettazione dell'app è stata svolta con l'ausilio di strumenti di intelligenza artificiale.

---

## Scopo dell'applicazione

L'app offre all'utente un punto unico di accesso a due tipi di informazione spesso correlati — l'attualità e i mercati crypto — permettendo di:

- consultare un **feed di notizie** aggiornate, filtrabili per categoria e ricercabili per parola chiave;
- monitorare i **prezzi delle criptovalute**, con variazione nelle 24 ore, capitalizzazione e mini-grafico di andamento;
- salvare articoli e criptovalute tra i **preferiti** in modo persistente;
- ricevere **notifiche** quando il prezzo di una crypto supera una soglia impostata, anche con app chiusa;
- personalizzare i contenuti in base alla **posizione geografica** (notizie locali) e alle proprie preferenze (lingua, valuta, tema).

---

## Funzionalità e schermate

| Schermata | Descrizione |
|-----------|-------------|
| **Home** | Dashboard di riepilogo: card "hero" con andamento aggregato del mercato crypto (capitalizzazione totale, variazione media, sparkline), ultime notizie e top crypto. Pull-to-refresh. Chip con il paese geolocalizzato. |
| **News** | Lista articoli da GNews, filtro per categoria (generale, business, tecnologia, sport, ecc.) e per **notizie locali**, ricerca per parola chiave, salvataggio nei preferiti. |
| **Mercati** | Elenco criptovalute con prezzo, variazione 24h, mini-grafico per riga; selezione valuta (USD/EUR); aggiunta alla watchlist e impostazione soglie di notifica. |
| **Dettaglio crypto** | Grafico storico del prezzo con intervalli temporali selezionabili, gestione degli alert impostati. |
| **Preferiti** | Articoli e crypto salvati, organizzati in due tab; rimozione tramite swipe-to-dismiss. |
| **Impostazioni** | Scelta del tema (sistema / chiaro / scuro), lingua delle notizie, valuta di riferimento, attivazione notifiche e intervallo di aggiornamento in background. |

Tutte le schermate gestiscono esplicitamente gli stati di **caricamento**, **errore** (con possibilità di riprovare) e **lista vuota**.


---

## Stack tecnologico

| Categoria | Tecnologia |
|-----------|-----------|
| Linguaggio | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Architettura | MVVM (Model–View–ViewModel) |
| Dependency Injection | Hilt |
| Networking | Retrofit + OkHttp + Gson |
| Asincronia | Coroutines + Flow / StateFlow |
| Storage locale | Room + DataStore Preferences |
| Background | WorkManager |
| Navigazione | Navigation Compose |
| Immagini | Coil |
| Permessi | Accompanist Permissions |
| Posizione | Play Services Location |

- **minSdk** 26 (Android 8.0) · **targetSdk / compileSdk** 35 · **JDK** 17

---

## Architettura

L'applicazione segue il pattern **MVVM** con una netta separazione in tre layer:

```
┌─────────────────────────────────────────────────────┐
│  UI (Jetpack Compose)                                │
│  Screen @Composable  ←→  ViewModel (StateFlow)       │
└───────────────────────────┬─────────────────────────┘
                            │  (Use Case)
┌───────────────────────────┴─────────────────────────┐
│  DOMAIN                                              │
│  Model · Repository (interfacce) · UseCase           │
└───────────────────────────┬─────────────────────────┘
                            │
┌───────────────────────────┴─────────────────────────┐
│  DATA                                                │
│  Remote (Retrofit + DTO)   Local (Room + DataStore)  │
│            └──── Repository (implementazioni) ───┘   │
└─────────────────────────────────────────────────────┘
```

Principi seguiti:

- Il **ViewModel** non importa classi del framework Android (Context, View): le dipendenze sono iniettate tramite **Hilt**.
- I **repository** sono l'unica fonte di dati per il dominio (*single source of truth*); espongono modelli di dominio, mai DTO o Entity.
- I **DTO** (risposta JSON) vivono solo nel layer `data/remote` e vengono mappati verso i **modelli di dominio**; le **Entity** Room solo nel layer `data/local`.
- La gestione degli errori di rete è uniformata da una sealed class **`Result<T>`** (`Success` / `Error`), così da rappresentare in modo esplicito gli stati di successo ed errore fino all'UI.
- Lo stato di ogni schermata è un **data class immutabile** esposto come `StateFlow`, con campi dedicati a `isLoading`, `error` e ai dati.

---

## Struttura dei package

```
com.example.newsfinance/
├── NewsFinanceApplication.kt      # Application class (Hilt + WorkManager)
├── MainActivity.kt                # Host Compose, tema, bottom navigation
│
├── data/
│   ├── local/
│   │   ├── dao/                    # ArticleDao, CryptoDao, AlertDao
│   │   ├── entity/                 # ArticleEntity, CryptoEntity, AlertEntity
│   │   ├── AppDatabase.kt          # Database Room
│   │   └── UserPreferencesDataStore.kt
│   ├── remote/
│   │   ├── api/                    # NewsApiService, CoinGeckoService (Retrofit)
│   │   └── dto/                    # NewsDto, CryptoDto, CryptoChartDto
│   └── repository/                 # Implementazioni dei repository
│
├── domain/
│   ├── model/                      # Article, Crypto, CryptoAlert
│   ├── repository/                 # Interfacce dei repository
│   └── usecase/                    # GetHomeDataUseCase, GetCryptoMarketsUseCase,
│                                   # GetNewsByCategoryUseCase, GetLocalNewsUseCase,
│                                   # SearchNewsUseCase
│
├── ui/
│   ├── home/                       # HomeScreen, HomeViewModel
│   ├── news/                       # NewsScreen, NewsViewModel
│   ├── markets/                    # MarketsScreen, MarketsViewModel,
│   │                               # CryptoDetailScreen, CryptoDetailViewModel
│   ├── favorites/                  # FavoritesScreen, FavoritesViewModel
│   ├── settings/                   # SettingsScreen, SettingsViewModel
│   ├── components/                 # ArticleCard, CryptoCard, MarketSummaryCard,
│   │                               # ChangePill, Sparkline, AddAlertDialog,
│   │                               # LoadingState, ErrorState, EmptyState
│   ├── navigation/                 # Screen (rotte), NavGraph
│   └── theme/                      # Theme, Color, Typography
│
├── worker/                         # PriceAlertWorker, PriceAlertScheduler
├── di/                             # AppModule, DatabaseModule, NetworkModule, WorkerModule
└── util/                           # Constants, Result, CurrencyFormatter,
                                    # LocationHelper, NotificationHelper
```

---

## API remote

| API | Endpoint principale | Uso | Autenticazione |
|-----|---------------------|-----|----------------|
| [**GNews**](https://gnews.io) | `GET /api/v4/top-headlines`, `GET /api/v4/search` | Feed notizie, notizie per categoria, ricerca, notizie locali | Chiave API (tier gratuito) |
| [**CoinGecko**](https://www.coingecko.com/en/api) | `GET /coins/markets`, `GET /coins/{id}/market_chart` | Prezzi, variazioni 24h, capitalizzazione, sparkline, grafico storico | Nessuna (tier gratuito) |

Le risposte JSON sono deserializzate con Gson in DTO dedicati e mappate verso i modelli di dominio. La rete è gestita da un client OkHttp con logging in debug; gli errori (connettività, codici HTTP, parsing) sono incapsulati in `Result<T>`.

---

## Gestione delle chiavi API

Per consentire l'**avvio immediato dopo il clone** del repository, la chiave necessaria a GNews è versionata in `config/apikeys.properties`. Per permettere comunque un override locale, la risoluzione segue questa priorità:

1. valore presente in `local.properties` (non versionato, ha la precedenza);
2. in alternativa, valore in `config/apikeys.properties` (versionato);
3. stringa vuota se nessuno dei due è presente.

La risoluzione avviene in `app/build.gradle.kts` e le chiavi vengono esposte al codice tramite `BuildConfig`, senza alcun valore hardcodato nel sorgente. CoinGecko, nel tier gratuito, non richiede chiave.


---

## Persistenza dei dati

- **Room** gestisce le tabelle dei preferiti (`ArticleEntity`, `CryptoEntity`) e delle soglie di notifica (`AlertEntity`), con i relativi DAO. Le liste reattive (es. i preferiti) sono esposte come `Flow<List<…>>`, così che l'UI si aggiorni automaticamente a ogni modifica.
- **DataStore Preferences** (`UserPreferencesDataStore`) memorizza le preferenze utente: tema, lingua, valuta, abilitazione notifiche e intervallo di aggiornamento.

In entrambi i casi i dati persistiti vengono mappati verso i modelli di dominio prima di raggiungere i ViewModel.

---

## Background e notifiche

- **`PriceAlertWorker`** è un `CoroutineWorker` schedulato come `PeriodicWorkRequest` con vincolo di rete (`NetworkType.CONNECTED`).
- A ogni esecuzione confronta i prezzi correnti con le soglie salvate in Room e, al superamento, invia una notifica tramite `NotificationHelper` su un canale dedicato.
- La schedulazione è gestita da `PriceAlertScheduler` e l'intervallo è configurabile dalle Impostazioni (15 / 30 / 60 minuti).
- L'integrazione di Hilt con WorkManager è realizzata tramite `WorkerModule` e l'inizializzatore personalizzato nell'`Application`.

---

## Permessi e geolocalizzazione

- Il permesso `ACCESS_COARSE_LOCATION` è richiesto a runtime in modo *lazy*, solo quando l'utente attiva le notizie locali o all'avvio della Home.
- La posizione è ottenuta tramite `FusedLocationProviderClient`; da essa si ricava il codice paese (Geocoder) per filtrare le notizie.
- Il caso di permesso negato è gestito con un messaggio esplicativo e un fallback, senza interrompere il resto dell'esperienza.
- Il permesso `POST_NOTIFICATIONS` è richiesto all'attivazione delle notifiche (necessario su Android 13+).

---

## Interfaccia utente

- UI interamente in **Jetpack Compose** con **Material 3**: nessuna logica di business nei Composable, componenti riutilizzabili in `ui/components`.
- **Tema chiaro e scuro** completi, con palette di brand coerente; la scelta del tema (sistema / chiaro / scuro) è persistita e applicata all'avvio.
- I valori finanziari (prezzi, variazioni, soglie) usano una **famiglia monospazio** per mantenere le cifre allineate; le variazioni percentuali sono mostrate con un badge colorato (`ChangePill`) corredato di freccia, così da non affidare il significato al solo colore.
- **Navigazione** a 5 tab tramite bottom navigation (`NavGraph` + Navigation Compose), con stato preservato tra le schermate.
- **Multilingua**: interfaccia disponibile in italiano, inglese, spagnolo e francese (`localeConfig` + per-app language).
- **Icona** adattiva dedicata.

---

## Setup e avvio

**Requisiti**

- Android Studio (Ladybug o successivo)
- JDK 17
- Android SDK con API level 35
- Emulatore o dispositivo con Android 8.0+ (API 26)

**Passi**

1. Clonare il repository.
2. Aprire il progetto in Android Studio (genera automaticamente `local.properties` con il percorso dell'SDK).
3. Sincronizzare Gradle ed eseguire l'app.

Non è richiesta alcuna configurazione manuale delle chiavi API: il progetto è eseguibile direttamente dopo il clone.

---

## Autori

Progetto realizzato da Jacopo Gambi e Martina Conficconi del Corso di Laurea in Tecnologie dei Sistemi Informatici, Università di Bologna — a.a. 2025/26.
