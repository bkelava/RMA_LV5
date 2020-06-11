package hr.ferit.whereisbozidarkelava

import android.Manifest
import android.app.Activity
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.location.*
import android.media.MediaScannerConnection
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private var soundPoolManager: SoundPoolManager = SoundPoolManager()
    private val values: Values = Values()

    private lateinit var googleMap: GoogleMap
    private lateinit var customOnMapClickListener: GoogleMap.OnMapClickListener

    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener

    private var adress: String = ""
    private var photoUri: Uri? = null
    private lateinit var storageDir: File
    private lateinit var photoName: String
    private lateinit var photoPath: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initialize()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == values.getPhotoCode() && resultCode == Activity.RESULT_OK) {
            MediaScannerConnection.scanFile(
                WhereIsBK.ApplicationContext,
                arrayOf(photoUri?.path),
                null
            ) { _, _ -> }

            val mFile: File = File(photoPath)
            val intent = Intent(Intent.ACTION_VIEW).setDataAndType(
                FileProvider.getUriForFile(
                    WhereIsBK.ApplicationContext,
                    values.getAuthorityValue(),
                    mFile
                ), "image/*"
            ).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

            val pendingIntent = PendingIntent.getActivity(
                WhereIsBK.ApplicationContext,
                0,
                intent,
                0
            )

            val notificationBuilder = NotificationCompat.Builder(this, getChannelId(CHANNEL_LIKES))
            notificationBuilder.setAutoCancel(true)
                .setContentTitle("New Image Saved!")
                .setContentText(storageDir.toString())
                .setSmallIcon(R.drawable.envelope)
                .setVibrate(longArrayOf(1000, 1000))
                .setColor(Color.BLUE)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentIntent(pendingIntent)
            val notification: Notification = notificationBuilder.build()
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(1001, notification)
        }
    }

    override fun onStart() {
        super.onStart()
        if (hasPermissions())
            if (!hasPermissions()) {
                requestPermissions()
            }
    }

    override fun onResume() {
        super.onResume()
        if (hasPermissions()) {
            startTracking()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        val uiSettings: UiSettings = this.googleMap.getUiSettings()
        uiSettings.isZoomControlsEnabled = true
        uiSettings.isMyLocationButtonEnabled = true
        uiSettings.isZoomGesturesEnabled = true
        this.googleMap.setOnMapClickListener(this.customOnMapClickListener)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        this.googleMap.setMyLocationEnabled(true)
        this.googleMap.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {
            override fun getInfoWindow(arg0: Marker): View? {
                return null
            }

            override fun getInfoContents(marker: Marker): View {
                val context = applicationContext
                val info = LinearLayout(context)
                info.orientation = LinearLayout.VERTICAL
                val title = TextView(context)
                title.setTextColor(Color.BLACK)
                title.gravity = Gravity.CENTER
                title.setTypeface(null, Typeface.BOLD)
                title.text = marker.title
                val snippet = TextView(context)
                snippet.setTextColor(Color.GRAY)
                snippet.text = marker.snippet
                snippet.gravity = Gravity.CENTER
                info.addView(title)
                info.addView(snippet)
                return info
            }
        })
    }

    private fun initialize() {
        soundPoolManager.init()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            createNotificationChannels()
        btnCamera.setOnClickListener() {
            takePicture()
        }

        this.locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        val startLocation: Location? =
            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        if (startLocation != null) updateAddress(startLocation)
        this.locationListener = myLocationListener()
        (gmGoogleMap as? SupportMapFragment)?.getMapAsync(this)
        this.customOnMapClickListener =
            GoogleMap.OnMapClickListener { latLng ->
                val newMarkerOptions = MarkerOptions()
                newMarkerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.pin))
                newMarkerOptions.title("WOOOOOW!")
                newMarkerOptions.snippet(
                    "FINALLY I KNOW WHERE I AM --> " + getAddress(
                        latLng.latitude,
                        latLng.longitude
                    )
                )
                newMarkerOptions.position(latLng)
                googleMap.addMarker(newMarkerOptions)
                soundPoolManager.playSound(R.raw.sound)
            }
    }

    private fun getAddress(latitude: Double, longitude: Double): String? {
        val geocoder = Geocoder(this, Locale.getDefault())
        val nearByAddresses: List<Address> =
            geocoder.getFromLocation(latitude, longitude, 1)
        if (nearByAddresses.size > 0) {
            val stringBuilder = StringBuilder()
            val nearestAddress: Address = nearByAddresses[0]
            stringBuilder
                .append(nearestAddress.getAddressLine(0))
                .append("\n")
                .append(nearestAddress.getLocality())
                .append("\n")
                .append(nearestAddress.getCountryName())
            return stringBuilder.toString()
        }
        return "0.0"
    }

    fun updateAddress(location: Location?) {
        tvLatitude.text = "Latitude " + location!!.latitude.toString()
        tvLongitude.text = "Longitude " + location.longitude.toString()
        if (Geocoder.isPresent()) {
            val geocoder = Geocoder(this, Locale.getDefault())
            val nearByAddresses: List<Address> = geocoder.getFromLocation(
                location.getLatitude(), location.getLongitude(), 1
            )
            if (nearByAddresses.size > 0) {
                val stringBuilder = StringBuilder()
                val nearestAddress: Address = nearByAddresses[0]
                stringBuilder.append(nearestAddress.getAddressLine(0)).append("\n")
                    .append(nearestAddress.getLocality()).append("\n")
                    .append(nearestAddress.getCountryName())
                tvLocationDescription.append(stringBuilder.toString())
                adress = nearestAddress.getAddressLine(0)
            }
        }
    }

    private fun startTracking() {
        val criteria = Criteria()
        criteria.accuracy = Criteria.ACCURACY_FINE
        val locationProvider: String = this.locationManager.getBestProvider(criteria, true)
        val minTime: Long = 1000
        val minDistance = 10f
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        if (this.locationListener != null) this.locationManager.requestLocationUpdates(
            locationProvider,
            minTime,
            minDistance,
            this.locationListener
        )
    }

    private fun takePicture() {
        val takePhotoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val photoFile: File = createPhotoFile()
        val uri = FileProvider.getUriForFile(
            WhereIsBK.ApplicationContext,
            values.getAuthorityValue(),
            photoFile
        )
        takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
        startActivityForResult(takePhotoIntent, values.getPhotoCode())
    }

    private fun createPhotoFile(): File {
        val takenTime: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        photoName = adress + "_" + takenTime
        storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        val image = File.createTempFile(photoName, ".jpg", storageDir)
        photoPath = image.absolutePath
        return image
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String?>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            values.getRequestLocationPermission() -> if (grantResults.size > 0) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("TAG", "GOT IT!")
                } else {
                    askForPermission(tvLatitude, tvLongitude)
                }
            }
        }
    }

    private inner class myLocationListener : LocationListener {
        override fun onLocationChanged(location: Location?) {
            updateAddress(location)
        }

        override fun onStatusChanged(
            provider: String,
            status: Int,
            extras: Bundle
        ) {
        }

        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }
}