// ExpgessiaApplication.kt
package app.expgessia

import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner
import app.expgessia.utils.AppTimeTracker
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class ExpgessiaApplication : Application() {
    @Inject lateinit var appTimeTracker: AppTimeTracker
    
    override fun onCreate() {
        super.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(appTimeTracker)
    }

}