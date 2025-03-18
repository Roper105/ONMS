package com.rlc.onms.Utils;

import com.rlc.onms.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;

public class DialogUtil {

    public static AlertDialog showLoadingDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(R.layout.loading_dialog); // loading_dialog adlı tasarım
        builder.setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.show();

        return dialog;
    }

    public static AlertDialog showConfirmationDialog(Context context, String title, String message, DialogInterface.OnClickListener positiveListener, DialogInterface.OnClickListener negativeListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("Evet", positiveListener);
        builder.setNegativeButton("Hayır", negativeListener);
        builder.setCancelable(false);
        return builder.show();
    }

    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
