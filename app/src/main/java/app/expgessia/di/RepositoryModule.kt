// di/RepositoryModule.kt
package app.expgessia.di

import app.expgessia.data.repository.CharacteristicRepositoryImpl
import app.expgessia.data.repository.TaskRepositoryImpl
import app.expgessia.data.repository.UserRepositoryImpl
import app.expgessia.domain.repository.CharacteristicRepository
import app.expgessia.domain.repository.TaskRepository
import app.expgessia.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository

    @Singleton
    @Binds
    abstract fun bindCharacteristicRepository(
        characteristicRepositoryImpl: CharacteristicRepositoryImpl
    ): CharacteristicRepository

    @Singleton
    @Binds
    abstract fun bindTaskRepository(
        taskRepositoryImpl: TaskRepositoryImpl
    ): TaskRepository
}