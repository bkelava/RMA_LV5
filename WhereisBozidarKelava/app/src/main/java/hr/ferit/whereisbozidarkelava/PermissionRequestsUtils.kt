package hr.ferit.whereisbozidarkelava

import android.Manifest
import android.app.Activity
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*

fun hasPermissions(): Boolean {
    val LocationPermission = Manifest.permission.ACCESS_FINE_LOCATION
    val status = ContextCompat.checkSelfPermission(WhereIsBK.ApplicationContext, LocationPermission)
    val status1 =
        ContextCompat.checkSelfPermission(WhereIsBK.ApplicationContext, Manifest.permission.CAMERA)
    val status2 =
        ContextCompat.checkSelfPermission(
            WhereIsBK.ApplicationContext,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    return status == PackageManager.PERMISSION_GRANTED && status1 == PackageManager.PERMISSION_GRANTED && status2 == PackageManager.PERMISSION_GRANTED
}

fun requestPermissions() {
    val permissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.ACCESS_FINE_LOCATION
    )
    ActivityCompat.requestPermissions(Activity(), permissions, 0)
}

fun askForPermission(tvLatitude: TextView, tvLongitude: TextView) {
    val explain = ActivityCompat.shouldShowRequestPermissionRationale(
        Activity(), Manifest.permission.ACCESS_FINE_LOCATION
    )
    if (explain) {
        displayDialog()
    } else {
        tvLatitude.setText("NEED PERMISSION")
        tvLongitude.setText("NEED PERMISSION")
    }
}

private fun displayDialog() {
    val dialogBuilder: AlertDialog.Builder = AlertDialog.Builder(WhereIsBK.ApplicationContext)
    dialogBuilder.setTitle("Location permission")
        .setMessage("NEED PERMISSION")
        .setNegativeButton("DISMISS",
            DialogInterface.OnClickListener { dialog, which -> dialog.cancel() })
        .setPositiveButton("ALLOW",
            DialogInterface.OnClickListener { dialog, which ->
                requestPermissions()
                dialog.cancel()
            })
        .show()
}