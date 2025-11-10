package com.gamex.app;

import java.text.NumberFormat;
import java.util.Locale;

public final class CurrencyUtils {

    private CurrencyUtils() {
        // Utility class.
    }

    public static String formatToRupiah(int amount) {
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        return formatRupiah.format(amount);
    }

    public static String formatToRupiah(double amount) {
        return formatToRupiah((int) amount);
    }
}
