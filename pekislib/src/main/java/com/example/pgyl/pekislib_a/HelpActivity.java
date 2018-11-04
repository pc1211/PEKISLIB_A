package com.example.pgyl.pekislib_a;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.webkit.WebView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static com.example.pgyl.pekislib_a.Constants.ERROR_VALUE;
import static com.example.pgyl.pekislib_a.MiscUtils.msgBox;

public class HelpActivity extends Activity {
    //region Constantes
    public enum HELP_ACTIVITY_EXTRA_KEYS {
        HTML_ID
    }

    public static final String HELP_ACTIVITY_TITLE = "Help";
    //endregion
    //region Variables
    private WebView webView;
    //endregion

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        getActionBar().setTitle(HELP_ACTIVITY_TITLE);
        setContentView(R.layout.help);
        webView = (WebView) findViewById(R.id.webview);
    }

    @Override
    protected void onResume() {
        final String IMAGE_ASSET_FOLDER = "file:///android_asset/";
        final String MIME_TYPE = "text/html";
        final String ENCODING = "utf-8";

        super.onResume();

        int resourceId = getIntent().getIntExtra(HELP_ACTIVITY_EXTRA_KEYS.HTML_ID.toString(), 0);
        String html = loadHtmlFromFile(resourceId);
        webView.loadDataWithBaseURL(IMAGE_ASSET_FOLDER, html, MIME_TYPE, ENCODING, null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_help_only, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.HELP) {
            Toast.makeText(this, "No help on Help !", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private String loadHtmlFromFile(int resourceId) {
        final int BUF_SIZE = 4096;  // Taille d'un paquet pour le buffer

        String ret = "";
        try {
            InputStream inputStream = getResources().openRawResource(resourceId);
            byte[] bytesBuffer = new byte[BUF_SIZE];
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            int n = inputStream.read(bytesBuffer);
            while (n != ERROR_VALUE) {
                outputStream.write(bytesBuffer, 0, n);
                n = inputStream.read(bytesBuffer);
            }
            inputStream.close();
            inputStream = null;
            ret = outputStream.toString();
            outputStream.close();
            outputStream = null;

        } catch (IOException e) {
            msgBox("Can't read Help file", this);
        }
        return ret;
    }

}        

