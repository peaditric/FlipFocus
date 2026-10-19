package com.flipfocus.clock;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

final class FocusNotifications {
    private static final String SOUND_CHANNEL = "focus_done_sound_v1";
    private static final String SILENT_CHANNEL = "focus_done_silent_v1";

    static void ensureChannels(Context context) {
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (manager == null) return;
        NotificationChannel alert = new NotificationChannel(SOUND_CHANNEL, "专注完成提醒", NotificationManager.IMPORTANCE_HIGH);
        alert.setDescription("专注时间结束时提醒");
        manager.createNotificationChannel(alert);
        NotificationChannel silent = new NotificationChannel(SILENT_CHANNEL, "静音完成提醒", NotificationManager.IMPORTANCE_DEFAULT);
        silent.setDescription("仅显示提醒，不发出提示音");
        silent.setSound(null, null);
        silent.enableVibration(false);
        manager.createNotificationChannel(silent);
    }

    static void notifyComplete(Context context, FocusStore store) {
        if (Build.VERSION.SDK_INT >= 33 && context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return;
        ensureChannels(context);
        String channel = store.sound() ? SOUND_CHANNEL : SILENT_CHANNEL;
        Intent open = new Intent(context, MainActivity.class);
        open.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi = PendingIntent.getActivity(context, 3135, open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        Notification notification = new Notification.Builder(context, channel)
                .setSmallIcon(android.R.drawable.ic_popup_reminder)
                .setContentTitle("专注完成 ✨")
                .setContentText("恭喜完成 " + store.minutes() + " 分钟专注，休息一下吧！")
                .setContentIntent(pi)
                .setAutoCancel(true)
                .build();
        NotificationManager mgr = context.getSystemService(NotificationManager.class);
        if (mgr != null) mgr.notify(3135, notification);
    }
}
