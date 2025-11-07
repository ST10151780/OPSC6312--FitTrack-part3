package com.example.fittrack

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsActivity : AppCompatActivity() {

    private lateinit var switchNotifications: Switch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        supportActionBar?.title = "Settings"

        // Reference views
        val spinnerLanguage = findViewById<Spinner>(R.id.spinnerLanguage)
        val radioGroupTheme = findViewById<RadioGroup>(R.id.radioGroupTheme)
        switchNotifications = findViewById(R.id.switchNotifications)
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        val btnDeleteAccount = findViewById<Button>(R.id.btnDeleteAccount)

        // Load saved preferences
        val prefs = getSharedPreferences("FitTrackPrefs", Context.MODE_PRIVATE)
        val notificationsEnabled = prefs.getBoolean("notifications_enabled", true)
        switchNotifications.isChecked = notificationsEnabled

        // 🔔 Handle notification toggle
        switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            val editor = prefs.edit()
            editor.putBoolean("notifications_enabled", isChecked)
            editor.apply()

            val message = if (isChecked) {
                "Notifications enabled"
            } else {
                "Notifications disabled"
            }
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }

        // 🔤 Language spinner (placeholder)
        spinnerLanguage.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                val language = parent?.getItemAtPosition(position).toString()
                Toast.makeText(this@SettingsActivity, "Language set to $language", Toast.LENGTH_SHORT).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // 🎨 Theme radio buttons (placeholder)
        radioGroupTheme.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radioLight -> Toast.makeText(this, "Light theme selected", Toast.LENGTH_SHORT).show()
                R.id.radioDark -> Toast.makeText(this, "Dark theme selected", Toast.LENGTH_SHORT).show()
            }
        }

        // 🚪 Logout button
        btnLogout.setOnClickListener {
            Toast.makeText(this, "Logging out...", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        // ❌ Delete account (placeholder for now)
        btnDeleteAccount.setOnClickListener {
            Toast.makeText(this, "Delete account feature coming soon!", Toast.LENGTH_SHORT).show()
        }
    }
}