package io.surf.wm.iwannasurfapp

import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.view.MenuItem
import android.widget.TextView
import io.surf.wm.iwannasurfapp.model.Dtos.*

class SpotInfoActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spot_info)

//      supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.hide()

        val spot = intent.getSerializableExtra(MainActivity.EXTRA_SPOT) as Spot
        val (sLat, sLon) = Pair(intent.getDoubleExtra(MainActivity.EXTRA_LAT, 0.0), intent.getDoubleExtra(MainActivity.EXTRA_LON, 0.0))

        title = spot.dbSpot.identification.name

        findViewById<ConstraintLayout>(R.id.navTo).setOnClickListener({
            goToMaps(sLat, sLon, spot.dbSpot.additionalInfo.realLat, spot.dbSpot.additionalInfo.realLon)
        })

        (findViewById<TextView>(R.id.name)).text = spot.dbSpot.identification.name
        (findViewById<TextView>(R.id.distance)).text = spot.dbSpot.identification.distance.format(2)
    }
    fun Double.format(digits: Int) = String.format("%.${digits}f", this)

    private fun goToMaps(sLat: Double, sLon: Double, dLat: Double, dLon: Double) {
        val uri = Uri.parse(String.format("http://maps.google.com/maps?saddr=%s,%s&daddr=%s,%s",
                String.format("%f", sLat).replace(',', '.'), String.format("%f", sLon).replace(',', '.'),
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
}