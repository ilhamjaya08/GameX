package com.gamex.app;

import android.content.Context;
import android.content.DialogInterface;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public final class DialogUtils {

    private DialogUtils() {
        // Utility class.
    }

    public static AlertDialog showNoInternetDialog(
            Context context,
            DialogInterface.OnClickListener retryListener) {
        return new MaterialAlertDialogBuilder(context)
            .setTitle(R.string.alert_no_internet_title)
            .setMessage(R.string.alert_no_internet_message)
            .setPositiveButton(R.string.alert_retry, retryListener)
            .setNegativeButton(R.string.alert_cancel, null)
            .show();
    }

    public static AlertDialog showErrorDialog(
            Context context,
            String title,
            String message) {
        return new MaterialAlertDialogBuilder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(R.string.alert_ok, null)
            .show();
    }

    public static AlertDialog showSuccessDialog(
            Context context,
            String title,
            String message,
            DialogInterface.OnClickListener okListener) {
        return new MaterialAlertDialogBuilder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(R.string.alert_ok, okListener)
            .setCancelable(false)
            .show();
    }
}
