package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.BuildConfig
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderFragment
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.*
import kotlin.properties.Delegates

private const val REQUEST_TURN_DEVICE_LOCATION_ON = 29
class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()

    private lateinit var binding: FragmentSelectLocationBinding

    private lateinit var map: GoogleMap
    private var PoiSelected by Delegates.notNull<Boolean>()
    private lateinit var chosenPoi: PointOfInterest
    private val TAG = this.javaClass.simpleName

    private val REQUEST_PERMISSION_LOCATION = 1
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

//        TODO: add the map setup implementation
//        TODO: zoom to the user location after taking his permission
//        TODO: add style to the map
//        TODO: put a marker to location that the user selected


//        TODO: call this function after the user confirms on the selected location
        onLocationSelected()

        return binding.root
    }

    private fun isPermissionGranted(): Boolean {

        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PERMISSION_GRANTED
    }

     override fun onMapReady(googleMap: GoogleMap) {
         PoiSelected = false
         map = googleMap
         checkPermissionsAndDeviceLocationSettings()

         CameraUpdateFactory.zoomTo(15f)
         setMapLongClick(map)
         setPoiClick(map)
         setMapStyle(map) //Styling only applies to maps that use the normal map type.
    }

    @SuppressLint("MissingPermission")
    private fun enableLocationLayer() {

        Log.e(TAG, "Inside Enable Location Start")
        if (!map.isMyLocationEnabled) {
            map.isMyLocationEnabled = true

        }
        showSnackBar(getString(R.string.select_poi))

    }

    private fun checkPermissionsAndDeviceLocationSettings() {
        if (isPermissionGranted()) {
            checkDeviceLocationSettings()

        } else {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_PERMISSION_LOCATION
            )
        }
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        if (grantResults.isNotEmpty() && (grantResults[0] == PERMISSION_GRANTED)) {
            checkPermissionsAndDeviceLocationSettings()
        } else {

            Snackbar.make(
                activity!!.findViewById<ConstraintLayout>(R.id.reminderActConstLayout),
                R.string.permission_denied_explanation,
                Snackbar.LENGTH_INDEFINITE
            )
                .setAction(R.string.settings) {
                    val intent = Intent().apply {
                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)

                    }
                    startActivityForResult(intent, REQUEST_PERMISSION_LOCATION)

                }.show()
        }
    }

    @SuppressLint("MissingPermission")
    private fun checkDeviceLocationSettings(resolve: Boolean = true) {

        enableLocationLayer()

        val locationRequest = createLocationRequest()

        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)

        val settingsClient = LocationServices.getSettingsClient(requireActivity())

        val locationSettingsResponseTask =
            settingsClient.checkLocationSettings(builder.build())

        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve) {
                try {
                    startIntentSenderForResult(
                        exception.resolution.intentSender,
                        REQUEST_TURN_DEVICE_LOCATION_ON, null, 0, 0, 0, null
                    )

                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d(
                        SaveReminderFragment.TAG,
                        "Error getting location settings resolution: " + sendEx.message
                    )
                }
            } else {
                Snackbar.make(
                    activity!!.findViewById<CoordinatorLayout>(R.id.selectLocationCoordLayout),
                    R.string.location_required_error, Snackbar.LENGTH_LONG
                ).setAction(android.R.string.ok) {
                    //
                    Log.e(TAG, "Here")

                }.show()
            }
        }

        locationSettingsResponseTask.addOnSuccessListener {

            Log.e(TAG, "SUCCESSFUL!")

        }
    }

    private fun createLocationRequest(): LocationRequest {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 10000L
        }
        return locationRequest
    }

    private fun showSnackBar(text: String) {
        //work on the position of Snack bar
        val viewPos = binding.selectLocationCoordLayout
        val snackbar = Snackbar.make(viewPos, text, Snackbar.LENGTH_SHORT)
        snackbar.show()

    }

    private fun setMapLongClick(googleMap: GoogleMap) {
        PoiSelected = false
        googleMap.setOnMapLongClickListener { latLng ->
            googleMap.clear()
            // A Snippet is Additional text that's displayed below the title.
            val snippet = String.format(
                Locale.getDefault(),
                "Lat: %1$.5f, Long: %2$.5f",
                latLng.latitude,
                latLng.longitude
            )

            val marker = MarkerOptions().position(latLng).
            title(getString(R.string.dropped_pin)).snippet(snippet)

            googleMap.addMarker(marker)

            chosenPoi = PointOfInterest(latLng, marker.title.toString(), marker.title.toString())
            PoiSelected = true
        }
    }

    private fun setPoiClick(map: GoogleMap) {
        PoiSelected = false
        map.setOnPoiClickListener {
            val poiMarker = map.addMarker(
                MarkerOptions()
                    .position(it.latLng)
                    .title(it.name)
            )
            poiMarker?.showInfoWindow()
            chosenPoi = it
            PoiSelected = true
        }

    }

    private fun setMapStyle(googleMap: GoogleMap) {
        try {
            // Customize the styling of the base map using a JSON object defined
            // in a raw resource file.
            val success = googleMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    context!!,
                    R.raw.map_style
                )
            )

            if (!success) {
                Log.e(TAG, "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", e)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        /*
        you need super.onActivityResult() in the host activity for this to be triggered
        * */

        when (requestCode) {
            REQUEST_TURN_DEVICE_LOCATION_ON -> {
                Log.e(TAG, "onActivityResult - REQ_DEV_LOC")
//                Toast.makeText(context, "Location RESPONSE", Toast.LENGTH_SHORT).show()
                checkDeviceLocationSettings(false)
            }

            REQUEST_PERMISSION_LOCATION -> {
                checkPermissionsAndDeviceLocationSettings()
                Log.e(TAG, "onActivityResult - REQ_PERM")
            }
        }
    }

    private fun onLocationSelected() {
        //        TODO: When the user confirms on the selected location,
        //         send back the selected location details to the view model
        //         and navigate back to the previous fragment to save the reminder and add the geofence
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        PoiSelected = false
    }

}
