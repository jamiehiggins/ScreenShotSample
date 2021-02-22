package com.example.screenshotsample

import android.Manifest
import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.araujo.jordan.excuseme.ExcuseMe
import com.example.screenshotsample.accessibilityservice.SampleAccessibilityService
import com.example.screenshotsample.accessibilityservice.Util
import com.example.screenshotsample.deviceadmin.SampleDeviceAdminReceiver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        // READ_EXTERNAL_STORAGE permission required to access the photo uri after it is taken
        ExcuseMe.couldYouGive(this).permissionFor(Manifest.permission.READ_EXTERNAL_STORAGE) {}

        findViewById<Button>(R.id.takeScreenShotButton).setOnClickListener { _ ->
            takeScreenShot()
        }

        if (!Util.isAccessibilityEnabled(this, "SampleAccessibilityService")) {
            showAccessibilityRequiredDialog();
        }
        if (!Util.isDeviceAdminEnabled(this)) {
            showDeviceAdminRequiredDialog();
        }
    }

    fun takeScreenShot() {
        SampleAccessibilityService.takeScreenShot(this)

        lifecycleScope.launch(Dispatchers.Main) {
            delay(2000)
            val lastPhotoUri = Util.getLastPhoto(this@MainActivity)
            Toast.makeText(this@MainActivity, "Screenshot Uri: $lastPhotoUri", Toast.LENGTH_LONG).show()

            // TODO - You can use the Uri of the last photo as desired here (e.g. to move to internal storage or something else)
        }
    }

    fun showAccessibilityRequiredDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.accessibility_required)
        builder.setMessage(R.string.accessibility_description)

        // Add the buttons
        builder.setPositiveButton(android.R.string.ok) { dialog: DialogInterface?, id: Int ->
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            try {
                startActivityForResult(intent, 0)
            } catch (e: Exception) {
                Toast.makeText(
                    this,
                    getString(R.string.cannot_launch_accessibility_settings),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        builder.setNegativeButton(android.R.string.cancel) { dialog: DialogInterface?, which: Int -> }
        builder.setCancelable(false)
        builder.show()
    }

    fun showDeviceAdminRequiredDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.device_admin_required)
        builder.setMessage(R.string.device_admin_description)

        // Add the buttons
        builder.setPositiveButton(android.R.string.ok) { dialog: DialogInterface?, id: Int ->
            try {
                val adminComponent = ComponentName(this, SampleDeviceAdminReceiver::class.java)
                // Launch the activity to have the user enable our admin.
                val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
                intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent)
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(
                    getApplicationContext(),
                    "DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN not supported on this device",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        builder.setNegativeButton(android.R.string.cancel) { dialog: DialogInterface?, which: Int -> }
        builder.setCancelable(false)
        builder.show()
    }
}