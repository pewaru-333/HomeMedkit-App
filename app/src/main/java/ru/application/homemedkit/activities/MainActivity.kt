package ru.application.homemedkit.activities

import android.content.SharedPreferences
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import ru.application.homemedkit.R
import ru.application.homemedkit.alarms.AlarmSetter
import ru.application.homemedkit.databinding.ActivityMainBinding
import ru.application.homemedkit.fragments.FragmentHome
import ru.application.homemedkit.fragments.FragmentIntakes
import ru.application.homemedkit.fragments.FragmentMedicines
import ru.application.homemedkit.fragments.FragmentSettings
import ru.application.homemedkit.helpers.NEW_INTAKE
import ru.application.homemedkit.helpers.NEW_MEDICINE
import ru.application.homemedkit.helpers.PAGES
import ru.application.homemedkit.helpers.SETTINGS_CHANGED

class MainActivity: AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    companion object {
        lateinit var preferences: SharedPreferences
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        preferences = PreferenceManager.getDefaultSharedPreferences(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        FragmentSettings().getAppTheme()

        binding.bottomNavigationBarView.setOnItemSelectedListener(pickItem())

        getPage()
        setExpirationChecker()
    }

    private fun pickItem() = { item: MenuItem ->
        when (item.itemId) {
            R.id.bottom_menu_main -> replace(FragmentHome())
            R.id.bottom_menu_medicines -> replace(FragmentMedicines())
            R.id.bottom_menu_intakes -> replace(FragmentIntakes())
            R.id.bottom_menu_settings -> replace(FragmentSettings())
        }

        true
    }

    private fun getPage() {
        when {
            intent.getBooleanExtra(NEW_MEDICINE, false) ->
                binding.bottomNavigationBarView.selectedItemId = R.id.bottom_menu_medicines
            intent.getBooleanExtra(NEW_INTAKE, false) ->
                binding.bottomNavigationBarView.selectedItemId = R.id.bottom_menu_intakes
            intent.getBooleanExtra(SETTINGS_CHANGED, false) ->
                binding.bottomNavigationBarView.selectedItemId = R.id.bottom_menu_settings
            else -> toPage()
        }.also { intent.replaceExtras(Bundle()) }
    }

    private fun toPage() {
        when (FragmentSettings().getHomePage()) {
            PAGES[1] -> binding.bottomNavigationBarView.selectedItemId = R.id.bottom_menu_medicines
            PAGES[2] -> binding.bottomNavigationBarView.selectedItemId = R.id.bottom_menu_intakes
            PAGES[3] -> binding.bottomNavigationBarView.selectedItemId = R.id.bottom_menu_settings
            else -> binding.bottomNavigationBarView.selectedItemId = R.id.bottom_menu_main
        }
    }

    private fun replace(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.constraintLayout, fragment).commit()
    }

    fun setExpirationChecker() = AlarmSetter(this).checkExpiration()
}