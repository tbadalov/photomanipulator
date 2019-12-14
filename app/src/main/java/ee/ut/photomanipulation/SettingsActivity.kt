package ee.ut.photomanipulation

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_settings.*


class SettingsActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        val settings = getSharedPreferences("settings", 0)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        setting_sync.isChecked = settings.getBoolean("cloudSync", false)
        setting_sync.setOnCheckedChangeListener { _, isChecked ->
            settings.edit().putBoolean("cloudSync", isChecked).apply()
        }

        setting_camera.isChecked = settings.getBoolean("useCustomCamera", false)
        setting_camera.setOnCheckedChangeListener { _, isChecked ->
            settings.edit().putBoolean("useCustomCamera", isChecked).apply()
        }
    }
}