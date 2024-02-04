package org.vpncore.vpncore_android

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.VpnService
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import de.blinkt.openvpn.OpenVpnApi
import de.blinkt.openvpn.core.OpenVPNService
import de.blinkt.openvpn.core.OpenVPNThread
import de.blinkt.openvpn.core.VpnStatus
import org.vpncore.vpncore_android.databinding.ActivityMainBinding
import java.io.BufferedReader

class MainActivity : AppCompatActivity() {
    companion object {
        private const val OPENVPN_PERMISSION = 1
        private const val COUNTRY = "Stockholm"
    }

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.activityMainConnectButton.setOnClickListener {
            val vpnIntent = VpnService.prepare(applicationContext)

            if (vpnIntent != null) {
                ActivityCompat.startActivityForResult(this, vpnIntent, OPENVPN_PERMISSION, null)
            } else {
                val config = applicationContext
                    .assets
                    .open("test.conf")
                    .bufferedReader()
                    .use(BufferedReader::readText)
                OpenVpnApi.startVpn(applicationContext, config, COUNTRY)
                binding.connected = true
            }
        }

        binding.activityMainDisconnectButton.setOnClickListener {
            val stopped = OpenVPNThread.stop()
            binding.connected = !stopped
        }

        VpnStatus.initLogCache(applicationContext.cacheDir)
        OpenVPNService.getStatus()?.let { binding.stage = it }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != OPENVPN_PERMISSION) {
            return
        }
        if (resultCode == Activity.RESULT_OK) {
            val config = applicationContext
                .assets
                .open("test.conf")
                .bufferedReader()
                .use(BufferedReader::readText)
            OpenVpnApi.startVpn(applicationContext, config, COUNTRY)
            binding.connected = true
        } else {
            Toast.makeText(applicationContext, "Permission denied!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager
            .getInstance(applicationContext)
            .registerReceiver(broadcastReceiverHandler, IntentFilter("connectionState"))
    }

    override fun onPause() {
        LocalBroadcastManager
            .getInstance(applicationContext)
            .unregisterReceiver(broadcastReceiverHandler)
        super.onPause()
    }

    override fun onDestroy() {
        OpenVPNThread.stop()
        super.onDestroy()
    }

    private val broadcastReceiverHandler = object: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.getStringExtra("state")?.let { runOnUiThread { binding.stage = it } }

            val duration = intent?.getStringExtra("duration") ?: "00:00:00"
            val byteIn = intent?.getStringExtra("byteIn") ?: "0"
            val byteOut = intent?.getStringExtra("byteOut") ?: "0"

            runOnUiThread {
                binding.duration = duration
                binding.byteIn = byteIn
                binding.byteOut = byteOut
            }
        }
    }
}