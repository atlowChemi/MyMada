package atlow.chemi.mymada

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import atlow.chemi.mymada.helpers.LocalHelper

/** Installed component identity and published layout retained during the Kotlin migration. */
class lockPage : AppCompatActivity() {
    override fun attachBaseContext(base: Context) = super.attachBaseContext(LocalHelper.onAttach(base))
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_not_mada)
        setSupportActionBar(findViewById<Toolbar>(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
    fun cod(view: View) { startActivity(Intent(this, codeC::class.java)); finish() }
    fun cond(view: View) { startActivity(Intent(this, codeRequest::class.java)) }
    fun TZ_app(view: View) {
        try { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.groboot.mdaemergency"))) }
        catch (_: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.groboot.mdaemergency")))
        }
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> { finish(); true }
        else -> super.onOptionsItemSelected(item)
    }
}
