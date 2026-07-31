package com.pashu360.app.feature.notifications.system

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Thin injectable wrapper so ViewModels can trigger the scanner without
 * knowing about Android Application context directly. */
@Singleton
class AlertScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun scanNow() = AlertScannerWorker.triggerNow(context)
}
