package com.tobiasdroste.papercups.app.printers

import android.content.Context
import androidx.room.Room
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Singleton
    @Binds
    abstract fun bindPrinterRepository(repository: DefaultPrinterRepository): PrinterRepository
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Singleton
    @Provides
    fun provideDataBase(@ApplicationContext context: Context): PrinterDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            PrinterDatabase::class.java,
            "Printers.db"
        ).build()
    }

    @Provides
    fun providePrinterDao(database: PrinterDatabase): PrinterDao = database.printerDao()
}