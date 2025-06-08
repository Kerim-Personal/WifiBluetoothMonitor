// app/src/main/java/com/example/wifibluetoothmonitor/MonitorService.kt
package com.example.wifibluetoothmonitor

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import java.util.concurrent.TimeUnit

class MonitorService : Service() {

    private val TAG = "MonitorService"
    private lateinit var handler: Handler
    private lateinit var wifiManager: WifiManager
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private val checkIntervalMillis = TimeUnit.MINUTES.toMillis(5) // 5 dakika

    companion object {
        const val CHANNEL_ID = "MonitorServiceChannel"
        const val NOTIFICATION_ID = 1
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service onCreate")
        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val bluetoothManager = applicationContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        handler = Handler(Looper.getMainLooper())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service onStartCommand")
        createNotificationChannel()
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Wi-Fi ve Bluetooth İzleyici")
            .setContentText("Uygulama arka planda çalışıyor.")
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Kendi ikonunuzu ayarlayın
            .setContentIntent(pendingIntent)
            .build()

        startForeground(NOTIFICATION_ID, notification)

        startMonitoring()
        return START_STICKY
    }

    private fun startMonitoring() {
        Log.d(TAG, "Monitoring started with interval: ${checkIntervalMillis / 1000} seconds")
        handler.postDelayed(object : Runnable {
            override fun run() {
                checkAndToggleWifiBluetooth()
                handler.postDelayed(this, checkIntervalMillis)
            }
        }, checkIntervalMillis)
    }

    private fun checkAndToggleWifiBluetooth() {
        Log.d(TAG, "Checking Wi-Fi and Bluetooth status...")

        // Wi-Fi Durumunu Kontrol Et ve Kapat (eğer açıksa)
        if (wifiManager.isWifiEnabled) {
            // Burada Wi-Fi kullanım süresini kontrol etmeniz gerekir.
            // Bu kısım için şimdilik doğrudan kapatma örneği gösteriyorum.
            // Gerçek uygulamada, en son kullanıldığı zamanı depolayıp karşılaştırmalısınız.
            Log.d(TAG, "Wi-Fi açık, kapatılıyor...")
            wifiManager.isWifiEnabled = false
            showToast("Wi-Fi kapatıldı.")
        } else {
            Log.d(TAG, "Wi-Fi zaten kapalı.")
        }

        // Bluetooth Durumunu Kontrol Et ve Kapat (eğer açıksa)
        if (bluetoothAdapter.isEnabled) {
            // Burada Bluetooth kullanım süresini kontrol etmeniz gerekir.
            // Bu kısım için şimdilik doğrudan kapatma örneği gösteriyorum.
            // Gerçek uygulamada, en son kullanıldığı zamanı depolayıp karşılaştırmalısınız.
            Log.d(TAG, "Bluetooth açık, kapatılıyor...")
            bluetoothAdapter.disable()
            showToast("Bluetooth kapatıldı.")
        } else {
            Log.d(TAG, "Bluetooth zaten kapalı.")
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Monitor Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun showToast(message: String) {
        // Toast mesajlarını ana iş parçacığında göstermek için Handler kullanın
        Handler(Looper.getMainLooper()).post {
            //Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
            Log.d(TAG, "Toast: $message") // Servis içinden doğrudan Toast göstermek zor olabilir, Log kullanın.
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service onDestroy")
        handler.removeCallbacksAndMessages(null) // Tüm bekleyen görevleri kaldır
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}