// app/src/main/java/com/example/wifibluetoothmonitor/MainActivity.kt
package com.example.wifibluetoothmonitor

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var statusTextView: TextView
    private lateinit var startButton: Button
    private lateinit var stopButton: Button

    // Android 12 (API 31) ve üzeri için Bluetooth izinlerini işlemek üzere tasarlanmış ActivityResultLauncher.
    // Bu, modern Android izin yönetiminin tercih edilen yoludur.
    private val requestBluetoothPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            // Her bir iznin verilip verilmediğini kontrol ediyoruz.
            val bluetoothConnectGranted = permissions[Manifest.permission.BLUETOOTH_CONNECT] ?: false
            val bluetoothScanGranted = permissions[Manifest.permission.BLUETOOTH_SCAN] ?: false

            if (bluetoothConnectGranted && bluetoothScanGranted) {
                // Eğer her iki Bluetooth izni de verilmişse, servisi başlatabiliriz.
                Toast.makeText(this, "Bluetooth izinleri verildi.", Toast.LENGTH_SHORT).show()
                startMonitorService()
            } else {
                // İzinler verilmediyse kullanıcıyı bilgilendiriyoruz.
                Toast.makeText(this, "Bluetooth izinleri reddedildi. Uygulama düzgün çalışmayabilir.", Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // UI bileşenlerini ID'leriyle bağlıyoruz.
        statusTextView = findViewById(R.id.statusTextView)
        startButton = findViewById(R.id.startButton)
        stopButton = findViewById(R.id.stopButton)

        statusTextView.text = "Wi-Fi ve Bluetooth İzleyici Uygulaması Hazır."

        // Başlat butonuna tıklama dinleyicisi ekliyoruz.
        startButton.setOnClickListener {
            // İzinleri kontrol edip isteme metodunu çağırıyoruz.
            checkAndRequestPermissions()
        }

        // Durdur butonuna tıklama dinleyicisi ekliyoruz.
        stopButton.setOnClickListener {
            // Servisi durdurma metodunu çağırıyoruz.
            stopMonitorService()
        }
    }

    /**
     * Gerekli izinleri kontrol eder ve henüz verilmemişse kullanıcıdan ister.
     * Android sürümüne göre farklı Bluetooth izinlerini ele alır.
     */
    private fun checkAndRequestPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // Android 12 (API 31) ve üzeri için
            // BLUETOOTH_CONNECT ve BLUETOOTH_SCAN izinlerini kontrol et ve listeye ekle.
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.BLUETOOTH_CONNECT)
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.BLUETOOTH_SCAN)
            }
        } else { // Android 11 (API 30) ve altı için
            // Eski Bluetooth izinlerini kontrol et ve listeye ekle.
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.BLUETOOTH)
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.BLUETOOTH_ADMIN)
            }
        }

        // Wi-Fi izinleri (ACCESS_WIFI_STATE, CHANGE_WIFI_STATE) genellikle "normal" izinlerdir
        // ve çoğu durumda çalışma zamanı izni istemeyi gerektirmezler (Manifest'te tanımlanmaları yeterlidir).
        // Ancak uygulamanızda bir sorun olursa, bu izinlerin de kontrol edildiğinden emin olun.
        // Foreground Service izni de (FOREGROUND_SERVICE) normal bir izindir.

        if (permissionsToRequest.isNotEmpty()) {
            // İstenmesi gereken izinler varsa, ilgili metodu çağırıyoruz.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // Android 12+ için ActivityResultLauncher'ı kullanıyoruz.
                requestBluetoothPermissions.launch(permissionsToRequest.toTypedArray())
            } else {
                // Eski Android sürümleri için ActivityCompat.requestPermissions kullanıyoruz.
                requestPermissions(permissionsToRequest.toTypedArray(), /* İstek kodu */ 100)
            }
        } else {
            // Tüm gerekli izinler zaten verilmişse, doğrudan servisi başlatıyoruz.
            startMonitorService()
        }
    }

    /**
     * İzin isteklerinin sonucunu işler.
     * Eski Android sürümleri için (Android 11 ve altı) onRequestPermissionsResult kullanılır.
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // İzin isteği kodumuzla eşleşip eşleşmediğini kontrol ediyoruz.
        if (requestCode == /* İstek kodu */ 100) {
            var allPermissionsGranted = true
            // Her bir iznin sonucunu kontrol ediyoruz.
            for (result in grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false
                    break
                }
            }
            if (allPermissionsGranted) {
                // Tüm izinler verilmişse servisi başlatıyoruz.
                Toast.makeText(this, "Tüm gerekli izinler verildi.", Toast.LENGTH_SHORT).show()
                startMonitorService()
            } else {
                // İzinler reddedilmişse kullanıcıyı bilgilendiriyoruz.
                Toast.makeText(this, "Gerekli izinler reddedildi. Uygulama düzgün çalışmayabilir.", Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * Arka plan izleme servisini başlatır.
     * Android 8.0 (API 26) ve sonrası için ContextCompat.startForegroundService kullanılır.
     */
    private fun startMonitorService() {
        val serviceIntent = Intent(this, MonitorService::class.java)
        // Servisi ön plan servisi olarak başlatıyoruz.
        // Bu, uygulamanız arka planda çalışırken bile sistemin servisi sonlandırmasını önler.
        ContextCompat.startForegroundService(this, serviceIntent)
        statusTextView.text = "İzleme servisi başlatıldı."
        Toast.makeText(this, "İzleme başlatıldı.", Toast.LENGTH_SHORT).show()
    }

    /**
     * Arka plan izleme servisini durdurur.
     */
    private fun stopMonitorService() {
        val serviceIntent = Intent(this, MonitorService::class.java)
        stopService(serviceIntent)
        statusTextView.text = "İzleme servisi durduruldu."
        Toast.makeText(this, "İzleme durduruldu.", Toast.LENGTH_SHORT).show()
    }
}