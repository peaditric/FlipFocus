package com.flipfocus.clock;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** Device-only focus timer state. Epoch deadline survives UI recreation and process death. */
final class FocusStore {
    private static final String PREF = "flipfocus_preferences";
    private static final String ACTION_FINISH = "com.flipfocus.clock.FINISH";
    private final Context context;
    private final SharedPreferences prefs;

    FocusStore(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = this.context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    int minutes() { return prefs.getInt("minutes", 25); }
    boolean running() { return prefs.getBoolean("running", false); }
    boolean keepAwake() { return prefs.getBoolean("awake", true); }
    boolean sound() { return prefs.getBoolean("sound", true); }

    long remainingMs() {
        if (running()) return Math.max(0, prefs.getLong("deadline", 0) - System.currentTimeMillis());
        return Math.max(0, prefs.getLong("remaining", minutes() * 60000L));
    }

    void start() {
        long left = remainingMs();
        if (left <= 0) left = minutes() * 60000L;
        long deadline = System.currentTimeMillis() + left;
        prefs.edit().putLong("remaining", left).putLong("deadline", deadline)
                .putBoolean("running", true).commit();
        schedule(deadline);
    }

    void pause() {
        if (!running()) return;
        long left = remainingMs();
        if (left == 0) {
            finishIfDue();
            return;
        }
        prefs.edit().putBoolean("running", false).putLong("deadline", 0)
                .putLong("remaining", left).commit();
        cancelAlarm();
    }

    void reset() {
        prefs.edit().putBoolean("running", false).putLong("deadline", 0)
                .putLong("remaining", minutes() * 60000L).commit();
        cancelAlarm();
    }

    void setMinutes(int minutes) {
        if (minutes < 1 || minutes > 99 || running()) return;
        prefs.edit().putInt("minutes", minutes).putLong("remaining", minutes * 60000L)
                .putLong("deadline", 0).commit();
        cancelAlarm();
    }

    void setOptions(boolean awake, boolean sound) {
        prefs.edit().putBoolean("awake", awake).putBoolean("sound", sound).apply();
    }

    /** Called from both Activity ticker and AlarmReceiver; completion is idempotent. */
    boolean finishIfDue() {
        if (!running() || System.currentTimeMillis() < prefs.getLong("deadline", 0)) return false;
        String today = dayKey();
        int sessions = today.equals(prefs.getString("stats_day", "")) ? prefs.getInt("sessions", 0) : 0;
        int totalMinutes = today.equals(prefs.getString("stats_day", "")) ? prefs.getInt("total_minutes", 0) : 0;
        prefs.edit()
                .putBoolean("running", false)
                .putLong("remaining", 0)
                .putLong("deadline", 0)
                .putString("stats_day", today)
                .putInt("sessions", sessions + 1)
                .putInt("total_minutes", totalMinutes + minutes())
                .commit();
        cancelAlarm();
        return true;
    }

    int sessionsToday() {
        return dayKey().equals(prefs.getString("stats_day", "")) ? prefs.getInt("sessions", 0) : 0;
    }

    int minutesToday() {
        return dayKey().equals(prefs.getString("stats_day", "")) ? prefs.getInt("total_minutes", 0) : 0;
    }

    private String dayKey() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
    }

    private PendingIntent pendingIntent() {
        Intent intent = new Intent(context, FocusAlarmReceiver.class).setAction(ACTION_FINISH);
        return PendingIntent.getBroadcast(context, 3134, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private void schedule(long timeMillis) {
        AlarmManager alarms = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarms == null) return;
        PendingIntent pi = pendingIntent();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarms.canScheduleExactAlarms()) {
            alarms.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeMillis, pi);
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            alarms.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeMillis, pi);
        } else {
            // Some HyperOS/Android versions do not grant exact alarm access.
            // Deadline remains authoritative; notification can arrive slightly late.
            alarms.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeMillis, pi);
        }
    }

    private void cancelAlarm() {
        AlarmManager alarms = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarms != null) alarms.cancel(pendingIntent());
    }
}
