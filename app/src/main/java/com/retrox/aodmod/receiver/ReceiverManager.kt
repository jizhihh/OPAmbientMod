package com.retrox.aodmod.receiver

import android.app.AndroidAppHelper
import android.content.Intent
import android.content.IntentFilter
import com.retrox.aodmod.MainHook

object ReceiverManager {
    fun register() {
        kotlin.run {
            val intentFilter = IntentFilter()
            intentFilter.addAction("com.retrox.aodmod.NEW_MEDIA_META")
            val receiver = MediaMessageReceiver()
            AndroidAppHelper.currentApplication().registerReceiver(receiver, intentFilter)
            MainHook.logD("Receiver:: MediaMessageReceiver registered")
        }

        kotlin.run {
            val intentFilter = IntentFilter()
            intentFilter.addAction(Intent.ACTION_TIME_TICK)
            val receiver = ClockTickReceiver()
            AndroidAppHelper.currentApplication().registerReceiver(receiver, intentFilter)
            MainHook.logD("Receiver:: ClockTick registered ")
        }

        kotlin.run {
            val intentFilter = IntentFilter()
            intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED)
            val receiver = PowerReceiver()
            AndroidAppHelper.currentApplication().registerReceiver(receiver, intentFilter)
            MainHook.logD("Receiver:: PowerReceiver registered ")
        }

        kotlin.run {
            val intentFilter = IntentFilter()
            intentFilter.addAction("com.aodmod.sleep.on")
            intentFilter.addAction("com.aodmod.sleep.off")
            val receiver = SleepModeReceiver()
            AndroidAppHelper.currentApplication().registerReceiver(receiver, intentFilter)
            MainHook.logD("Receiver:: SleepModeReceiver registered ")
        }
    }
}