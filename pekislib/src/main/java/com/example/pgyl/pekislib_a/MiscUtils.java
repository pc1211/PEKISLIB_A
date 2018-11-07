package com.example.pgyl.pekislib_a;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;

import java.util.Locale;

public class MiscUtils {

    public static void msgBox(String msg, Context context) {        //  Ex:  msgBox("ERROR: Invalid number",this)
        AlertDialog.Builder dlgAlert = new AlertDialog.Builder(context);
        dlgAlert.setMessage(msg);
        dlgAlert.setTitle("Info");
        dlgAlert.setPositiveButton("OK", null);
        dlgAlert.setCancelable(true);
        dlgAlert.create().show();
        //dlgAlert.setPositiveButton("OK",new DialogInterface.OnClickListener()
        //    {public void onClick(DialogInterface dialog, int which) {}});
    }

    public static void beep(Context context) {   //  Ex: beep(this)
        context.startService(new Intent(context, BeeperIntentService.class));
    }

    public static String capitalize(String string) {
        return string.substring(0, 1).toUpperCase(Locale.ENGLISH) + string.substring(1).toLowerCase(Locale.ENGLISH);
    }

}
