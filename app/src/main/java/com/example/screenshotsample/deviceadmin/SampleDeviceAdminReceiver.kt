package com.example.screenshotsample.deviceadmin

import android.accessibilityservice.AccessibilityService
import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import com.example.screenshotsample.MainActivity
import com.example.screenshotsample.accessibilityservice.SampleAccessibilityService
import com.example.screenshotsample.accessibilityservice.SampleAccessibilityService.Companion.GLOBAL_ACTION_TYPE

class SampleDeviceAdminReceiver : DeviceAdminReceiver() {

    /**
     * This is one possible way to prevent the device admin functionality being disabled. Whenever
     * the user attempts to disable device admin for this app, a faked back key press will be invoked
     * which should then close the settings page (or at least prevent the uninstall proceedging).
     * This relies on the accessibility service being active as this is required to perform the fake
     * back key press.
     */
    override fun onDisableRequested(context: Context, intent: Intent): CharSequence {

        val runServiceIntent = Intent(
            context,
            SampleAccessibilityService::class.java
        )
        runServiceIntent.putExtra(
            GLOBAL_ACTION_TYPE,
            AccessibilityService.GLOBAL_ACTION_BACK
        )
        context.startService(runServiceIntent)

        return "Some text that would normally appear in a dialog"
    }

}