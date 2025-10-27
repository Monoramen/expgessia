package app.expgessia.domain.model

import androidx.annotation.StringRes
import app.expgessia.R

data class Task(
    val id: Long,
    val title: String,
    val description: String,
    val characteristicId: Int,
    val repeatMode: RepeatMode = RepeatMode.NONE,
    val repeatDetails: String? = null,
    val xpReward: Int,
    val isCompleted: Boolean = false,
    val scheduledFor: Long? = null,
)
enum class RepeatMode(@StringRes val stringResId: Int) {
    NONE(R.string.repeat_mode_none),
    DAILY(R.string.repeat_mode_daily),
    WEEKLY(R.string.repeat_mode_weekly),
    MONTHLY(R.string.repeat_mode_monthly),
    YEARLY(R.string.repeat_mode_yearly);
}