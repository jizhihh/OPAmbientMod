package com.retrox.aodmod.service.notification

import android.app.AndroidAppHelper
import android.app.Notification
import android.arch.lifecycle.MutableLiveData
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.retrox.aodmod.MainHook
import com.retrox.aodmod.state.AodMedia
import de.robv.android.xposed.XposedHelpers

object NotificationManager {

    const val POSTED = "Posted"
    const val REMOVED = "Removed"
    const val REFRESHED = "Refreshed"

    val notificationStatusLiveData = MutableLiveData<Pair<StatusBarNotification, String>>() // 建议拿到后从Map二次查表确认

    val notificationMap = mutableMapOf<String, StatusBarNotification>()

    fun onNotificationPosted(sbn: StatusBarNotification, rankingMap: NotificationListenerService.RankingMap) {
        notificationMap[sbn.key] = sbn
        val notification = sbn.notification
        notification.debugMessage()
        if (notification.channelId != "后台服务图标") notificationStatusLiveData.postValue(sbn to "Posted")
    }

    fun removeNotification(sbn: StatusBarNotification, rankingMap: NotificationListenerService.RankingMap) {
        notificationMap.remove(sbn.key)
        sbn.notification.debugMessage(type = "Removed")
        notificationStatusLiveData.postValue(sbn to "Removed")
    }

    fun addNotification(sbn: StatusBarNotification, rankingMap: NotificationListenerService.RankingMap) {
        notificationMap[sbn.key] = sbn
        sbn.notification.debugMessage(type = "Added")
    }

    fun resetState() {
        notificationStatusLiveData.postValue(null)
        notificationMap.clear()
    }

    fun notifyRefresh() {
        notificationStatusLiveData.postValue(null)

        // todo 按理说不应该放在这里 但是也没啥办法
        val musicActive = notificationMap.values.any {
            val hasMediaSession = XposedHelpers.callMethod(it.notification, "hasMediaSession") as Boolean
            hasMediaSession
        }
        val musicNotification = notificationMap.values.any { // 避免一部分人不开系统通知栏
            it.packageName == "com.tencent.qqmusic" || it.packageName == "com.netease.cloudmusic" || it.packageName == "code.name.monkey.retromusic"
        }
        if (!musicActive && !musicNotification) {
            AodMedia.aodMediaLiveData.postValue(null)
        }
    }
}

fun Notification.debugMessage(type: String = "Posted") {
    val (appName, title, content, isOnGoing) = getNotificationData()
    val hasMediaSession = XposedHelpers.callMethod(this, "hasMediaSession") as Boolean
    val extras = extras
    val messages = extras.getParcelableArray(Notification.EXTRA_MESSAGES)
    val histMessages = extras.getParcelableArray(Notification.EXTRA_HISTORIC_MESSAGES)

// todo 兼容Nevo增强的通知
//    try {
//        val newMessages = XposedHelpers.callStaticMethod(Notification.MessagingStyle.Message::class.java, "getMessagesFromBundleArray", messages) as List<Notification.MessagingStyle.Message>
//        val newHistoricMessages = XposedHelpers.callStaticMethod(Notification.MessagingStyle.Message::class.java, "getMessagesFromBundleArray", histMessages) as List<Notification.MessagingStyle.Message>
//        newMessages.forEach {
//            MainHook.logD("new message : ${it.text}")
//        }
//        newHistoricMessages.forEach {
//            MainHook.logD("newHistroy message : ${it.text}")
//
//        }
//    } catch (e: Exception) {
//
//    }


    MainHook.logD("通知调试: type: $type 应用->$appName 标题->$title 内容->$content OnGoing->$isOnGoing hasMeidaSession: $hasMediaSession visbility: $visibility  priority: $priority")
}

fun Notification.getNotificationData(): NotificationData {
    val builder = Notification.Builder.recoverBuilder(AndroidAppHelper.currentApplication().applicationContext, this)
    val appName = XposedHelpers.callMethod(builder, "loadHeaderAppName") as String

    val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
    val content = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: "" // 不能直接取String Spannable的时候会CastException
    val isOnGoing = flags and Notification.FLAG_ONGOING_EVENT

    return NotificationData(appName, title, content, isOnGoing > 0)
}

data class NotificationData(val appName: String, val title: String, val content: String, val isOnGoing: Boolean)