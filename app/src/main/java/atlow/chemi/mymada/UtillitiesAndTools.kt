package atlow.chemi.mymada

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import atlow.chemi.mymada.fragments.*
import atlow.chemi.mymada.helpers.LocalHelper
import atlow.chemi.mymada.settings.DetectionControls
import atlow.chemi.mymada.tools.ToolsWindow

/** Installed activity/fragment identities and preference keys survive the shell migration. */
class UtillitiesAndTools : AppCompatActivity() {
    private lateinit var shell: ToolsWindow
    private val fragments = mutableMapOf<Int, Fragment>()

    override fun attachBaseContext(base: Context) = super.attachBaseContext(LocalHelper.onAttach(base))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        shell = ToolsWindow(this, R.menu.navigation_menu)
        val routes = linkedMapOf("contrac" to R.id.contractionCalc, "pulse" to R.id.pulseCalc,
            "metro" to R.id.metronome, "vital" to R.id.vitalCalc, "oxy" to R.id.oxygenCalc,
            "apgar" to R.id.apgarCalc, "glaz" to R.id.glazgowCalc, "dict" to R.id.dictionary, "prot" to R.id.protocoles)
        val visible = getSharedPreferences("Settings", 0).getStringSet("Tools", routes.keys) ?: routes.keys
        routes.filterKeys { it !in visible }.values.forEach(shell.menu::removeItem)
        val initial = if ("metro" in visible || intent.getBooleanExtra("running", false) || intent.getBooleanExtra("start", false)) R.id.metronome
            else routes.entries.firstOrNull { it.key in visible }?.value?.let { if (it == R.id.protocoles) R.id.bls else it } ?: R.id.metronome
        val restored = supportFragmentManager.findFragmentById(R.id.frame)
        // The Java shell did not save a destination key. Never cache its restored
        // fragment under the default metronome route after an upgrade.
        val restoredRoute = when (restored) {
            is ContracTimer -> R.id.contractionCalc
            is PulseCalc -> R.id.pulseCalc
            is MetroActivity -> R.id.metronome
            is VitalsChecker -> R.id.vitalCalc
            is OxyCalc -> R.id.oxygenCalc
            is ApgarCalc -> R.id.apgarCalc
            is GlazgoCalc -> R.id.glazgowCalc
            is DictionaryAdmin -> R.id.dictionary
            is BlsProtocoles -> R.id.bls
            else -> initial
        }
        shell.selected = savedInstanceState?.getInt("destination", restoredRoute) ?: initial
        restored?.let { fragments[fragmentKey(restoredRoute)] = it }
        menuInflater.inflate(R.menu.popup_menu, shell.menu)
        shell.install(true, ::onNavigationItemSelected) {
            if (savedInstanceState == null) displayFragment(shell.selected, false)
        }
    }

    override fun setTitle(title: CharSequence?) {
        super.setTitle(title)
        if (::shell.isInitialized) shell.title = title?.toString().orEmpty()
    }

    override fun onResume() {
        super.onResume()
        shell.onResume()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt("destination", shell.selected)
        super.onSaveInstanceState(outState)
    }

    fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.homeDrawer -> finish()
            R.id.one -> DetectionControls.toggle(this, item)
            R.id.action_settings -> startActivity(Intent(this, SettingsPage::class.java))
            else -> displayFragment(item.itemId, true)
        }
        return true
    }

    private fun fragmentKey(id: Int) = if (id in listOf(R.id.bls, R.id.als, R.id.death)) R.id.protocoles else id

    private fun displayFragment(id: Int, animate: Boolean) {
        val fragment = fragments.getOrPut(fragmentKey(id)) {
            when (id) {
                R.id.contractionCalc -> ContracTimer()
                R.id.pulseCalc -> PulseCalc()
                R.id.vitalCalc -> VitalsChecker()
                R.id.oxygenCalc -> OxyCalc()
                R.id.apgarCalc -> ApgarCalc()
                R.id.glazgowCalc -> GlazgoCalc()
                R.id.dictionary -> DictionaryAdmin()
                R.id.bls, R.id.als, R.id.death -> BlsProtocoles()
                else -> MetroActivity()
            }
        }
        if (fragment is BlsProtocoles) fragment.setCurrentPage(when (id) { R.id.als -> 1; R.id.death -> 2; else -> 0 })
        shell.selected = id
        supportFragmentManager.beginTransaction().apply {
            if (animate) setCustomAnimations(R.anim.enter_from_top, R.anim.exit_to_top)
            replace(R.id.frame, fragment, "tools:${fragmentKey(id)}")
        }.commit()
    }

    fun isDeviceTablet() = ToolsWindow.isDeviceTablet(this)
}
