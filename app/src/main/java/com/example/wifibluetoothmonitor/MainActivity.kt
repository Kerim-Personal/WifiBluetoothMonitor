package com.example.wifibluetoothmonitor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WifiBluetoothMonitorApp()
        }
    }
}

@Composable
fun WifiBluetoothMonitorApp() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Wi-Fi ve Bluetooth İzleyici Uygulaması",
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Button(
                onClick = { /* Start tracking logic */ },
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text("İzlemeyi Başlat")
            }
            Button(
                onClick = { /* Stop tracking logic */ }
            ) {
                Text("İzlemeyi Durdur")
            }
        }
    }
}