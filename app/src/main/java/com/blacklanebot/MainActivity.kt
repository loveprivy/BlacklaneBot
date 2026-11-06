package com.blacklanebot

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"

    private var tvStatus: TextView? = null
    private var btnToggleBot: Button? = null
    private var btnAccessibility: Button? = null
    private var btnNotification: Button? = null
    private var btnMockOffer: Button? = null
    private var tvLog: TextView? = null
    private var btnTestNotification: Button? = null
    private var tvTrialStatus: TextView? = null
    private var btnResetTrial: Button? = null
    private var rgMode: RadioGroup? = null
    private var rbAuto: RadioButton? = null
    private var rbManual: RadioButton? = null
    private var rgFilter: RadioGroup? = null
    private var rbAll: RadioButton? = null
    private var rbFlorida: RadioButton? = null
    private var etKeywords: EditText? = null
    private var etMinPayout: EditText? = null
    private var btnSaveConfig: Button? = null

    private val botReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            try {
                if (intent == null) return
                when (intent.action) {
                    "com.blacklanebot.BOT_ACTION" -> {
                        val status = intent.getStringExtra("status")
                        if (status != null) {
                            tvStatus?.text = status
                        }
                        updateUI()
                    }
                    "com.blacklanebot.BOT_LOG" -> {
                        val message = intent.getStringExtra("message")
                        if (message != null && tvLog != null) {
                            val currentText = tvLog?.text?.toString() ?: ""
                            tvLog?.text = "$message\n$currentText"
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in botReceiver: ", e)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_main)
            initViews()
            setupListeners()
            registerBotReceiver()
            updateUI()
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate: ", e)
            Toast.makeText(this, "App loaded with fallback setup", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initViews() {
        try {
            tvStatus = findViewById(R.id.tvStatus)
            btnToggleBot = findViewById(R.id.btnToggleBot)
            btnAccessibility = findViewById(R.id.btnAccessibility)
            btnNotification = findViewById(R.id.btnNotification)
            btnMockOffer = findViewById(R.id.btnMockOffer)
            tvLog = findViewById(R.id.tvLog)
            btnTestNotification = findViewById(R.id.btnTestNotification)
            tvTrialStatus = findViewById(R.id.tvTrialStatus)
            btnResetTrial = findViewById(R.id.btnResetTrial)
            rgMode = findViewById(R.id.rgMode)
            rbAuto = findViewById(R.id.rbAuto)
            rbManual = findViewById(R.id.rbManual)
            rgFilter = findViewById(R.id.rgFilter)
            rbAll = findViewById(R.id.rbAll)
            rbFlorida = findViewById(R.id.rbFlorida)
            etKeywords = findViewById(R.id.etKeywords)
            etMinPayout = findViewById(R.id.etMinPayout)
            btnSaveConfig = findViewById(R.id.btnSaveConfig)

            loadSavedConfig()
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing views: ", e)
        }
    }

    private fun loadSavedConfig() {
        try {
            val config = BotConfig.loadConfig(this)
            if (config.mode == BotConfig.MODE_AUTO) {
                rbAuto?.isChecked = true
            } else {
                rbManual?.isChecked = true
            }

            if (config.filterMode == BotConfig.FILTER_FLORIDA) {
                rbFlorida?.isChecked = true
            } else {
                rbAll?.isChecked = true
            }

            etKeywords?.setText(config.customKeywords.joinToString(", "))
            etMinPayout?.setText(config.minPayout.toString())
        } catch (e: Exception) {
            Log.e(TAG, "Error loading saved config: ", e)
        }
    }

    private fun setupListeners() {
        try {
            btnToggleBot?.setOnClickListener {
                try {
                    val currentState = BotConfig.isBotEnabled(this)
                    BotConfig.setBotEnabled(this, !currentState)
                    updateUI()
                } catch (e: Exception) {
                    Log.e(TAG, "Error toggling bot: ", e)
                }
            }

            btnAccessibility?.setOnClickListener {
                try {
                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                } catch (e: Exception) {
                    Log.e(TAG, "Error opening accessibility settings: ", e)
                    Toast.makeText(this, "Unable to open Accessibility Settings", Toast.LENGTH_SHORT).show()
                }
            }

            btnNotification?.setOnClickListener {
                try {
                    val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                } catch (e: Exception) {
                    Log.e(TAG, "Error opening notification settings: ", e)
                    Toast.makeText(this, "Unable to open Notification Settings", Toast.LENGTH_SHORT).show()
                }
            }

            btnMockOffer?.setOnClickListener {
                try {
                    val intent = Intent(this, MockBlacklaneActivity::class.java)
                    startActivity(intent)
                } catch (e: Exception) {
                    Log.e(TAG, "Error opening Mock Offer activity: ", e)
                }
            }

            btnTestNotification?.setOnClickListener {
                try {
                    TestNotificationHelper.sendTestNotification(this)
                } catch (e: Exception) {
                    Log.e(TAG, "Error sending test notification: ", e)
                }
            }

            btnResetTrial?.setOnClickListener {
                try {
                    TrialManager.resetTrial(this)
                    updateUI()
                } catch (e: Exception) {
                    Log.e(TAG, "Error resetting trial: ", e)
                }
            }

            btnSaveConfig?.setOnClickListener {
                try {
                    val mode = if (rbAuto?.isChecked == true) BotConfig.MODE_AUTO else BotConfig.MODE_MANUAL
                    val filterMode = if (rbFlorida?.isChecked == true) BotConfig.FILTER_FLORIDA else BotConfig.FILTER_ALL
                    val kwText = etKeywords?.text?.toString() ?: ""
                    val kwSet = kwText.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
                    val payoutStr = etMinPayout?.text?.toString() ?: "0"
                    val payout = payoutStr.toDoubleOrNull() ?: 0.0

                    val config = BotConfig(
                        mode = mode,
                        filterMode = filterMode,
                        customKeywords = kwSet,
                        minPayout = payout
                    )
                    BotConfig.saveConfig(this, config)
                    Toast.makeText(this, "Settings saved successfully", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Log.e(TAG, "Error saving config: ", e)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up listeners: ", e)
        }
    }

    private fun registerBotReceiver() {
        try {
            val filter = IntentFilter().apply {
                addAction("com.blacklanebot.BOT_ACTION")
                addAction("com.blacklanebot.BOT_LOG")
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(botReceiver, filter, Context.RECEIVER_EXPORTED)
            } else {
                registerReceiver(botReceiver, filter)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error registering receiver: ", e)
        }
    }

    private fun updateUI() {
        try {
            val isEnabled = BotConfig.isBotEnabled(this)
            btnToggleBot?.text = if (isEnabled) "Stop Bot" else "Start Bot"
            tvStatus?.text = if (isEnabled) "Status: Running" else "Status: Stopped"

            val daysRemaining = TrialManager.getDaysRemaining(this)
            tvTrialStatus?.text = "Trial Days Remaining: $daysRemaining"
        } catch (e: Exception) {
            Log.e(TAG, "Error updating UI: ", e)
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            updateUI()
        } catch (e: Exception) {
            Log.e(TAG, "Error in onResume: ", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(botReceiver)
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering receiver: ", e)
        }
    }
}
