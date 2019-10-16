package com.example.cliforcast.Util;

import android.content.Context;
import android.util.Log;

import com.example.cliforcast.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import saman.zamani.persiandate.PersianDate;
import saman.zamani.persiandate.PersianDateFormat;

public class Utility {
    public static String idToStringMapper(Context context, int id) {
        switch (id) {
            case 200:
                return context.getString(R.string.x200);
            case 201:
                return context.getString(R.string.x201);
            case 202:
                return context.getString(R.string.x202);
            case 210:
                return context.getString(R.string.x210);
            case 211:
                return context.getString(R.string.x211);
            case 212:
                return context.getString(R.string.x212);
            case 221:
                return context.getString(R.string.x221);
            case 230:
                return context.getString(R.string.x230);
            case 231:
                return context.getString(R.string.x231);
            case 232:
                return context.getString(R.string.x232);
            case 300:
                return context.getString(R.string.x300);
            case 301:
                return context.getString(R.string.x301);
            case 302:
                return context.getString(R.string.x302);
            case 310:
                return context.getString(R.string.x310);
            case 311:
                return context.getString(R.string.x311);
            case 312:
                return context.getString(R.string.x312);
            case 313:
                return context.getString(R.string.x313);
            case 314:
                return context.getString(R.string.x314);
            case 321:
                return context.getString(R.string.x321);
            case 500:
                return context.getString(R.string.x500);
            case 501:
                return context.getString(R.string.x501);
            case 502:
                return context.getString(R.string.x502);
            case 503:
                return context.getString(R.string.x503);
            case 504:
                return context.getString(R.string.x504);
            case 511:
                return context.getString(R.string.x511);
            case 520:
                return context.getString(R.string.x520);
            case 521:
                return context.getString(R.string.x521);
            case 522:
                return context.getString(R.string.x522);
            case 531:
                return context.getString(R.string.x531);
            case 600:
                return context.getString(R.string.x600);
            case 601:
                return context.getString(R.string.x601);
            case 602:
                return context.getString(R.string.x602);
            case 611:
                return context.getString(R.string.x611);
            case 612:
                return context.getString(R.string.x612);
            case 613:
                return context.getString(R.string.x613);
            case 615:
                return context.getString(R.string.x615);
            case 616:
                return context.getString(R.string.x616);
            case 620:
                return context.getString(R.string.x620);
            case 621:
                return context.getString(R.string.x621);
            case 622:
                return context.getString(R.string.x622);
            case 701:
                return context.getString(R.string.atmosphere_701);
            case 711:
                return context.getString(R.string.atmosphere_711);
            case 721:
                return context.getString(R.string.atmosphere_721);
            case 731:
                return context.getString(R.string.atmosphere_731);
            case 741:
                return context.getString(R.string.atmosphere_741);
            case 751:
                return context.getString(R.string.atmosphere_751);
            case 761:
                return context.getString(R.string.atmosphere_761);
            case 762:
                return context.getString(R.string.atmosphere_762);
            case 771:
                return context.getString(R.string.atmosphere_771);
            case 781:
                return context.getString(R.string.atmosphere_781);
            case 800:
                return context.getString(R.string.x800);
            case 801:
                return context.getString(R.string.x801);
            case 802:
                return context.getString(R.string.x802);
            case 803:
                return context.getString(R.string.x803);
            case 804:
                return context.getString(R.string.x804);
        }
        return null;
    }

    public static int kelvinToCelsius(double kelvin) {
        return (int) (kelvin - 273.15);
    }

    public static int idToConditionMapper(int id) {
        if (id >= 200 && id <= 232) {
            return R.drawable.thunderstorm;
        } else if (id >= 300 && id <= 321) {
            return R.drawable.shower_rain;
        } else if (id >= 500 && id <= 504) {
            return R.drawable.rain;
        } else if (id == 511) {
            return R.drawable.snow;
        } else if (id >= 520 && id <= 531) {
            return R.drawable.shower_rain;
        } else if (id >= 600 && id <= 622) {
            return R.drawable.snow;
        } else if (id >= 701 & id <= 781) {
            return R.drawable.mist;
        } else if (id == 800) {
            return R.drawable.clear_sky;
        } else if (id == 801) {
            return R.drawable.few_clouds;
        } else if (id == 802) {
            return R.drawable.scattered_clouds;
        } else if (id == 803) {
            return R.drawable.broken_clouds;
        } else if (id == 804) {
            return R.drawable.broken_clouds;
        }
        return -1;
    }

    public static String epochToDate(long epoch) {
        String language = Locale.getDefault().getLanguage();
        switch (language) {
            case Constants.ENGLISH:

            case Constants.PERSIAN:
                PersianDate date = new PersianDate(epoch * 1000);
                PersianDateFormat formater = new PersianDateFormat("l, j F");
                return formater.format(date);
            default:
                return null;
        }
    }

    public static int mphToKmh(double mph) {
        return (int) (mph * 1.609);
    }
}
