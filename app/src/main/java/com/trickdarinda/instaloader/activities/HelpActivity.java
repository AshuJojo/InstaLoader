package com.trickdarinda.instaloader.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.trickdarinda.instaloader.BuildConfig;
import com.trickdarinda.instaloader.R;

import java.util.List;

public class HelpActivity extends AppCompatActivity {
    private TextView versionTextView;
    private String versionName = BuildConfig.VERSION_NAME;
    private String[] emailAddress = {"joblessjoker.dev@gmail.com"};
    private String subject = "InstaLoader Feedback";
    private String content = "Enter your feedback here!";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        versionTextView = findViewById(R.id.versionTextView);

        String versionText = "VERSION: v" + versionName;
        versionTextView.setText(versionText);
    }

    public void openPrivacyPolicy(View view) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("https://sites.google.com/view/instaloader-privacy-policy/home"));
        startActivity(browserIntent);
    }

    public void openFeedback(View view) {
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.putExtra(Intent.EXTRA_EMAIL, emailAddress);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.setType("text/plain");
        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, content);
        final PackageManager pm = this.getPackageManager();
        final List<ResolveInfo> matches = pm.queryIntentActivities(emailIntent, 0);
        ResolveInfo best = null;
        for (final ResolveInfo info : matches) {
            if (info.activityInfo.packageName.endsWith(".gm") || info.activityInfo.name.toLowerCase().contains("gmail")) {
                best = info;
            }
        }
        if (best != null) {
            emailIntent.setClassName(best.activityInfo.packageName, best.activityInfo.name);
        }
        this.startActivity(emailIntent);
    }
}