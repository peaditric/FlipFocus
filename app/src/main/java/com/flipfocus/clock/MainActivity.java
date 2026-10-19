package com.flipfocus.clock;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class MainActivity extends Activity {
    private static final int BG = Color.rgb(11, 12, 14);
    private static final int PRIMARY = Color.rgb(245, 183, 124);
    private static final int WHITE = Color.rgb(247, 245, 238);
    private static final int MUTED = Color.rgb(144, 146, 151);
    private static final int PANEL = Color.rgb(27, 28, 32);
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final FlipDigitView[] digits = new FlipDigitView[4];
    private final TextView[] durations = new TextView[4];
    private final int[] presets = {15, 25, 45, 60};
    private FocusStore store;
    private boolean clockMode = false;
    private TextView clockTab, focusTab, heading, subheading, info, progressLabel;
    private TextView mainButton, todayMinutes, todaySessions;
    private LinearLayout focusUi;
    private ProgressBar progressBar;

    private final Runnable refreshRunnable = new Runnable() {
        @Override public void run() {
            refreshUi();
            handler.postDelayed(this, 240);
        }
    };

    @Override public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        store = new FocusStore(this);
        FocusNotifications.ensureChannels(this);
        buildUi();
        switchMode(false);
    }

    @Override protected void onResume() {
        super.onResume();
        handler.removeCallbacks(refreshRunnable);
        handler.post(refreshRunnable);
    }

    @Override protected void onPause() {
        handler.removeCallbacks(refreshRunnable);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onPause();
    }

    @Override protected void onDestroy() {
        handler.removeCallbacks(refreshRunnable);
        super.onDestroy();
    }

    private int dp(float x) { return Math.round(x * getResources().getDisplayMetrics().density); }
    private int c(String hex) { return Color.parseColor(hex); }

    private GradientDrawable background(int fill, int radiusDp, int stroke) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(fill);
        d.setCornerRadius(dp(radiusDp));
        if (stroke != Color.TRANSPARENT) d.setStroke(dp(1), stroke);
        return d;
    }

    private LinearLayout vertical() {
        LinearLayout x = new LinearLayout(this);
        x.setOrientation(LinearLayout.VERTICAL);
        return x;
    }
    private LinearLayout horizontal() {
        LinearLayout x = new LinearLayout(this);
        x.setOrientation(LinearLayout.HORIZONTAL);
        x.setGravity(Gravity.CENTER_VERTICAL);
        return x;
    }

    private LinearLayout.LayoutParams lp(int w, int h, int left, int top, int right, int bottom) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(w < 0 ? w : dp(w), h < 0 ? h : dp(h));
        p.setMargins(dp(left), dp(top), dp(right), dp(bottom));
        return p;
    }

    private TextView text(String content, float sp, int color, boolean bold, int gravity) {
        TextView v = new TextView(this);
        v.setText(content);
        v.setTextColor(color);
        v.setTextSize(sp);
        v.setGravity(gravity);
        if (bold) v.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        v.setIncludeFontPadding(false);
        return v;
    }

    private TextView pill(String content, boolean selected) {
        TextView v = text(content, 13f, selected ? PRIMARY : c("#B3B4B8"), selected, Gravity.CENTER);
        v.setPadding(dp(17), dp(11), dp(17), dp(11));
        v.setBackground(background(selected ? c("#33241B") : PANEL, 28, selected ? c("#A87752") : c("#383A40")));
        return v;
    }

    private void buildUi() {
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setVerticalScrollBarEnabled(false);
        scroll.setBackgroundColor(BG);
        LinearLayout body = vertical();
        int side = 20;
        body.setPadding(dp(side), dp(26), dp(side), dp(30));
        scroll.addView(body, new FrameLayout.LayoutParams(-1, -2));
        setContentView(scroll);

        LinearLayout toolbar = horizontal();
        TextView logo = text("◫", 25, PRIMARY, true, Gravity.CENTER);
        logo.setBackground(background(c("#261D17"), 11, c("#5C4130")));
        toolbar.addView(logo, lp(44, 44, 0, 0, 12, 0));
        LinearLayout appName = vertical();
        appName.addView(text("翻时 · 专注", 16, WHITE, true, Gravity.LEFT));
        appName.addView(text("FLIP & FOCUS", 10, MUTED, false, Gravity.LEFT), lp(-2, -2, 0, 5, 0, 0));
        toolbar.addView(appName, new LinearLayout.LayoutParams(0, -2, 1));
        TextView settings = text("⚙", 23, c("#C4C5C8"), false, Gravity.CENTER);
        settings.setBackground(background(PANEL, 26, c("#303238")));
        settings.setContentDescription("设置");
        settings.setOnClickListener(v -> showSettings());
        toolbar.addView(settings, lp(44, 44, 0, 0, 0, 0));
        body.addView(toolbar, lp(-1, -2, 0, 0, 0, 0));

        TextView free = text("100% 免费  ·  无广告  ·  无需账号", 12, PRIMARY, false, Gravity.CENTER);
        free.setPadding(dp(13), dp(9), dp(13), dp(9));
        free.setBackground(background(c("#211A15"), 24, c("#493726")));
        LinearLayout freeWrap = horizontal();
        freeWrap.setGravity(Gravity.CENTER);
        freeWrap.addView(free);
        body.addView(freeWrap, lp(-1, -2, 0, 53, 0, 0));

        heading = text("每一分钟，都值得专注。", 25, WHITE, true, Gravity.CENTER);
        body.addView(heading, lp(-1, -2, 0, 20, 0, 0));
        subheading = text("放下干扰，把重要的事做好。", 13, MUTED, false, Gravity.CENTER);
        body.addView(subheading, lp(-1, -2, 0, 12, 0, 0));

        LinearLayout tabs = horizontal();
        tabs.setGravity(Gravity.CENTER);
        tabs.setPadding(dp(5), dp(5), dp(5), dp(5));
        tabs.setBackground(background(c("#1C1D21"), 15, c("#303137")));
        clockTab = text("翻页时钟", 14, MUTED, false, Gravity.CENTER);
        focusTab = text("专注计时", 14, WHITE, true, Gravity.CENTER);
        clockTab.setPadding(dp(22), dp(11), dp(22), dp(11));
        focusTab.setPadding(dp(22), dp(11), dp(22), dp(11));
        tabs.addView(clockTab); tabs.addView(focusTab);
        clockTab.setOnClickListener(v -> switchMode(true));
        focusTab.setOnClickListener(v -> switchMode(false));
        LinearLayout tabsWrap = horizontal();
        tabsWrap.setGravity(Gravity.CENTER);
        tabsWrap.addView(tabs);
        body.addView(tabsWrap, lp(-1, -2, 0, 32, 0, 33));

        LinearLayout clockRow = horizontal();
        clockRow.setGravity(Gravity.CENTER_VERTICAL);
        for (int i = 0; i < digits.length; i++) {
            if (i == 2) {
                TextView colon = text(":", 51, c("#B1A59B"), false, Gravity.CENTER);
                clockRow.addView(colon, lp(23, -2, 0, 0, 0, 9));
            }
            digits[i] = new FlipDigitView(this);
            LinearLayout.LayoutParams dlp = new LinearLayout.LayoutParams(0, -2, 1f);
            dlp.setMargins(dp(i == 0 ? 0 : 3), 0, dp(i == 3 ? 0 : 3), 0);
            clockRow.addView(digits[i], dlp);
        }
        body.addView(clockRow, lp(-1, -2, 0, 0, 0, 0));
        info = text("", 13, c("#C3A98F"), false, Gravity.CENTER);
        body.addView(info, lp(-1, -2, 0, 27, 0, 29));

        focusUi = vertical();
        LinearLayout progressHeaders = horizontal();
        progressHeaders.addView(text("本次专注进度", 12, MUTED, false, Gravity.LEFT),
                new LinearLayout.LayoutParams(0, -2, 1f));
        progressLabel = text("0%", 12, MUTED, false, Gravity.RIGHT);
        progressHeaders.addView(progressLabel);
        focusUi.addView(progressHeaders, lp(-1, -2, 0, 0, 0, 11));
        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setMax(1000);
        progressBar.setProgressTintList(android.content.res.ColorStateList.valueOf(PRIMARY));
        progressBar.setProgressBackgroundTintList(android.content.res.ColorStateList.valueOf(c("#292A30")));
        focusUi.addView(progressBar, lp(-1, 7, 0, 0, 0, 16));

        HorizontalScrollView choicesScroll = new HorizontalScrollView(this);
        choicesScroll.setHorizontalScrollBarEnabled(false);
        LinearLayout choices = horizontal();
        choices.setGravity(Gravity.CENTER_VERTICAL);
        for (int i = 0; i < presets.length; i++) {
            int minutes = presets[i];
            TextView b = pill(minutes + "分钟", minutes == 25);
            durations[i] = b;
            choices.addView(b, lp(-2, -2, 0, 0, 9, 0));
            b.setOnClickListener(v -> { if (!store.running()) { store.setMinutes(minutes); refreshUi(); } });
        }
        TextView custom = pill("自定义", false);
        choices.addView(custom, lp(-2, -2, 0, 0, 0, 0));
        custom.setOnClickListener(v -> showSettings());
        choicesScroll.addView(choices);
        focusUi.addView(choicesScroll, lp(-1, -2, 0, 1, 0, 17));

        LinearLayout actions = horizontal();
        actions.setGravity(Gravity.CENTER);
        mainButton = text("开始专注", 16, c("#26170E"), true, Gravity.CENTER);
        mainButton.setPadding(dp(29), dp(17), dp(29), dp(17));
        mainButton.setBackground(background(PRIMARY, 15, Color.TRANSPARENT));
        actions.addView(mainButton, lp(-2, -2, 0, 0, 11, 0));
        mainButton.setOnClickListener(v -> {
            if (store.running()) {
                if (store.remainingMs() == 0) completeIfDue();
                else store.pause();
            } else {
                store.start();
                askNotificationPermission();
            }
            refreshUi();
        });
        TextView reset = text("重置", 15, WHITE, true, Gravity.CENTER);
        reset.setPadding(dp(26), dp(17), dp(26), dp(17));
        reset.setBackground(background(c("#2A2B31"), 15, c("#3B3C43")));
        reset.setOnClickListener(v -> { store.reset(); refreshUi(); });
        actions.addView(reset, lp(-2, -2, 0, 0, 0, 0));
        focusUi.addView(actions, lp(-1, -2, 0, 0, 0, 0));
        body.addView(focusUi, lp(-1, -2, 0, 0, 0, 0));

        LinearLayout statsRow = horizontal();
        todayMinutes = text("0", 29, WHITE, true, Gravity.LEFT);
        todaySessions = text("0", 29, WHITE, true, Gravity.LEFT);
        statsRow.addView(statCard("今天累计专注", todayMinutes, "分钟"),
                new LinearLayout.LayoutParams(0, -2, 1f));
        statsRow.addView(statCard("今天完成专注", todaySessions, "次"), lp(0, -2, 12, 0, 0, 0));
        // The second card also needs a weight when its width is 0.
        LinearLayout.LayoutParams second = (LinearLayout.LayoutParams) statsRow.getChildAt(1).getLayoutParams();
        second.weight = 1f;
        statsRow.getChildAt(1).setLayoutParams(second);
        body.addView(statsRow, lp(-1, -2, 0, 47, 0, 0));
        TextView footer = text("所有记录仅保存在本机  ·  完全离线", 11, c("#74767B"), false, Gravity.CENTER);
        body.addView(footer, lp(-1, -2, 0, 31, 0, 8));
    }

    private LinearLayout statCard(String label, TextView number, String unit) {
        LinearLayout box = vertical();
        box.setPadding(dp(16), dp(19), dp(10), dp(19));
        box.setBackground(background(c("#1B1C20"), 16, c("#292B30")));
        box.addView(text(label, 12, MUTED, false, Gravity.LEFT));
        LinearLayout row = horizontal();
        row.addView(number);
        row.addView(text("  " + unit, 12, MUTED, false, Gravity.BOTTOM), lp(-2, -2, 0, 10, 0, 3));
        box.addView(row, lp(-1, -2, 0, 11, 0, 0));
        return box;
    }

    private void switchMode(boolean showClock) {
        this.clockMode = showClock;
        clockTab.setTextColor(showClock ? WHITE : MUTED);
        focusTab.setTextColor(showClock ? MUTED : WHITE);
        clockTab.setTypeface(Typeface.DEFAULT, showClock ? Typeface.BOLD : Typeface.NORMAL);
        focusTab.setTypeface(Typeface.DEFAULT, showClock ? Typeface.NORMAL : Typeface.BOLD);
        clockTab.setBackground(showClock ? background(c("#36373D"), 10, Color.TRANSPARENT) : null);
        focusTab.setBackground(!showClock ? background(c("#36373D"), 10, Color.TRANSPARENT) : null);
        heading.setText(showClock ? "静静看时间流淌。" : "每一分钟，都值得专注。");
        subheading.setText(showClock ? "简洁、沉浸的翻页时钟。" : "放下干扰，把重要的事做好。");
        focusUi.setVisibility(showClock ? View.GONE : View.VISIBLE);
        for (FlipDigitView digit : digits) digit.clear();
        refreshUi();
    }

    private void completeIfDue() {
        if (store.finishIfDue()) FocusNotifications.notifyComplete(this, store);
    }

    private void refreshUi() {
        if (store == null || digits[0] == null) return;
        completeIfDue();
        if (clockMode) {
            Date now = new Date();
            setTime(new SimpleDateFormat("HHmm", Locale.US).format(now));
            info.setText(new SimpleDateFormat("yyyy年M月d日 EEEE · ss秒", Locale.CHINA).format(now));
        } else {
            long seconds = (store.remainingMs() + 999L) / 1000L;
            int minutes = (int) (seconds / 60);
            int secs = (int) (seconds % 60);
            setTime(String.format(Locale.US, "%02d%02d", minutes, secs));
            boolean active = store.running();
            long full = store.minutes() * 60000L;
            int progress = (int) Math.max(0, Math.min(1000, (full - store.remainingMs()) * 1000L / full));
            progressBar.setProgress(progress);
            progressLabel.setText((int) Math.round(progress / 10.0) + "%");
            mainButton.setText(active ? "暂停专注" : seconds == 0 ? "再次专注" :
                    seconds < store.minutes() * 60L ? "继续专注" : "开始专注");
            info.setText(active ? "● 正在专注  ·  只做这一件事" : seconds == 0 ?
                    "✦ 专注完成  ·  休息一下吧" : seconds < store.minutes() * 60L ?
                    "已暂停  ·  随时可以继续" : "一段专注，从现在开始");
            for (int i = 0; i < presets.length; i++) {
                boolean selected = presets[i] == store.minutes();
                durations[i].setTextColor(selected ? PRIMARY : c("#B3B4B8"));
                durations[i].setBackground(background(selected ? c("#33241B") : PANEL,
                        28, selected ? c("#A87752") : c("#383A40")));
                durations[i].setAlpha(active ? .48f : 1f);
            }
        }
        todayMinutes.setText(String.valueOf(store.minutesToday()));
        todaySessions.setText(String.valueOf(store.sessionsToday()));
        if (store.keepAwake() && (clockMode || store.running())) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    private void setTime(String fourDigits) {
        if (fourDigits.length() != 4) return;
        for (int i = 0; i < 4; i++) digits[i].setDigit(fourDigits.charAt(i));
    }

    private void askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 3136);
        }
    }

    private void showSettings() {
        LinearLayout form = vertical();
        form.setPadding(dp(22), dp(14), dp(22), dp(0));
        TextView tip = text("自定义专注时长（1–99分钟）", 14, WHITE, false, Gravity.LEFT);
        form.addView(tip, lp(-1, -2, 0, 0, 0, 10));
        EditText minutes = new EditText(this);
        minutes.setInputType(InputType.TYPE_CLASS_NUMBER);
        minutes.setSingleLine(true);
        minutes.setSelectAllOnFocus(true);
        minutes.setText(String.valueOf(store.minutes()));
        minutes.setTextColor(WHITE);
        form.addView(minutes, lp(-1, 52, 0, 0, 0, 15));
        CheckBox awake = new CheckBox(this);
        awake.setText("专注或时钟界面保持屏幕常亮");
        awake.setTextColor(WHITE);
        awake.setChecked(store.keepAwake());
        form.addView(awake, lp(-1, -2, 0, 0, 0, 7));
        CheckBox sound = new CheckBox(this);
        sound.setText("专注结束提醒音");
        sound.setTextColor(WHITE);
        sound.setChecked(store.sound());
        form.addView(sound);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("翻时专注 · 设置")
                .setView(form)
                .setNegativeButton("取消", (d, which) -> {})
                .setPositiveButton("保存", null)
                .create();
        dialog.setOnShowListener(v -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
            int value;
            try { value = Integer.parseInt(minutes.getText().toString().trim()); }
            catch (NumberFormatException e) { minutes.setError("请输入1–99之间的整数"); return; }
            if (value < 1 || value > 99) { minutes.setError("请输入1–99之间的整数"); return; }
            if (store.running() && value != store.minutes()) {
                minutes.setError("请先暂停并重置计时，才能修改时长");
                return;
            }
            store.setOptions(awake.isChecked(), sound.isChecked());
            if (!store.running()) store.setMinutes(value);
            dialog.dismiss();
            refreshUi();
        }));
        dialog.show();
    }
}
