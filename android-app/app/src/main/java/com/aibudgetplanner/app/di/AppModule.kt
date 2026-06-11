package com.aibudgetplanner.app.di

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.aibudgetplanner.app.ai.SpendingPredictor
import com.aibudgetplanner.app.ai.TFLiteSpendingPredictor
import com.aibudgetplanner.app.data.local.AppDatabase
import com.aibudgetplanner.app.data.local.dao.ExpenseDao
import com.aibudgetplanner.app.data.local.dao.FixedExpenseDao
import com.aibudgetplanner.app.data.local.dao.PendingSyncOperationDao
import com.aibudgetplanner.app.data.local.dao.UserProfileDao
import com.aibudgetplanner.app.data.repository.BudgetRepository
import com.aibudgetplanner.app.data.repository.PendingSyncRepository
import com.aibudgetplanner.app.data.sync.SyncScheduler
import com.aibudgetplanner.app.domain.usecase.BudgetEngineUseCase
import com.aibudgetplanner.app.security.DatabasePassphraseProvider
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.sqlcipher.database.SupportFactory
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "planner_preferences")

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        databasePassphraseProvider: DatabasePassphraseProvider
    ): AppDatabase {
        val passphrase = databasePassphraseProvider.providePassphrase()
        val factory = SupportFactory(passphrase)
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "ai_budget_planner.db"
        )
            .openHelperFactory(factory)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    fun provideUserProfileDao(database: AppDatabase): UserProfileDao = database.userProfileDao()

    @Provides
    fun provideFixedExpenseDao(database: AppDatabase): FixedExpenseDao = database.fixedExpenseDao()

    @Provides
    fun provideExpenseDao(database: AppDatabase): ExpenseDao = database.expenseDao()

    @Provides
    fun providePendingSyncOperationDao(database: AppDatabase): PendingSyncOperationDao =
        database.pendingSyncOperationDao()

    @Provides
    @Singleton
    fun provideSpendingPredictor(
        predictor: TFLiteSpendingPredictor
    ): SpendingPredictor = predictor

    @Provides
    @Singleton
    fun provideBudgetRepository(
        userProfileDao: UserProfileDao,
        fixedExpenseDao: FixedExpenseDao,
        expenseDao: ExpenseDao,
        budgetEngineUseCase: BudgetEngineUseCase,
        pendingSyncRepository: PendingSyncRepository,
        syncScheduler: SyncScheduler
    ): BudgetRepository = BudgetRepository(
        userProfileDao,
        fixedExpenseDao,
        expenseDao,
        budgetEngineUseCase,
        pendingSyncRepository,
        syncScheduler
    )
}
