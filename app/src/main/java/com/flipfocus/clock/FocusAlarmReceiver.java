package com.flipfocus.clock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public final class FocusAlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        FocusStore store = new FocusStore(context);
        if (store.finishIfDue()) FocusNotifications.notifyComplete(context, store);
    }
}
