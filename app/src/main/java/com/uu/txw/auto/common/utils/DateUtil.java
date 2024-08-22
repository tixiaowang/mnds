package com.uu.txw.auto.common.utils;

import android.annotation.SuppressLint;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtil {

    public static long ONE_HOUR_TIME_MILLIMS = 60 * 60 * 1000;

    @SuppressLint("SimpleDateFormat")
    public static String getyMd(long time) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return simpleDateFormat.format(new Date(time));
    }

    @SuppressLint("SimpleDateFormat")
    public static String getMdHms(long time) {
        return new SimpleDateFormat("MM-dd HH:mm:ss").format(new Date(time));
    }

    @SuppressLint("SimpleDateFormat")
    public static String getMdHm(long time) {
        return new SimpleDateFormat("MM-dd HH:mm").format(new Date(time));
    }

    @SuppressLint("SimpleDateFormat")
    public static String getyMdHms(long time) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(time));
    }

    @SuppressLint("SimpleDateFormat")
    public static String stringToString(String dateString) {
        String formattedTime = "";
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss");
            // SimpleDateFormat的parse(String time)方法将String转换为Date
            Date date = simpleDateFormat.parse(dateString);
            simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日");
            // SimpleDateFormat的format(Date date)方法将Date转换为String
            formattedTime = simpleDateFormat.format(date);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return formattedTime;
    }

    /**
     * 获取日期和星期,01-25 周一
     *
     * @param ctime
     * @return 获取星期
     */
    public static String getDateAndWeek(String ctime) {
        StringBuffer dateTemp = new StringBuffer();
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    .parse(ctime));
            dateTemp.append(new SimpleDateFormat("MM-dd", Locale.getDefault())
                    .format(cal.getTime()));
            switch (cal.get(Calendar.DAY_OF_WEEK)) {
                case 1:
                    dateTemp.append("  周日");
                    break;
                case 2:
                    dateTemp.append("  周一");
                    break;
                case 3:
                    dateTemp.append("  周二");
                    break;
                case 4:
                    dateTemp.append("  周三");
                    break;
                case 5:
                    dateTemp.append("  周四");
                    break;
                case 6:
                    dateTemp.append("  周五");
                    break;
                case 7:
                    dateTemp.append("  周六");
                    break;
                default:
                    break;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dateTemp.toString();
    }
}
