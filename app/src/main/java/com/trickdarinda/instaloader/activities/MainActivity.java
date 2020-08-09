package com.trickdarinda.instaloader.activities;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.trickdarinda.instaloader.BuildConfig;
import com.trickdarinda.instaloader.R;
import com.trickdarinda.instaloader.ResponseLoadingDialog;
import com.trickdarinda.instaloader.asynctasks.CreateCallTask;
import com.trickdarinda.instaloader.asynctasks.PostsDownloadTask;
import com.trickdarinda.instaloader.helperclass.CheckNetwork;
import com.trickdarinda.instaloader.helperclass.HandleResponse;
import com.trickdarinda.instaloader.model.PostsResponse;
import com.trickdarinda.instaloader.services.ClipboardJobService;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int PERMISSION_REQUEST_CODE = 231;
    private static final String[] permission = {Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE};
    private NotificationManager notifyMgr;
    private NotificationCompat.Builder notificationBuilder;
    private static final int DOWNLOAD_NOTIFICATION_ID = 6445;
    private static final String DOWNLOAD_CHANNEL_ID = "download_notification_channel";
    private static final String DOWNLOAD_CHANNEL_NAME = "Download Notification";
    private static final int JOB_ID = 234123;
    private boolean doubleBackToExitPressedOnce = false;
    private boolean isDownloading = false;
    private EditText urlEditText;
    private Button btDownload;
    private Button btPasteLink;
    private TextView usernameTextView;
    private TextView captionTextView;
    private TextView downloadProgressTextView;
    private RelativeLayout progressBarLayout;
    private ProgressBar downloadProgressBar;
    private CardView downloadContainer;
    private CardView adViewContainer;
    private CircleImageView profilePic;
    private ImageView mediaPreview;
    private ImageView copyCaptionIcon;
    private ImageView playIcon;
    private ImageView multipleMediaIndicatorIcon;
    private ClipboardManager clipboardManager;
    private AdView bannerAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initializing views
        init();

        //Ads Initialization
        adInit();

        //Cancel tap to download notification
        notifyMgr.cancel(2342);

        //Create a Job Scheduler service
        JobScheduler jobScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        //Create component name with ClipboardJobService
        ComponentName serviceName = new ComponentName(getPackageName(),
                ClipboardJobService.class.getName());
        //Create JobInfo.Builder object
        JobInfo.Builder jobBuilder = new JobInfo.Builder(JOB_ID, serviceName)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
        //Create JobInfo object from the JobInfo.Builder object
        JobInfo myJobInfo = jobBuilder.build();
        //Schedule the job
        jobScheduler.schedule(myJobInfo);

        //paste link in edit text box when the Paste Link button is clicked
        btPasteLink.setOnClickListener(v -> pasteUrl());

        //create call when the download button is clicked
        btDownload.setOnClickListener(v -> {
            if (checkSelfPermission(permission[0]) == PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(permission[1]) == PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(permission[2]) == PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(permission[3]) == PackageManager.PERMISSION_GRANTED) {
                //Check if the download task is running
                if (isDownloading) {
                    //TODO: if download task is running
                    Toast.makeText(this, "Download Running", Toast.LENGTH_SHORT).show();
                } else {
                    //initiate call
                    initCall(urlEditText.getText().toString());
                }
            } else {
                ActivityCompat.requestPermissions(this, permission, PERMISSION_REQUEST_CODE);
            }
        });

        //Copy caption from caption textView
        copyCaptionIcon.setOnClickListener(v -> {
            if (captionTextView.getText().length() > 0) {
                ClipData data = ClipData.newPlainText("Caption Text", captionTextView.getText());
                if (clipboardManager != null) {
                    clipboardManager.setPrimaryClip(data);
                }

                Toast.makeText(this, "Caption Copied!", Toast.LENGTH_SHORT).show();
            }
        });
        createNotificationChannel();
        //paste the url if the link is valid
        pasteAndDownload();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        //Cancel tap to download notification
        notifyMgr.cancel(2342);
        bannerAdView.setAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                adInit();
            }
        });
        pasteAndDownload();
    }

    @Override
    protected void onDestroy() {
        notifyMgr.cancelAll();
        super.onDestroy();
    }

    /* method to initialize views */
    private void init() {
        urlEditText = findViewById(R.id.urlEditText);
        btDownload = findViewById(R.id.bt_download);
        btPasteLink = findViewById(R.id.bt_paste_link);
        usernameTextView = findViewById(R.id.usernameTextView);
        captionTextView = findViewById(R.id.captionTextView);
        downloadProgressTextView = findViewById(R.id.progressTextView);
        downloadProgressBar = findViewById(R.id.progressBar);
        progressBarLayout = findViewById(R.id.progressBarLayout);
        downloadContainer = findViewById(R.id.downloadContainer);
        mediaPreview = findViewById(R.id.mediaImageView);
        profilePic = findViewById(R.id.userProfilePic);
        copyCaptionIcon = findViewById(R.id.copy_caption);
        playIcon = findViewById(R.id.playIcon);
        multipleMediaIndicatorIcon = findViewById(R.id.multiple_icon);
        bannerAdView = findViewById(R.id.bannerAdView);
        adViewContainer = findViewById(R.id.adviewContainer);
        //get the clipboard clipboardManager service
        clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        //NotificationManager
        notifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        //NotificationBuilder
        notificationBuilder = getNotificationBuilder();
    }

    /* Method to check storage permission */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0) {
            if (requestCode == PERMISSION_REQUEST_CODE) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED
                        && grantResults[2] == PackageManager.PERMISSION_GRANTED && grantResults[3] == PackageManager.PERMISSION_GRANTED) {
                    //Check if the download task is running
                    if (isDownloading) {
                        //TODO: if download task is running
                        Toast.makeText(this, "Download Running", Toast.LENGTH_SHORT).show();
                    } else {
                        //initiate call
                        initCall(urlEditText.getText().toString());
                    }
                } else {
                    Toast.makeText(this, "Please Grant Permissions!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    /*
    Exit the app on back pressed
     */
    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            Intent homeIntent = new Intent(Intent.ACTION_MAIN);
            homeIntent.addCategory(Intent.CATEGORY_HOME);
            homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(homeIntent);
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
    }

    private void adInit() {
        MobileAds.initialize(this, initializationStatus -> {

        });

        AdRequest adRequest = new AdRequest.Builder().build();
        bannerAdView.loadAd(adRequest);

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) adViewContainer.getLayoutParams();

        bannerAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                params.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
            }

            @Override
            public void onAdFailedToLoad(LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);

                params.height = 0;
            }
        });

        adViewContainer.setLayoutParams(params);
    }

    public void pasteAndDownload() {
        //paste and download the url from clipboard clipboardManager
        if (clipboardManager != null && clipboardManager.hasPrimaryClip()) {
            String url = "";
            try {
                url = (Objects.requireNonNull(clipboardManager.getPrimaryClip()).getItemAt(0).getText()).toString().trim();
                Log.e(TAG, "onCreate: URL = " + url);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (checkUrl(url)) {
                Log.e(TAG, "onCreate: copy clipboard");
                //Check for the permission
                if (checkSelfPermission(permission[0]) == PackageManager.PERMISSION_GRANTED
                        && checkSelfPermission(permission[1]) == PackageManager.PERMISSION_GRANTED
                        && checkSelfPermission(permission[2]) == PackageManager.PERMISSION_GRANTED
                        && checkSelfPermission(permission[3]) == PackageManager.PERMISSION_GRANTED) {
                    //Check if the download task is running
                    if (!isDownloading) {
                        //Paste url
                        pasteUrl();
                        //initiate call
                        initCall(urlEditText.getText().toString());
                    }
                } else {
                    ActivityCompat.requestPermissions(this, permission, PERMISSION_REQUEST_CODE);
                }
            }
        }
    }

    /* method to validate url */
    private boolean checkUrl(String url) {
        Log.e(TAG, "checkUrl: url = " + url);
        //REGULAR EXPRESSION for validation of url
        String REGEX = "https://www.instagram.com/(p|reel|tv)/(.*?)/(.*?)";
        //pattern to check the url with REGULAR EXPRESSION
        Pattern postsUrlPattern = Pattern.compile(REGEX);
        //check the url with the help of matcher with pattern
        Matcher matcher = postsUrlPattern.matcher(url);

        //return if the pattern matches or not
        return matcher.matches();
    }

    /* method to paste the url from clipboard */
    private void pasteUrl() {
        //paste the url when the btPasteUrl button is clicked
        if (clipboardManager != null) {
            try {
                //trim any extra white spaces
                String url = (Objects.requireNonNull(clipboardManager.getPrimaryClip()).getItemAt(0).getText()).toString().trim();
                //set the url in EditText Box
                urlEditText.setText(url);
                //remove the primary clip
                clipboardManager.setPrimaryClip(ClipData.newPlainText(null, ""));
            } catch (NullPointerException e) {
                e.getLocalizedMessage();
            }
        }
    }

    /* method to initiate call from CreateCall class */
    private void initCall(String link) {
        if (checkUrl(link)) {
            if (new CheckNetwork(MainActivity.this).registerNetworkCallback() > 0) {
                isDownloading = true;
                //Start Loading dialog
                ResponseLoadingDialog dialog = new ResponseLoadingDialog(MainActivity.this);
                dialog.startLoadingDialog();
                //REGULAR EXPRESSION for validation of url
                String REGEX = "https://www.instagram.com/(p|reel|tv)/(.*?)/(.*?)";
                //pattern to check the url with REGULAR EXPRESSION
                Pattern postsUrlPattern = Pattern.compile(REGEX);
                //check the url with the help of matcher with pattern
                Matcher matcher = postsUrlPattern.matcher(link);

                String url = "";
                if (matcher.matches()) {
                    url = "https://www.instagram.com/" + matcher.group(1) + "/" + matcher.group(2) + "/";
                } else {
                    Toast.makeText(MainActivity.this, "Invalid URL", Toast.LENGTH_SHORT).show();
                }
                //Create call using CreateCallTask class and then handle the received response
                CreateCallTask callTask = new CreateCallTask();
                callTask.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, url);
                callTask.setOnResponseListener(new CreateCallTask.onResponseListener() {
                    @Override
                    public void onCallStart() {
                        Log.e(TAG, "onCallStart: ");
                    }

                    @Override
                    public void onCallFinished() {
                        Log.e(TAG, "onCallFinished: ");
                    }

                    @Override
                    public void onResponse(PostsResponse response) {

                        //dismiss the alert dialog
                        dialog.dismissDialog();

                        if (response != null) {
                            //set the download cardView container layout visibility to visible
                            downloadContainer.setVisibility(View.VISIBLE);
                            responseHandler(response);
                        } else {
                            Toast.makeText(MainActivity.this, "Private Account", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Throwable t) {

                        //dismiss the alert dialog
                        dialog.dismissDialog();

                        //set downloading to false
                        isDownloading = false;

                        Log.e(TAG, "onFailure: " + t.getLocalizedMessage());
                        //Toast Internet Connection error message
                        Toast.makeText(MainActivity.this, "Check Your Internet Connection", Toast.LENGTH_SHORT).show();
                        //set the download cardView container layout visibility to invisible
                        downloadContainer.setVisibility(View.INVISIBLE);
                    }
                });
            } else {
                Toast.makeText(MainActivity.this, "Check Your Internet Connection", Toast.LENGTH_SHORT).show();
            }

        } else {
            Toast.makeText(MainActivity.this, "Invalid Url", Toast.LENGTH_SHORT).show();
        }

    }

    /* Method to handle response */
    private void responseHandler(PostsResponse response) {
        if (response != null) {
            //Get the HandleResponse instance
            HandleResponse hr = new HandleResponse(response);

            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) downloadContainer.getLayoutParams();
            params.height = RelativeLayout.LayoutParams.WRAP_CONTENT;

            String baseSubPath = "InstaLoader/";
            String imageSubPath = baseSubPath + "InstaLoader Images/";
            String videoSubPath = baseSubPath + "InstaLoader Videos/";
            //Check if the response contains multiple media
            if (response.getGraphql().getShortcodeMedia().getEdgeSidecarToChildren() == null) {
                //TODO: when the media contains single image or video
                //hide the multiple media indicator icon
                multipleMediaIndicatorIcon.setVisibility(View.INVISIBLE);
                if (!hr.getIsVideo()) {
                    //TODO: If the response is an image
                    //Download Image
                    PostsDownloadTask downloadTask = new PostsDownloadTask(this);
                    downloadTask.setDownloadAttr(imageSubPath, "InstaLoader" + hr.getSinglePostID() + ".jpg");
                    downloadTask.onDownloadResponse(new PostsDownloadTask.PostsDownloadResponse() {
                        @Override
                        public void onDownloadStarted() {
                            //Set the username
                            usernameTextView.setText(hr.getUsername());
                            //Set the caption of text
                            if (hr.getMediaCaption() != null) {
                                captionTextView.setVisibility(View.VISIBLE);
                                captionTextView.setText(hr.getMediaCaption());
                                copyCaptionIcon.setVisibility(View.VISIBLE);
                            } else {
                                captionTextView.setText("");
                                copyCaptionIcon.setVisibility(View.INVISIBLE);
                            }

                            //set the profile pic
                            Glide.with(MainActivity.this)
                                    .load(hr.getProfilePicUrl())
                                    .error(R.drawable.load_failed)
                                    .into(profilePic);
                            playIcon.setVisibility(View.INVISIBLE);
                            progressBarLayout.setVisibility(View.VISIBLE);
                            downloadProgressBar.setVisibility(View.VISIBLE);
                            downloadProgressTextView.setVisibility(View.VISIBLE);
                            //Set the image to mediaPreview
                            Glide.with(MainActivity.this)
                                    .load(hr.getDisplayUrl())
                                    .error(R.drawable.load_failed)
                                    .into(mediaPreview);
                        }

                        @Override
                        public void onDownloadSuccessful() {
                            //set the isDownloading variable to false
                            isDownloading = false;
                            //set view visibilities
                            playIcon.setVisibility(View.INVISIBLE);
                            progressBarLayout.setVisibility(View.INVISIBLE);
                            downloadProgressBar.setVisibility(View.INVISIBLE);
                            downloadProgressTextView.setVisibility(View.INVISIBLE);
                            //update notification
                            notificationBuilder.setProgress(0, 0, false);
                            notificationBuilder.setContentText("Download Complete");
                            notificationBuilder.setAutoCancel(true);
                            notifyMgr.notify(DOWNLOAD_NOTIFICATION_ID, notificationBuilder.build());
                            //Toast download Successful message
                            Toast.makeText(MainActivity.this, "Download Successful", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onDownloadRunning(int progress) {
                            String showProgress = progress + "%";
                            downloadProgressTextView.setText(showProgress);
                            downloadProgressBar.setProgress(progress);
                            //set download progress in notification
                            notificationBuilder.setProgress(100, progress, false);
                            notifyMgr.notify(DOWNLOAD_NOTIFICATION_ID, notificationBuilder.build());
                        }

                        @Override
                        public void onDownloadFailed() {
                            //set isDownloading boolean to false
                            isDownloading = false;
                            //set view visiblity
                            progressBarLayout.setVisibility(View.INVISIBLE);
                            //toast download failed message
                            Toast.makeText(MainActivity.this, R.string.download_failed_text, Toast.LENGTH_SHORT).show();
                            //update notification
                            notificationBuilder.setProgress(0, 0, false);
                            notificationBuilder.setContentText("Download Failed");
                            notificationBuilder.setAutoCancel(true);
                            notifyMgr.notify(DOWNLOAD_NOTIFICATION_ID, notificationBuilder.build());
                            //Toast a download failed message
                            Toast.makeText(MainActivity.this, "Download Failed", Toast.LENGTH_SHORT).show();
                        }
                    });
                    downloadTask.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, hr.getSingleImgUrl());
                } else {
                    //TODO: If the response is a video
                    //Download Video
                    PostsDownloadTask downloadTask = new PostsDownloadTask(this);
                    downloadTask.setDownloadAttr(videoSubPath, "InstaLoader" + hr.getSinglePostID() + ".mp4");
                    downloadTask.onDownloadResponse(new PostsDownloadTask.PostsDownloadResponse() {
                        @Override
                        public void onDownloadStarted() {
                            //Set the username
                            usernameTextView.setText(hr.getUsername());
                            //Set the caption of text
                            if (hr.getMediaCaption() != null) {
                                captionTextView.setVisibility(View.VISIBLE);
                                captionTextView.setText(hr.getMediaCaption());
                                copyCaptionIcon.setVisibility(View.VISIBLE);
                            } else {
                                captionTextView.setText("");
                                copyCaptionIcon.setVisibility(View.INVISIBLE);
                            }

                            //set the profile pic
                            Glide.with(MainActivity.this)
                                    .load(hr.getProfilePicUrl())
                                    .error(R.drawable.load_failed)
                                    .into(profilePic);
                            playIcon.setVisibility(View.INVISIBLE);
                            progressBarLayout.setVisibility(View.VISIBLE);
                            downloadProgressBar.setVisibility(View.VISIBLE);
                            downloadProgressTextView.setVisibility(View.VISIBLE);
                            //Set the video thumbnail to mediaPreview
                            Glide.with(MainActivity.this)
                                    .load(hr.getDisplayUrl())
                                    .error(R.drawable.load_failed)
                                    .into(mediaPreview);
                        }

                        @Override
                        public void onDownloadSuccessful() {
                            //set the isDownloading variable to false
                            isDownloading = false;

                            Log.e(TAG, "onDownloadSuccessful: ");
                            //set views visiblity
                            playIcon.setVisibility(View.VISIBLE);
                            progressBarLayout.setVisibility(View.VISIBLE);
                            downloadProgressBar.setVisibility(View.INVISIBLE);
                            downloadProgressTextView.setVisibility(View.INVISIBLE);
                            //update notification
                            notificationBuilder.setProgress(0, 0, false);
                            notificationBuilder.setContentText("Download Complete");
                            notificationBuilder.setAutoCancel(true);
                            notifyMgr.notify(DOWNLOAD_NOTIFICATION_ID, notificationBuilder.build());
                            //Toast download Successful message
                            Toast.makeText(MainActivity.this, "Download Successful", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onDownloadRunning(int progress) {
                            downloadProgressBar.setProgress(progress);
                            String showProgress = progress + "%";
                            downloadProgressTextView.setText(showProgress);
                            //set download progress in notification
                            notificationBuilder.setProgress(100, progress, false);
                            notifyMgr.notify(DOWNLOAD_NOTIFICATION_ID, notificationBuilder.build());
                        }

                        @Override
                        public void onDownloadFailed() {
                            //set views visiblity
                            progressBarLayout.setVisibility(View.INVISIBLE);
                            //toast download failed message
                            Toast.makeText(MainActivity.this, R.string.download_failed_text, Toast.LENGTH_SHORT).show();
                            //update notification
                            notificationBuilder.setProgress(0, 0, false);
                            notificationBuilder.setContentText("Download Failed");
                            notificationBuilder.setAutoCancel(true);
                            notifyMgr.notify(DOWNLOAD_NOTIFICATION_ID, notificationBuilder.build());
                            //Toast a download failed message
                            Toast.makeText(MainActivity.this, "Download Failed", Toast.LENGTH_SHORT).show();
                        }
                    });
                    downloadTask.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, hr.getSingleVidUrl());
                }
            } else {
                //TODO: when the media contains multiple image or video
                //change the multiple media indicator icon visibility to visible
                multipleMediaIndicatorIcon.setVisibility(View.VISIBLE);
                //set the display image to mediaPreview item
                Glide.with(this)
                        .load(hr.getMultipleMediaDisplayUrl(0))
                        .error(R.drawable.load_failed)
                        .into(mediaPreview);
                //set play icon invisible
                playIcon.setVisibility(View.INVISIBLE);

                int i = 0;
                //loop from first to last item in multiple media
                while (i < hr.getNumOfEdges()) {
                    //Check if the current item is an image or video
                    if (!hr.getMultipleMediaIsVid(i)) {
                        //if the media is an image
                        //Download the image
                        PostsDownloadTask downloadTask = new PostsDownloadTask(this);
                        downloadTask.setDownloadAttr(imageSubPath, "InstaLoader" + hr.getMultipleMediaPostID(i) + ".jpg");
                        downloadTask.onDownloadResponse(new PostsDownloadTask.PostsDownloadResponse() {
                            @Override
                            public void onDownloadStarted() {
                                //Set the username
                                usernameTextView.setText(hr.getUsername());
                                //Set the caption of text
                                if (hr.getMediaCaption() != null) {
                                    captionTextView.setVisibility(View.VISIBLE);
                                    captionTextView.setText(hr.getMediaCaption());
                                    copyCaptionIcon.setVisibility(View.VISIBLE);
                                } else {
                                    captionTextView.setText("");
                                    copyCaptionIcon.setVisibility(View.INVISIBLE);
                                }

                                //set the profile pic
                                Glide.with(MainActivity.this)
                                        .load(hr.getProfilePicUrl())
                                        .error(R.drawable.load_failed)
                                        .into(profilePic);
                                //set progress bar layout visible
                                progressBarLayout.setVisibility(View.VISIBLE);
                                //set progress bar visible
                                downloadProgressBar.setVisibility(View.VISIBLE);
                                //set progress text view visible
                                downloadProgressTextView.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onDownloadSuccessful() {
                                if (downloadProgressBar.getProgress() >= 91) {
                                    //set the isDownloading to false
                                    isDownloading = false;
                                    downloadProgressBar.setVisibility(View.INVISIBLE);
                                    downloadProgressTextView.setVisibility(View.INVISIBLE);
                                    if (hr.getMultipleMediaIsVid(0)) {
                                        progressBarLayout.setVisibility(View.VISIBLE);
                                        playIcon.setVisibility(View.VISIBLE);
                                    } else {
                                        progressBarLayout.setVisibility(View.INVISIBLE);
                                    }
                                    //update notification
                                    notificationBuilder.setProgress(0, 0, false);
                                    notificationBuilder.setContentText("Download Complete");
                                    notificationBuilder.setAutoCancel(true);
                                    notifyMgr.notify(DOWNLOAD_NOTIFICATION_ID, notificationBuilder.build());
                                    //Toast download Successful message
                                    Toast.makeText(MainActivity.this, "Download Successful", Toast.LENGTH_SHORT).show();

                                }
                            }

                            @Override
                            public void onDownloadRunning(int progress) {
                                int downloadProgress = downloadProgressBar.getProgress() + (progress / hr.getNumOfEdges());
                                downloadProgressBar.setProgress(downloadProgress);
                                String showProgress = downloadProgressBar.getProgress() + "%";
                                downloadProgressTextView.setText(showProgress);
                                //set download progress in notification
                                notificationBuilder.setProgress(100, downloadProgress, false);
                                notifyMgr.notify(DOWNLOAD_NOTIFICATION_ID, notificationBuilder.build());
                            }

                            @Override
                            public void onDownloadFailed() {
                                progressBarLayout.setVisibility(View.INVISIBLE);
                                //set the isDownloading to false
                                isDownloading = false;
                                if (downloadProgressBar.getProgress() >= 91) {
                                    //toast download failed message
                                    Toast.makeText(MainActivity.this, R.string.download_failed_text, Toast.LENGTH_SHORT).show();
                                    //update notification
                                    notificationBuilder.setProgress(0, 0, false);
                                    notificationBuilder.setContentText("Download Failed");
                                    notificationBuilder.setAutoCancel(true);
                                    notifyMgr.notify(DOWNLOAD_NOTIFICATION_ID, notificationBuilder.build());
                                    //Toast a download failed message
                                    Toast.makeText(MainActivity.this, "Download Failed", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        downloadTask.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, hr.getMultipleMediaImgUrl(i));
                    } else {
                        // if the first media is a video
                        //download the video
                        PostsDownloadTask downloadTask = new PostsDownloadTask(this);
                        downloadTask.setDownloadAttr(videoSubPath, "InstaLoader" + hr.getMultipleMediaPostID(i) + ".mp4");
                        downloadTask.onDownloadResponse(new PostsDownloadTask.PostsDownloadResponse() {
                            @Override
                            public void onDownloadStarted() {
                                //Set the username
                                usernameTextView.setText(hr.getUsername());
                                //Set the caption of text
                                if (hr.getMediaCaption() != null) {
                                    captionTextView.setVisibility(View.VISIBLE);
                                    captionTextView.setText(hr.getMediaCaption());
                                    copyCaptionIcon.setVisibility(View.VISIBLE);
                                } else {
                                    captionTextView.setText("");
                                    copyCaptionIcon.setVisibility(View.INVISIBLE);
                                }

                                //set the profile pic
                                Glide.with(MainActivity.this)
                                        .load(hr.getProfilePicUrl())
                                        .error(R.drawable.load_failed)
                                        .into(profilePic);
                                //set progress bar layout visible
                                progressBarLayout.setVisibility(View.VISIBLE);
                                //set progress bar visible
                                downloadProgressBar.setVisibility(View.VISIBLE);
                                //set progress text view visible
                                downloadProgressTextView.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onDownloadSuccessful() {
                                if (downloadProgressBar.getProgress() >= 91) {
                                    //set the isDownloading variable to false
                                    isDownloading = false;
                                    //set the views attributes
                                    downloadProgressBar.setVisibility(View.INVISIBLE);
                                    downloadProgressTextView.setVisibility(View.INVISIBLE);
                                    if (hr.getMultipleMediaIsVid(0)) {
                                        progressBarLayout.setVisibility(View.VISIBLE);
                                        playIcon.setVisibility(View.VISIBLE);
                                    } else {
                                        progressBarLayout.setVisibility(View.INVISIBLE);
                                    }
                                    //update notification
                                    notificationBuilder.setProgress(0, 0, false);
                                    notificationBuilder.setContentText("Download Complete");
                                    notificationBuilder.setAutoCancel(true);
                                    notifyMgr.notify(DOWNLOAD_NOTIFICATION_ID, notificationBuilder.build());
                                    //Toast download Successful message
                                    Toast.makeText(MainActivity.this, "Download Successful", Toast.LENGTH_SHORT).show();

                                }
                            }

                            @Override
                            public void onDownloadRunning(int progress) {
                                int downloadProgress = downloadProgressBar.getProgress() + (progress / hr.getNumOfEdges());
                                downloadProgressBar.setProgress(downloadProgress);
                                String showProgress = downloadProgressBar.getProgress() + "%";
                                downloadProgressTextView.setText(showProgress);
                                //set download progress in notification
                                notificationBuilder.setProgress(100, downloadProgress, false);
                                notifyMgr.notify(DOWNLOAD_NOTIFICATION_ID, notificationBuilder.build());
                            }

                            @Override
                            public void onDownloadFailed() {
                                progressBarLayout.setVisibility(View.INVISIBLE);
                                //set isDownloading to false
                                isDownloading = false;
                                if (downloadProgressBar.getProgress() >= 91) {
                                    //toast download failed message
                                    Toast.makeText(MainActivity.this, R.string.download_failed_text, Toast.LENGTH_SHORT).show();
                                    //update notification
                                    notificationBuilder.setProgress(0, 0, false);
                                    notificationBuilder.setContentText("Download Failed");
                                    notificationBuilder.setAutoCancel(true);
                                    notifyMgr.notify(DOWNLOAD_NOTIFICATION_ID, notificationBuilder.build());
                                    //Toast a download failed message
                                    Toast.makeText(MainActivity.this, "Download Failed", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        downloadTask.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, hr.getMultipleMediaVidUrl(i));
                    }
                    i++;
                }
            }
            Log.e(TAG, "responseHandler: Last line");
            downloadProgressTextView.setText("0%");
            downloadProgressBar.setProgress(0);
        }
    }

    /*Create Notification channel */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(DOWNLOAD_CHANNEL_ID, DOWNLOAD_CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
            channel.setName(DOWNLOAD_CHANNEL_NAME);

            notifyMgr.createNotificationChannel(channel);
        }
    }

    /* Method to create NotificationCompat.Builder object */
    private NotificationCompat.Builder getNotificationBuilder() {
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, DOWNLOAD_CHANNEL_ID);
        notificationBuilder.setContentTitle("InstaLoader");
        notificationBuilder.setContentText("Download In Progress...");
        notificationBuilder.setAutoCancel(false);
        notificationBuilder.setSmallIcon(R.mipmap.ic_launcher_round);
        //Intent to back to MainActivity when the notification is tapped
        Intent intent = new Intent(this, MainActivity.class);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                intent, 0);
        notificationBuilder.setContentIntent(pendingIntent);

        return notificationBuilder;
    }

    /* method to open instagram app */
    public void openInstagram(View view) {
        try {
            Intent i = getPackageManager().getLaunchIntentForPackage("com.instagram.android");
            startActivity(i);
        } catch (Exception e) {
            Toast.makeText(this, "Instagram Not Installed", Toast.LENGTH_SHORT).show();
        }

    }

    /* Method to open HelpActivity */
    public void openHelpActivity(View view) {
        Intent i = new Intent(MainActivity.this, HelpActivity.class);
        startActivity(i);
    }

    /* Method to share InstaLoader app */
    public void shareApp(View view) {
        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "InstaLoader");
            String shareMessage = "\nDownload Instagram IGTV, Reels and Posts. It's simple, free and secure. Download this app from the link below: \n\n";
            shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID + "\n\n";
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
            startActivity(Intent.createChooser(shareIntent, "Select One"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}