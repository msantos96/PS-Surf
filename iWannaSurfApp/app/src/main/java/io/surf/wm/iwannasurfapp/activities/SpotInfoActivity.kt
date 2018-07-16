package io.surf.wm.iwannasurfapp.activities

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.support.constraint.ConstraintLayout
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.MenuItem
import io.surf.wm.iwannasurfapp.adapters.PreviewArrayAdapter
import io.surf.wm.iwannasurfapp.fragments.FavoriteFragment
import io.surf.wm.iwannasurfapp.model.Dtos.*
import android.support.v7.app.AlertDialog
import android.text.InputType
import android.view.View
import android.widget.*
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import io.surf.wm.iwannasurfapp.IWSApplication
import io.surf.wm.iwannasurfapp.R

class SpotInfoActivity : AppCompatActivity(), OnMapReadyCallback {

    private val appState by lazy { applicationContext as IWSApplication }
    private val mapView: MapView by lazy { findViewById<MapView>(R.id.mapView) }
    private val spot by lazy { intent.getSerializableExtra(MainActivity.EXTRA_SPOT) as Spot }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spot_info)

        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

//      supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.hide()

        title = spot.dbSpot.identification.name

        findViewById<ConstraintLayout>(R.id.navTo).setOnClickListener {
            goToMaps(spot.dbSpot.additionalInfo.realLat, spot.dbSpot.additionalInfo.realLon)
        }

        findViewById<Button>(R.id.feedback).setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                val location = (getSystemService(Context.LOCATION_SERVICE) as LocationManager).getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (distanceBetweenCoordinates(location.latitude, location.longitude, spot.dbSpot.additionalInfo.realLat, spot.dbSpot.additionalInfo.realLon) <= 100) {
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle(applicationContext.resources.getString(R.string.feedback_dialog))

                    // Set up the input
                    val input = EditText(this)

                    // Specify the type of input expected
                    input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
                    builder.setView(input)

                    // Set up the buttons
                    builder.setPositiveButton("OK") { _, _ ->
                        appState.queue.add(
                                JsonObjectRequest(Request.Method.POST, MainActivity.sendFeedback(), Notification(spot.dbSpot._id, input.text.toString()).toJsonObj(),
                                        Response.Listener { _ ->
                                            Toast.makeText(applicationContext, applicationContext.getString(R.string.done), Toast.LENGTH_LONG).show()
                                        },
                                        Response.ErrorListener { _ ->
                                            Toast.makeText(applicationContext, applicationContext.getString(R.string.error), Toast.LENGTH_LONG).show()
                                        }))
                        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
                    }

                    builder.show()
                } else Toast.makeText(this, applicationContext.resources.getString(R.string.feedback_distance), Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, applicationContext.resources.getString(R.string.feedback_start_location), Toast.LENGTH_LONG).show()
            }
        }

        (findViewById<TextView>(R.id.distance)).visibility = if(spot.dbSpot.identification.distance != 0.0) View.VISIBLE else View.INVISIBLE

        bindTextViews(mapOf(
                Pair(R.id.name, spot.dbSpot.identification.name),
                Pair(R.id.distance, String.format("%.${2}f Km", spot.dbSpot.identification.distance)),
                Pair(R.id.min_rank, spot.dbSpot.additionalInfo.rank.toString()),
                Pair(R.id.height, spot.realTimeData.swell.height.format(2)),
                Pair(R.id.period, spot.realTimeData.swell.period.format(2)),
                Pair(R.id.swell_direction, spot.realTimeData.swell.direction.format(2)),
                Pair(R.id.speed, spot.realTimeData.wind.speed.format(2)),
                Pair(R.id.wind_direction, spot.realTimeData.wind.direction.format(2))
        ))

        val add_rem = (findViewById<Button>(R.id.add_rem_favs))
        val favsId: MutableSet<String> = appState.sharedPreference.getStringSet(IWSApplication.FAVS_PREF, mutableSetOf())
        var remNAdd = favsId.contains(spot.dbSpot._id)

        add_rem.text = applicationContext.getText(if(remNAdd) R.string.remove_favorite else R.string.add_favorite)
        add_rem.setOnClickListener {
            if(remNAdd) {
                favsId.remove(spot.dbSpot._id)
                (appState.frags[R.id.navigation_favorite] as FavoriteFragment).removeSpot(spot)
                add_rem.text = applicationContext.getText(R.string.add_favorite)
            } else {
                favsId.add(spot.dbSpot._id)
                (appState.frags[R.id.navigation_favorite] as FavoriteFragment).addSpot(spot)
                add_rem.text = applicationContext.getText(R.string.remove_favorite)
            }

            appState.sharedPreference
                    .edit()
                    .remove(IWSApplication.FAVS_PREF)
                    .apply()
            appState.sharedPreference
                    .edit()
                    .putStringSet(IWSApplication.FAVS_PREF, favsId)
                    .apply()

            remNAdd = !remNAdd
        }

        findViewById<RecyclerView>(R.id.preview).apply {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(true)

            // use a linear layout manager
            layoutManager = LinearLayoutManager(applicationContext, LinearLayoutManager.HORIZONTAL, false)

            // specify an viewAdapter (see also next example)
            adapter = PreviewArrayAdapter(spot.weekPreview)
        }
    }

    private fun toRad(value: Double): Double {
        return value * Math.PI / 180
    }

    private fun distanceBetweenCoordinates(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val EARTH_RADIUS = 6371

        val dLat = toRad(lat2-lat1)
        val dLon = toRad(lon2-lon1)
        val latitude1 = toRad(lat1)
        val latitude2 = toRad(lat2)

        val a = Math.sin(dLat/2) * Math.sin(dLat/2) + Math.sin(dLon/2) * Math.sin(dLon/2) * Math.cos(latitude1) * Math.cos(latitude2);

        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a))
        val dist =  EARTH_RADIUS * c
        return dist
    }

    private fun bindTextViews(idsAndValues: Map<Int, String>) {
        for(idAndValue in idsAndValues)
            (findViewById<TextView>(idAndValue.key)).text = idAndValue.value
    }

    fun Double.format(digits: Int) = String.format("%.${digits}f", this)

    private fun goToMaps(dLat: Double, dLon: Double) {
        val uri = Uri.parse(String.format("http://maps.google.com/maps?saddr=Your+location&daddr=%s,%s",
                String.format("%f", dLat).replace(',', '.'), String.format("%f", dLon).replace(',', '.')))

        val mapIntent = Intent(Intent.ACTION_VIEW, uri)
        mapIntent.`package` = "com.google.android.apps.maps"
        startActivity(mapIntent)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle?, outPersistentState: PersistableBundle?) {
        super.onSaveInstanceState(outState, outPersistentState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }



    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        val dst = LatLng(spot.dbSpot.additionalInfo.realLat, spot.dbSpot.additionalInfo.realLon)

        googleMap.addMarker(MarkerOptions().position(dst).title(spot.dbSpot.identification.name))
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(dst, 12f))
        googleMap.uiSettings.isMapToolbarEnabled = false
    }
}