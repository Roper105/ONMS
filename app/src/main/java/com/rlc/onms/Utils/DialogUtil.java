package com.rlc.onms.Utils;

import com.rlc.onms.R;

import android.app.AlertDialog;
import android.content.Context;
public class DialogUtil {

    public static AlertDialog showLoadingDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(R.layout.loading_dialog); // loading_dialog adlı tasarım
        builder.setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.show();

        return dialog;
    }

}
