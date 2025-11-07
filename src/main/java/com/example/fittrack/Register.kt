package com.example.fittrack

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var userDao: UserDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Initialize database
        db = AppDatabase.getDatabase(this)
        userDao = db.userDao()

        // Reference views
        val firstNameEditText = findViewById<EditText>(R.id.editTextText)
        val lastNameEditText = findViewById<EditText>(R.id.editTextText2)
        val usernameEditText = findViewById<EditText>(R.id.editTextText3)
        val emailEditText = findViewById<EditText>(R.id.editTextText4)
        val passwordEditText = findViewById<EditText>(R.id.editTextTextPassword)
        val confirmPasswordEditText = findViewById<EditText>(R.id.editTextTextPassword2)
        val registerButton = findViewById<Button>(R.id.button)

        // Register button click
        registerButton.setOnClickListener {
            val firstName = firstNameEditText.text.toString().trim()
            val lastName = lastNameEditText.text.toString().trim()
            val username = usernameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()

            // Basic validation
            if (firstName.isBlank() || lastName.isBlank() || username.isBlank() ||
                email.isBlank() || password.isBlank() || confirmPassword.isBlank()
            ) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Save user locally & send to remote API
            lifecycleScope.launch(Dispatchers.IO) {
                val existingUser = userDao.getUserByUsername(username)
                if (existingUser != null) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@RegisterActivity,
                            "Username already exists",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    val hashedPassword = Utils.hashPassword(password)

                    val newUser = User(
                        firstName = firstName,
                        lastName = lastName,
                        email = email,
                        username = username,
                        password = hashedPassword
                    )

                    // Save locally (RoomDB)
                    userDao.insertUser(newUser)

                    // Try sending to remote API
                    try {
                        val userDto = UserDTO(
                            name = "$firstName $lastName",
                            email = email
                        )

                        val created = ApiClient.api.createUser(userDto)
                        println("✅ Synced with API: ${created.name} - ${created.email}")
                    } catch (e: Exception) {
                        e.printStackTrace()
                        println("⚠️ Could not connect to API: ${e.message}")
                    }

                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@RegisterActivity,
                            "Registration successful!",
                            Toast.LENGTH_SHORT
                        ).show()

                        // ✅ Navigate straight to Dashboard
                        val intent =
                            Intent(this@RegisterActivity, DashboardActivity::class.java)
                        intent.putExtra("username", newUser.username)
                        startActivity(intent)
                        finish()
                    }
                }
            }
        }
    }
}
