/**
 * WatchApp.kt — Application entry point for the Wear OS Tasks app.
 *
 * This is the first class instantiated when the app process starts.
 * It initializes the two core subsystems:
 *   1. **Notifications** — Creates the notification channel, reschedules pending
 *      reminders (needed after force-stop or reboot).
 *   2. **Sync** — Schedules a periodic WorkManager job ([SyncWorker]) and starts
 *      the [DataLayerSyncManager] listener so real-time DataItem changes from the
 *      phone are picked up immediately.
 *
 * Timber debug logging is planted only in DEBUG builds.
 */
package org.tasks

import android.app.Application
import org.tasks.data.sync.DataLayerSyncManager
import org.tasks.data.sync.SyncWorker
import org.tasks.notifications.WearNotificationManager
import timber.log.Timber

class WatchApp : Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        // Initialize notification channel
        initNotifications()

        // Initialize sync infrastructure
        initSync()
    }

    /**
     * Set up the notification system:
     * - Create the Android notification channel (required on API 26+).
     * - Reschedule any reminders that were lost when the app was killed.
     */
    private fun initNotifications() {
        val notificationManager = WearNotificationManager.getInstance(this)
        notificationManager.createNotificationChannel()
        // Reschedule any pending reminders (in case app was force-stopped)
        notificationManager.rescheduleAll()
        Timber.d("Wear notification system initialized")
    }

    /**
     * Set up the sync system:
     * - Schedule the periodic [SyncWorker] (WorkManager).
     * - Start the [DataLayerSyncManager] so we receive DataItem events in real-time.
     */
    private fun initSync() {
        // Schedule periodic sync worker
        SyncWorker.schedule(this)

        // Start listening for data changes from phone
        DataLayerSyncManager.getInstance(this).startListening()

        Timber.d("Sync infrastructure initialized")
    }
}
