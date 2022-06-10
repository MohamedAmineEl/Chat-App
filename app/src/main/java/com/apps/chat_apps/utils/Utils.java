package com.apps.chat_apps.utils;

import android.content.Context;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.widget.Toast;

import java.util.Calendar;

public class Utils {
    public static void showToast(Context ctx, String message) {
        Toast.makeText(ctx, message, Toast.LENGTH_SHORT).show();
    }

    public static String formatDate(long timestamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        if (DateUtils.isToday(timestamp)) {
            return DateFormat.format("hh:mm aa", calendar).toString();
        } else if (DateUtils.isToday(timestamp + DateUtils.DAY_IN_MILLIS)) {
            return "Yesterday";
        } else {
            int thisYear = Calendar.getInstance().get(Calendar.YEAR);
            int year = calendar.get(Calendar.YEAR);
            if (thisYear == year) {
                return DateFormat.format("MMM dd", calendar).toString();
            } else {
                return DateFormat.format("MMM dd, yyyy", calendar).toString();
            }
        }
    }
}
