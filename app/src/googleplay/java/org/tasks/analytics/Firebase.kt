package org.tasks.analytics

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import org.tasks.R
import org.tasks.preferences.Preferences
import org.tasks.time.DateTimeUtils2.currentTimeMillis
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Firebase @Inject constructor(
    @ApplicationContext val context: Context,
    private val preferences: Preferences
) {
    fun reportException(t: Throwable) = Timber.e(t)

    fun updateRemoteConfig() {}

    fun logEvent(event: Int, vararg params: Pair<Int, Any>) {
        Timber.d("${context.getString(event)} -> ${params.associate { context.getString(it.first) to it.second }}")
    }

    fun logEventOncePerDay(event: Int, vararg params: Pair<Int, Any>) {
        logEvent(event, *params)
    }

    fun addTask(source: String) =
        logEvent(R.string.event_add_task, R.string.param_type to source)

    fun completeTask(source: String) =
        logEvent(R.string.event_complete_task, R.string.param_type to source)

    fun reportIabResult(result: String, sku: String, state: String, orderId: String) {
        logEvent(
            R.string.event_purchase_result,
            R.string.param_sku to sku,
            R.string.param_result to result,
            R.string.param_state to state,
            R.string.param_order_id to orderId,
        )
    }

    private val installCooldown: Boolean
        get() = preferences.installDate + days(7L) > currentTimeMillis()

    val reviewCooldown: Boolean
        get() = installCooldown || preferences.lastReviewRequest + days(30L) > currentTimeMillis()

    val subscribeCooldown: Boolean
        get() = installCooldown
                || preferences.lastSubscribeRequest + days(28L) > currentTimeMillis()

    private fun days(default: Long): Long =
        TimeUnit.DAYS.toMillis(default)

    fun getTosVersion(): Int =
        context.resources.getInteger(R.integer.default_tos_version)

    @Suppress("RedundantSuspendModifier")
    suspend fun getToken(): String? = null
}
