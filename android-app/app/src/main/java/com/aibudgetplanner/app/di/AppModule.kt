package com.aibudgetplanner.app.di

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.aibudgetplanner.app.data.local.AppDatabase
import com.aibudgetplanner.app.data.local.dao.ExpenseDao
import com.aibudgetplanner.app.data.local.dao.FixedExpenseDao
import com.aibudgetplanner.app.data.local.dao.UserProfileDao
import com.aibudgetplanner.app.data.repository.BudgetRepository
import com.aibudgetplanner.app.domain.usecase.BudgetEngineUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "planner_preferences")

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "ai_budget_planner.db"
        ).build()
    }

    @Provides
    fun provideUserProfileDao(database: AppDatabase): UserProfileDao = database.userProfileDao()

    @Provides
    fun provideFixedExpenseDao(database: AppDatabase): FixedExpenseDao = database.fixedExpenseDao()

    @Provides
    fun provideExpenseDao(database: AppDatabase): ExpenseDao = database.expenseDao()

    @Provides
    @Singleton
    fun provideBudgetRepository(
        userProfileDao: UserProfileDao,
        fixedExpenseDao: FixedExpenseDao,
        expenseDao: ExpenseDao,
        budgetEngineUseCase: BudgetEngineUseCase
    ): BudgetRepository = BudgetRepository(userProfileDao, fixedExpenseDao, expenseDao, budgetEngineUseCase)
}
