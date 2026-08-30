package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Room
import com.example.data.AppDatabase
import com.example.data.repository.StoreRepository
import com.example.ui.StoreViewModel
import com.example.ui.screens.MainScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private lateinit var database: AppDatabase
    private lateinit var repository: StoreRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "sarisari_store_db"
        ).fallbackToDestructiveMigration().build()

        repository = StoreRepository(database.productDao(), database.saleDao())

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: StoreViewModel = viewModel(
                        factory = StoreViewModel.Factory(repository)
                    )
                    MainScreen(viewModel)
                }
            }
        }
    }
}
