package com.example.screenshotsample.accessibilityservice

import android.accessibilityservice.AccessibilityService
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.screenshotsample.BlockSettingsActivity

enum class BlockedScreenType {
    ACCESSIBILITY_SETTINGS,
    DEVICE_ADMIN,
    NONE
}

class SampleAccessibilityService : AccessibilityService() {

    companion object {

        private const val PERFORM_GLOBAL_ACTION =
            "com.example.screenshotsample.PERFORM_GLOBAL_ACTION"
        const val GLOBAL_ACTION_TYPE = "global_action_type"

        fun takeScreenShot(context: Context) {
            val runServiceIntent = Intent(context, SampleAccessibilityService::class.java)
            runServiceIntent.action = PERFORM_GLOBAL_ACTION
            runServiceIntent.putExtra(GLOBAL_ACTION_TYPE, GLOBAL_ACTION_TAKE_SCREENSHOT)
            context.startService(runServiceIntent)
        }
    }

    override fun onInterrupt() {}

    override fun onAccessibilityEvent(event: AccessibilityEvent) {

        Log.d("******", "******* onAccessibilityEvent: ${event.className}")

        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            if (event.packageName != null) {
                handleWindowStateChanged(
                    event.packageName.toString(),
                    if (event.className != null) event.className.toString() else "?"
                )
            }
        }

    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val extras = intent.extras
        val commandType = extras?.getInt(GLOBAL_ACTION_TYPE, -1) ?: -1
        if (commandType != -1) {
            performGlobalAction(commandType)
        }
        stopSelf()
        return Service.START_NOT_STICKY
    }

    private fun handleWindowStateChanged(packageName: String, className: String) {
        Log.d("******", "***** foreground package = $packageName, className = $className")

        val windowsLocal = windows

        // We know both screens live in com.android.settings so we limit any checking to this package only
        if (packageName == "com.android.settings") {
            // This stage works by checking the title of the page for known screen we want to block.
            getWindows().forEach {
                if (it.title == "Accessibility") { // TODO - This needs to support all language variations
                    showBlockScreen()
                } else if (it.title == "Device admin apps") { // TODO - This needs to support all language variations
                    showBlockScreen()
                }
            }

            // This stage works by looking at the UI components in the screen and trying to identify known signatures
            // of a paricular screen.
            getActiveRootNode()?.let {
                val blockedScreenType = isBlockedScreen(it)
                Log.d("****", "**** Is blocked screen: " + blockedScreenType)
                if (blockedScreenType != BlockedScreenType.NONE) {
                    showBlockScreen()
                }
            }
        }
    }

    /**
     * This function will attempt to identify a blocked screen by looking for components within the UI
     * tree.
     */
    private fun isBlockedScreen(node: AccessibilityNodeInfo): BlockedScreenType {

        Log.d("++++++++++", "+++++++++++++ Node = $node")

        if (isNodeAccessibilitySettings(node)) {
            return BlockedScreenType.ACCESSIBILITY_SETTINGS
        } else if (isNodeDeviceAdminScreen(node)) {
            return BlockedScreenType.DEVICE_ADMIN
        } else {
            for (i in 0 until node.childCount) {
                val nodeChild = node.getChild(i)
                val blockedScreenType = isBlockedScreen(nodeChild)
                if (blockedScreenType != BlockedScreenType.NONE) {
                    return blockedScreenType
                }
            }
        }
        return BlockedScreenType.NONE
    }

    /**
     * This will inspect an individual node to try and identify a particular view that only exists within
     * the certain screen. It may be necessary to do something more complex than this to avoid false
     * positives, such as inspecting multiple views.
     */
    private fun isNodeAccessibilitySettings(node: AccessibilityNodeInfo): Boolean {
        if (node.packageName == "com.android.settings" &&
            node.className == "android.widget.TextView" &&
            node.text == "Accessibility" &&
            node.parent.className == "android.widget.FrameLayout"
        ) {
            return true
        } else {
            return false
        }
    }

    private fun isNodeDeviceAdminScreen(node: AccessibilityNodeInfo): Boolean {
        // TODO - Something similar to the isNodeAccessibilitySettings to identify the device admin screen.
        return false;
    }

    private fun getActiveRootNode(): AccessibilityNodeInfo? {
        return try {
            rootInActiveWindow
        } catch (e: Exception) {
            null
        }
    }

    private fun showBlockScreen() {
        val intent = Intent(this, BlockSettingsActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }
}