package com.example.minovatestapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import com.example.minovatestapp.util.getIntOrZero
import com.example.minovatestapp.util.hide
import com.example.minovatestapp.util.show
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import no.entur.android.nfc.external.ExternalNfcReaderCallback
import no.entur.android.nfc.external.ExternalNfcServiceAdapter
import no.entur.android.nfc.external.ExternalNfcServiceCallback
import no.entur.android.nfc.external.ExternalNfcTagCallback
import no.entur.android.nfc.external.minova.reader.McrReader
import no.entur.android.nfc.external.minova.service.MinovaService
import no.entur.android.nfc.wrapper.Tag

class MainActivity : AppCompatActivity(), ExternalNfcServiceCallback, View.OnClickListener {
    val TAG = "MainActivity"

    private var externalReaderService: ExternalNfcServiceAdapter? = null
    private var mcrReader: McrReader? = null

    private lateinit var commandSelector: RadioGroup
    private lateinit var radioBuzz: RadioButton
    private lateinit var radioDisplayText: RadioButton
    private lateinit var radioDisplayTextDelayed: RadioButton

    private lateinit var editTextDuration: EditText
    private lateinit var editTextTimes: EditText
    private lateinit var editTextXAxis: EditText
    private lateinit var editTextYAxis: EditText
    private lateinit var editTextFont: EditText
    private lateinit var editTextText: EditText
    private lateinit var editTextDelay: EditText

    private lateinit var commandButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initService()
        registerReceivers()
        startService()

        initUi()
    }


    private fun initService() {
        externalReaderService = ExternalNfcServiceAdapter(
            this,
            MinovaService::class.java,
            false
        )
    }

    private fun initUi() {
        commandSelector = findViewById(R.id.commandSelector)
        radioBuzz = findViewById(R.id.buzz)
        radioDisplayText = findViewById(R.id.displayText)
        radioDisplayTextDelayed = findViewById(R.id.displayTextDelayed)

        editTextDuration = findViewById(R.id.duration)
        editTextTimes = findViewById(R.id.times)

        editTextXAxis = findViewById(R.id.displayXAxis)
        editTextYAxis = findViewById(R.id.displayYAxis)
        editTextFont = findViewById(R.id.fontType)
        editTextText = findViewById(R.id.text)
        editTextDelay = findViewById(R.id.delay)

        commandButton = findViewById(R.id.commandButton)

        commandSelector.setOnCheckedChangeListener { _, checkedId ->
            hideEditTexts()
            when (findViewById<RadioButton>(checkedId)) {
                radioBuzz -> {
                    editTextDuration.show()
                    editTextTimes.show()
                }
                radioDisplayText -> {
                    editTextXAxis.show()
                    editTextYAxis.show()
                    editTextFont.show()
                    editTextText.show()
                }
                radioDisplayTextDelayed -> {
                    editTextXAxis.show()
                    editTextYAxis.show()
                    editTextFont.show()
                    editTextText.show()
                    editTextDelay.show()
                }
            }
        }

        commandButton.setOnClickListener(this)
    }

    private fun hideEditTexts() {
        editTextDuration.hide()
        editTextTimes.hide()
        editTextXAxis.hide()
        editTextYAxis.hide()
        editTextFont.hide()
        editTextText.hide()
        editTextDelay.hide()
    }

    override fun onClick(v: View?) {
        val selectedRadioButton = commandSelector.checkedRadioButtonId
        when (findViewById<RadioButton>(selectedRadioButton)) {
            radioBuzz -> {
                lifecycleScope.launch(Dispatchers.IO) {
                    mcrReader?.buzz(
                        editTextDuration.text.getIntOrZero(),
                        editTextTimes.text.getIntOrZero()
                    )
                }
            }
            radioDisplayText -> {
                lifecycleScope.launch(Dispatchers.IO) {
                    mcrReader?.displayText(
                        editTextXAxis.text.getIntOrZero(),
                        editTextYAxis.text.getIntOrZero(),
                        editTextFont.text.getIntOrZero(),
                        editTextText.text.toString()
                    )
                }
            }
            radioDisplayTextDelayed -> {
                lifecycleScope.launch(Dispatchers.IO) {
                    mcrReader?.displayTextDelayed(
                        editTextXAxis.text.getIntOrZero(),
                        editTextYAxis.text.getIntOrZero(),
                        editTextFont.text.getIntOrZero(),
                        editTextText.text.toString(),
                        editTextDelay.text.getIntOrZero()
                    )
                }
            }
        }
    }

    private fun startService() {
        externalReaderService?.startService(bundleOf())
    }

    private fun registerReceivers() {
        val tagReceiver: BroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent) {
                when (intent.action) {
                    ExternalNfcTagCallback.ACTION_TAG_DISCOVERED -> {
                        Log.d(TAG, "Got Tag!")
                        val tag: Tag? = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
                        lifecycleScope.launch(Dispatchers.IO) {
                            mcrReader?.buzz(40, 1)
                        }
                    }
                    ExternalNfcReaderCallback.ACTION_READER_OPENED -> {
                        Log.d(TAG, "Got Reader!")
                        mcrReader =
                            intent.getParcelableExtra(ExternalNfcReaderCallback.EXTRA_READER_CONTROL)
                    }
                    else -> {
                        Log.d(TAG, "Ignore action " + intent.action)
                    }
                }
            }
        }

        registerReceiver(tagReceiver, IntentFilter(ExternalNfcTagCallback.ACTION_TAG_DISCOVERED))
        registerReceiver(tagReceiver, IntentFilter(ExternalNfcReaderCallback.ACTION_READER_OPENED))
    }

    override fun onExternalNfcServiceStarted(p0: Intent?) {
        Log.d(TAG, "Service started!")
    }

    override fun onExternalNfcServiceStopped(p0: Intent?) {
        Log.d(TAG, "Service stopped!")
    }

}