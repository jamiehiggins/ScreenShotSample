package com.example.screenshotsample.accessibilityservice

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.provider.Settings
import android.provider.Settings.SettingNotFoundException
import android.text.TextUtils.SimpleStringSplitter
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager

object Util {

    fun getLastPhoto(context: Context): Uri? {

        // Get the last picture that was taken
        val contentResolver = context.applicationContext.contentResolver
        val projection = arrayOf(
                MediaStore.Images.ImageColumns._ID,
                MediaStore.Images.ImageColumns.DATE_TAKEN,
                MediaStore.Images.ImageColumns.DATA
        )
        val cursor = contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, MediaStore.Images.ImageColumns.DATE_TAKEN + "<=?", arrayOf((System.currentTimeMillis() + 60000).toString()), projection[1] + " DESC")

        var photoUri: Uri? = null
        return if (cursor == null) {
            null
        } else {
            if (cursor.moveToFirst()) {
                val id = cursor.getInt(0)
                photoUri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "$id")
            }
            cursor.close()
            photoUri
        }
    }

    fun isAccessibilityEnabled(context: Context, service: String): Boolean {
        // I use two mechanisms for this, I can't remember the exact details but I know I used to get reports from
        // a few users who would be constantly told accessibility was disabeld when it was actually enabled.
        return isAccessibilityEnabledMechanism1(context, service) || isAccessibilityEnabledMechanism2(context, service)
    }

    private fun isAccessibilityEnabledMechanism1(context: Context, service: String): Boolean {
        try {
            val accessibilityFound = false
            val accessibilityEnabled = Settings.Secure.getInt(context.contentResolver, Settings.Secure.ACCESSIBILITY_ENABLED)

            if (accessibilityEnabled == 1) {
                val enabledServices = Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)

                enabledServices?.split(":")?.forEach { enabledService ->
                    if (enabledService.endsWith(service)) {
                        return true
                    }
                }
            }
            return accessibilityFound
        } catch (ignored: SettingNotFoundException) {
            return false
        }
    }

    private fun isAccessibilityEnabledMechanism2(context: Context, service: String): Boolean {
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val runningServices = am.getEnabledAccessibilityServiceList(AccessibilityEvent.TYPES_ALL_MASK)
        runningServices.forEach { runningService ->
            if (runningService.id.endsWith(service)) {
                return true
            }
        }
        return false
    }
}