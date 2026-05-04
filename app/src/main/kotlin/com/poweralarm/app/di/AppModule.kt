package com.poweralarm.app.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.poweralarm.core.data.AlarmRepositoryImpl
import com.poweralarm.core.data.db.PowerAlarmDb
import com.poweralarm.core.domain.port.AlarmRepository
import com.poweralarm.core.scheduler.AlarmScheduler
import com.poweralarm.core.scheduler.NextFireCalculator
import com.poweralarm.core.settings.InMemorySettingsRegistry
import com.poweralarm.core.settings.RegistrySeed
import com.poweralarm.core.settings.SettingsRegistry
import com.poweralarm.core.settings.SettingsStore
import com.poweralarm.core.ui.theme.ThemeRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "power_alarm_prefs")

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides @Singleton
    fun provideRegistry(): SettingsRegistry = RegistrySeed.build()

    @Provides @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> = context.dataStore

    @Provides @Singleton
    fun provideSettingsStore(ds: DataStore<Preferences>, registry: SettingsRegistry): SettingsStore =
        SettingsStore(ds, registry)

    @Provides @Singleton
    fun provideThemeRepository(store: SettingsStore): ThemeRepository = ThemeRepository(store)

    @Provides @Singleton
    fun provideDb(@ApplicationContext context: Context): PowerAlarmDb =
        Room.databaseBuilder(context, PowerAlarmDb::class.java, PowerAlarmDb.NAME).build()

    @Provides @Singleton
    fun provideAlarmRepository(db: PowerAlarmDb): AlarmRepository = AlarmRepositoryImpl(db.alarmDao())

    @Provides @Singleton
    fun provideNextFireCalculator(): NextFireCalculator = NextFireCalculator()

    @Provides @Singleton
    fun provideAlarmScheduler(@ApplicationContext context: Context, calc: NextFireCalculator): AlarmScheduler =
        AlarmScheduler(context, calc)
}
