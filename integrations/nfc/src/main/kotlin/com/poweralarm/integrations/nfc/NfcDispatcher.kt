package com.poweralarm.integrations.nfc

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.nfc.Tag
import java.util.Locale

class NfcDispatcher(private val activity: Activity) {

    private val adapter: NfcAdapter? = NfcAdapter.getDefaultAdapter(activity)

    fun enableForegroundDispatch() {
        val nfc = adapter ?: return
        val pi = PendingIntent.getActivity(
            activity,
            0,
            Intent(activity, activity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_MUTABLE,
        )
        val filters = arrayOf(
            IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED),
            IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED),
            IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED),
        )
        nfc.enableForegroundDispatch(activity, pi, filters, null)
    }

    fun disableForegroundDispatch() {
        adapter?.disableForegroundDispatch(activity)
    }

    fun extractUid(intent: Intent): String? {
        val tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG) as? Tag ?: return null
        return tag.id.joinToString("") { "%02X".format(it) }.uppercase(Locale.ROOT)
    }
}
