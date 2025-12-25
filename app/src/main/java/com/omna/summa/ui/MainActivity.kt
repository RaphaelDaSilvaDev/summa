package com.omna.summa.ui

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.fragment.NavHostFragment
import com.omna.summa.R
import com.omna.summa.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    private val navController by lazy {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fcvContent) as? NavHostFragment
        navHostFragment?.navController
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()


        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())

            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                if (imeVisible) imeInsets.bottom else systemBars.bottom
            )
            insets
        }
    }
}