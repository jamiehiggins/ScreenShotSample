package com.example.screenshotsample.accessibilityservice

import android.accessibilityservice.AccessibilityService
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.accessibility.AccessibilityEvent

class SampleAccessibilityService: AccessibilityService() {

    companion object {

        private const val PERFORM_GLOBAL_ACTION = "com.example.screenshotsample.PERFORM_GLOBAL_ACTION"
        private const val GLOBAL_ACTION_TYPE = "global_action_type"

        fun takeScreenShot(context: Context) {
            val runServiceIntent = Intent(context, SampleAccessibilityService::class.java)
            runServiceIntent.action = PERFORM_GLOBAL_ACTION
            runServiceIntent.putExtra(GLOBAL_ACTION_TYPE, GLOBAL_ACTION_TAKE_SCREENSHOT)
            context.startService(runServiceIntent)
        }
    }

    override fun onInterrupt() {}

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val extras = intent.extras
        val commandType = extras?.getInt(GLOBAL_ACTION_TYPE, -1) ?: - 1
        if (commandType != -1) {
            performGlobalAction(commandType)
        }
        stopSelf()
        return Service.START_NOT_STICKY
    }
}