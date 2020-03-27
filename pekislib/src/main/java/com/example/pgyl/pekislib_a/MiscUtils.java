package com.example.pgyl.pekislib_a;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import java.util.Locale;

public class MiscUtils {

    public static class BiDimensions {
        public int width;
        public int height;

        BiDimensions(int width, int height) {
            this.width = width;
            this.height = height;
        }
    }

    public static void msgBox(String string, Context context) {        //  Ex:  msgBox("ERROR: Invalid number", this)
        AlertDialog.Builder dlgAlert = new AlertDialog.Builder(context);
        dlgAlert.setMessage(string);
        dlgAlert.setTitle("Info");
        dlgAlert.setPositiveButton("OK", null);
        dlgAlert.setCancelable(true);
        dlgAlert.create().show();
        //dlgAlert.setPositiveButton("OK",new DialogInterface.OnClickListener()
        //    {public void onClick(DialogInterface dialog, int which) {}});
    }

    public static void toastLong(String string, Context context) {    //  Ex: toastLong("Info", this)
        Toast.makeText(context, string, Toast.LENGTH_LONG).show();
    }

    public static void toastShort(String string, Context context) {
        Toast.makeText(context, string, Toast.LENGTH_SHORT).show();
    }

    public static void beep(Context context) {   //  Ex: beep(this)
        context.startService(new Intent(context, BeepIntentService.class));
    }

    public static String capitalize(String string) {
        return string.substring(0, 1).toUpperCase(Locale.ENGLISH) + string.substring(1).toLowerCase(Locale.ENGLISH);
    }

}
