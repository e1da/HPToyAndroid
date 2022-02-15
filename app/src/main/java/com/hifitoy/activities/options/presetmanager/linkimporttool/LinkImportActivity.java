package com.hifitoy.activities.options.presetmanager.linkimporttool;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.hifitoy.ApplicationContext;
import com.hifitoy.activities.BaseActivity;
import com.hifitoy.R;
import com.hifitoy.dialogsystem.DialogSystem;
import com.hifitoy.hifitoydevice.ToyPreset;

import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class LinkImportActivity extends BaseActivity {
    final static String TAG = "HiFiToy";

    private EditText linkText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Direct link");
        setContentView(R.layout.activity_link_import);

        //show back button
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        initOutlets();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.preset_link_menu, menu);
        return true;
    }

    //back button handler
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()) {
            case R.id.importPresetFromLink_outl:
                downloadPreset();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initOutlets() {
        linkText = findViewById(R.id.presetImportLinkText_outl);
    }


    private String convertStreamToString(java.io.InputStream is) {
        try {
            return new java.util.Scanner(is).useDelimiter("\\A").next();
        } catch (java.util.NoSuchElementException e) {
            return "";
        }
    }

    private void downloadPreset() {
        //String u = "https://kerosinn.github.io/hptoy-repo/HD800S2Harman.tpr";
        String u = linkText.getText().toString();

        if (isNetworkAvailable()) {
            new DownloadFileFromURL().execute(u);
        } else {
            DialogSystem.getInstance().showDialog("Warning", "Internet is not available.", "Ok");
        }

    }

    private boolean isNetworkAvailable() {

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        return false;
    }


    private static class DownloadFileFromURL extends AsyncTask<String, Void, String> {

        private String convertStreamToString(java.io.InputStream is) {
            try {
                return new java.util.Scanner(is).useDelimiter("\\A").next();
            } catch (java.util.NoSuchElementException e) {
                return "";
            }
        }


        @Override
        protected String doInBackground(String... f_url) {
            String prs = null;

            try {
                URL url = new URL(f_url[0]);
                URLConnection conection = url.openConnection();
                conection.connect();

                Log.d(TAG, String.format("Length =%d", conection.getContentLength()));

                InputStream input = new BufferedInputStream(url.openStream(), 8192);
                prs = convertStreamToString(input);

                input.close();

            } catch (Exception e) {
                Log.d(TAG, e.toString());

                Handler mainHandler = new Handler(Looper.getMainLooper());
                Runnable myRunnable = new Runnable() {
                    @Override
                    public void run() {
                        DialogSystem.getInstance().showDialog("Warning", "Url is not correct.", "Ok");
                    }
                };
                mainHandler.post(myRunnable);
            }


            return prs;
        }

        @Override
        protected void onPostExecute(final String presetString) {
            if ( (presetString == null) || (presetString.equals("")) ) return;

            Activity c = (Activity) ApplicationContext.getInstance().getContext();

            DialogSystem.OnClickTextDialog dialogListener = new DialogSystem.OnClickTextDialog() {
                Activity c = (Activity)ApplicationContext.getInstance().getContext();

                public void onPositiveClick(String name){

                    if (name.length() > 0) {
                        try {
                            ToyPreset importPreset = new ToyPreset(name, presetString);
                            importPreset.save(true);

                            DialogSystem.getInstance().showDialog("Info",
                                        "Add " + importPreset.getName() + " preset", "Ok");

                        } catch (IOException | XmlPullParserException e) {
                            DialogSystem.getInstance().showDialog("Error", e.getMessage(), "Ok");

                        } finally {
                            c.setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                        }

                    } else {
                        Toast.makeText(c,
                                "Name field is empty.", Toast.LENGTH_SHORT).show();
                        c.setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                    }
                }
                public void onNegativeClick(String text){
                    c.setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                }
            };

            c.setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_LOCKED);
            DialogSystem.getInstance().showTextDialog(dialogListener, "Enter preset name", "Import", "Cancel");
        }

    }

}
