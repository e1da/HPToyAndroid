package com.hifitoy.activities.options;

import android.app.ActionBar;
import android.os.Bundle;
import android.widget.TextView;

import com.hifitoy.BuildConfig;
import com.hifitoy.R;
import com.hifitoy.activities.BaseActivity;

public class AboutActivity extends BaseActivity {
    private TextView versionValue_outl;
    private TextView gitHashValue_outl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("About");
        setContentView(R.layout.activity_about);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        versionValue_outl = findViewById(R.id.versionValue_outl);
        gitHashValue_outl = findViewById(R.id.gitHashValue_outl);

        versionValue_outl.setText(BuildConfig.VERSION_NAME);
        gitHashValue_outl.setText(BuildConfig.GIT_HASH);
    }
}
