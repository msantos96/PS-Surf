package io.surf.wm.iwannasurfapp

import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import io.surf.wm.iwannasurfapp.model.Dtos.*

class SpotInfoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spot_info)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val spot = intent.getSerializableExtra(MainActivity.EXTRA_SPOT) as Spot
        val (sLat, sLon) = Pair(intent.getDoubleExtra(MainActivity.EXTRA_LAT, 0.0), intent.getDoubleExtra(MainActivity.EXTRA_LON, 0.0))

        title = spot.dbSpot.identification.name

        findViewById<TextView>(R.id.spot_maps).setOnClickListener({
            goToMaps(sLat, sLon, spot.dbSpot.identification.lat, spot.dbSpot.identification.lon)
        })
    }

    private fun goToMaps(sLat: Double, sLon: Double, dLat: Double, dLon: Double) {
        //val str = String.format("geo:%s,%s", String.format("%f", lat).replace(',', '.'), String.format("%f", lon).replace(',', '.'))
        //val gmmIntentUri: Uri = Uri.parse(String.format("geo:%f,%f", identification.lat, identification.lon))

        val str = String.format("http://maps.google.com/maps?saddr=%s,%s&daddr=%s,%s",
                String.format("%f", sLat).replace(',', '.'), String.format("%f", sLon).replace(',', '.'),
                String.format("%f", dLat).replace(',', '.'), String.format("%f", dLon).replace(',', '.'))

        val uri: Uri = Uri.parse(str)

        val mapIntent = Intent(Intent.ACTION_VIEW, uri)
        mapIntent.`package` = "com.google.android.apps.maps"
        startActivity(mapIntent)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}