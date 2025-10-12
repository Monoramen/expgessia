// di/ViewModelModule.kt
package app.expgessia.di

import app.expgessia.domain.repository.UserRepository
import app.expgessia.presentation.viewmodel.UserViewModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object ViewModelModule {

    @Provides
    @ViewModelScoped
    fun provideUserViewModel(userRepository: UserRepository): UserViewModel {
        return UserViewModel(userRepository)
    }
}