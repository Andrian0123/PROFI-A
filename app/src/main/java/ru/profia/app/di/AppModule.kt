package ru.profia.app.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ru.profia.app.data.local.AppDatabase
import ru.profia.app.data.local.MIGRATION_1_2
import ru.profia.app.data.local.MIGRATION_2_3
import ru.profia.app.data.local.MIGRATION_3_4
import ru.profia.app.data.local.MIGRATION_4_5
import ru.profia.app.data.local.dao.IntermediateEstimateActDao
import ru.profia.app.data.local.dao.OpeningDao
import ru.profia.app.data.local.dao.ProjectDao
import ru.profia.app.data.local.dao.RoomDao
import ru.profia.app.data.local.dao.RoomScanDao
import ru.profia.app.data.local.dao.RoomWorkItemDao
import ru.profia.app.data.local.datastore.PreferencesDataStore
import ru.profia.app.data.remote.DefaultPurchaseVerificationApi
import ru.profia.app.data.remote.PurchaseVerificationApi
import ru.profia.app.data.remote.DefaultAuthAccountApi
import ru.profia.app.data.remote.AuthAccountApi
import ru.profia.app.data.remote.DefaultScanProcessingApi
import ru.profia.app.data.remote.ScanProcessingApi
import ru.profia.app.data.repository.PreferencesRepository
import ru.profia.app.data.repository.ProjectRepository
import ru.profia.app.data.repository.RoomScanRepository
import ru.profia.app.data.repository.AuthAccountRepository
import ru.profia.app.data.repository.ScanProcessingRepository
import ru.profia.app.data.remote.DefaultSupportApi
import ru.profia.app.data.remote.SupportApi
import ru.profia.app.data.repository.SupportRepository
import ru.profia.app.data.repository.SubscriptionRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "profia_database"
        ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5).build()

    @Provides
    @Singleton
    fun provideProjectDao(database: AppDatabase): ProjectDao = database.projectDao()

    @Provides
    @Singleton
    fun provideRoomDao(database: AppDatabase): RoomDao = database.roomDao()

    @Provides
    @Singleton
    fun provideOpeningDao(database: AppDatabase): OpeningDao = database.openingDao()

    @Provides
    @Singleton
    fun provideRoomScanDao(database: AppDatabase): RoomScanDao = database.roomScanDao()

    @Provides
    @Singleton
    fun provideRoomWorkItemDao(database: AppDatabase): RoomWorkItemDao = database.roomWorkItemDao()

    @Provides
    @Singleton
    fun provideIntermediateEstimateActDao(database: AppDatabase): IntermediateEstimateActDao = database.intermediateEstimateActDao()

    @Provides
    @Singleton
    fun providePreferencesDataStore(@ApplicationContext context: Context): PreferencesDataStore =
        PreferencesDataStore(context)

    @Provides
    @Singleton
    fun providePurchaseVerificationApi(api: DefaultPurchaseVerificationApi): PurchaseVerificationApi = api

    @Provides
    @Singleton
    fun provideAuthAccountApi(api: DefaultAuthAccountApi): AuthAccountApi = api

    @Provides
    @Singleton
    fun provideScanProcessingApi(api: DefaultScanProcessingApi): ScanProcessingApi = api

    @Provides
    @Singleton
    fun provideSupportApi(api: DefaultSupportApi): SupportApi = api

    @Provides
    @Singleton
    fun provideSubscriptionRepository(
        preferencesDataStore: PreferencesDataStore,
        purchaseVerificationApi: PurchaseVerificationApi
    ): SubscriptionRepository =
        SubscriptionRepository(preferencesDataStore, purchaseVerificationApi)

    @Provides
    @Singleton
    fun providePreferencesRepository(preferencesDataStore: PreferencesDataStore): PreferencesRepository =
        PreferencesRepository(preferencesDataStore)

    @Provides
    @Singleton
    fun provideProjectRepository(
        projectDao: ProjectDao,
        roomDao: RoomDao,
        openingDao: OpeningDao,
        roomWorkItemDao: RoomWorkItemDao,
        intermediateEstimateActDao: IntermediateEstimateActDao
    ): ProjectRepository = ProjectRepository(projectDao, roomDao, openingDao, roomWorkItemDao, intermediateEstimateActDao)

    @Provides
    @Singleton
    fun provideRoomScanRepository(roomScanDao: RoomScanDao): RoomScanRepository =
        RoomScanRepository(roomScanDao)

    @Provides
    @Singleton
    fun provideScanProcessingRepository(
        scanProcessingApi: ScanProcessingApi
    ): ScanProcessingRepository = ScanProcessingRepository(scanProcessingApi)

    @Provides
    @Singleton
    fun provideAuthAccountRepository(
        authAccountApi: AuthAccountApi,
        preferencesDataStore: PreferencesDataStore
    ): AuthAccountRepository = AuthAccountRepository(authAccountApi, preferencesDataStore)

    @Provides
    @Singleton
    fun provideSupportRepository(
        supportApi: SupportApi
    ): SupportRepository = SupportRepository(supportApi)
}
