package com.trickdarinda.instaloader.activities;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
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

import com.bumptech.glide.Glide;
import com.trickdarinda.instaloader.R;
import com.trickdarinda.instaloader.ResponseLoadingDialog;
import com.trickdarinda.instaloader.asynctasks.CreateCallTask;
import com.trickdarinda.instaloader.asynctasks.PostsDownloadTask;
import com.trickdarinda.instaloader.helperclass.CheckNetwork;
import com.trickdarinda.instaloader.helperclass.HandleResponse;
import com.trickdarinda.instaloader.model.PostsResponse;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {
    public static final int PERMISSION_REQUEST_CODE = 231;
    public static final String[] permission = {Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE};
    private boolean doubleBackToExitPressedOnce = false;
    private EditText urlEditText;
    private Button btDownload;
    private Button btPasteLink;
    private TextView usernameTextView;
    private TextView captionTextView;
    private TextView downloadProgressTextView;
    private RelativeLayout progressBarLayout;
    private ProgressBar downloadProgressBar;
    private CardView downloadContainer;
    private CircleImageView profilePic;
    private ImageView mediaPreview;
    private ImageView copyCaptionIcon;
    private ImageView playIcon;
    private ImageView multipleMediaIndicatorIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initializing views
        init();

        //paste link in edit text box when the Paste Link button is clicked
        btPasteLink.setOnClickListener(v -> pasteUrl());

        //create call when the download button is clicked
        btDownload.setOnClickListener(v -> {
            if (checkSelfPermission(permission[0]) == PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(permission[1]) == PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(permission[2]) == PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(permission[3]) == PackageManager.PERMISSION_GRANTED) {
                initCall();
            } else {
                ActivityCompat.requestPermissions(this, permission, PERMISSION_REQUEST_CODE);
            }
        });

        //Copy caption from caption textView
        copyCaptionIcon.setOnClickListener(v -> {
            if (captionTextView.getText().length() > 0) {
                ClipboardManager manager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData data = ClipData.newPlainText("Caption Text", captionTextView.getText());
                if (manager != null)
                    manager.setPrimaryClip(data);

                Toast.makeText(this, "Caption Copied!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /* Method to check storage persmision */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0) {
            if (requestCode == PERMISSION_REQUEST_CODE) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED
                        && grantResults[2] == PackageManager.PERMISSION_GRANTED && grantResults[3] == PackageManager.PERMISSION_GRANTED) {
                    initCall();
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
    }

    /* method to validate url */
    private boolean checkUrl(String url) {
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
        //get the clipboard manager
        final ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

        //paste the url when the btPasteUrl button is clicked
        if (clipboard != null) {
            try {
                //trim any extra white spaces
                String url = (Objects.requireNonNull(clipboard.getPrimaryClip()).getItemAt(0).getText()).toString().trim();
                //set the url in EditText Box
                urlEditText.setText(url);
            } catch (NullPointerException e) {
                e.getLocalizedMessage();
            }
        }
    }

    /* method to initiate call from CreateCall class */
    private void initCall() {
        if (urlEditText != null) {
            if (checkUrl(urlEditText.getText().toString())) {
                if (new CheckNetwork(this).registerNetworkCallback() > 0) {
                    //Start Loading dialog
                    ResponseLoadingDialog dialog = new ResponseLoadingDialog(MainActivity.this);
                    dialog.startLoadingDialog();
                    //REGULAR EXPRESSION for validation of url
                    String REGEX = "https://www.instagram.com/(p|reel|tv)/(.*?)/(.*?)";
                    //pattern to check the url with REGULAR EXPRESSION
                    Pattern postsUrlPattern = Pattern.compile(REGEX);
                    //check the url with the help of matcher with pattern
                    Matcher matcher = postsUrlPattern.matcher(urlEditText.getText().toString());

                    String url = "";
                    if (matcher.matches()) {
                        url = "https://www.instagram.com/" + matcher.group(1) + "/" + matcher.group(2) + "/";
                    } else {
                        Toast.makeText(this, "Invalid URL", Toast.LENGTH_SHORT).show();
                    }
                    //Create call using CreateCallTask class and then handle the received response
                    CreateCallTask callTask = new CreateCallTask();
                    callTask.execute(url);
                    callTask.setOnResponseListener(new CreateCallTask.onResponseListener() {
                        @Override
                        public void onResponse(PostsResponse response) {
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
                            dialog.dismissDialog();
                            Toast.makeText(MainActivity.this, "Check Your Internet Connection", Toast.LENGTH_SHORT).show();
                            //set the download cardView container layout visibility to invisible
                            downloadContainer.setVisibility(View.INVISIBLE);
                        }
                    });
                } else {
                    Toast.makeText(this, "Check Your Internet Connection", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Invalid Url", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /* Method to handle response */
    private void responseHandler(PostsResponse response) {
        if (response != null) {
            //Get the HandleResponse instance
            HandleResponse hr = new HandleResponse(response);
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
            Glide.with(this)
                    .load(hr.getProfilePicUrl())
                    .error(R.drawable.load_failed)
                    .into(profilePic);

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
                    PostsDownloadTask downloadTask = new PostsDownloadTask(this, imageSubPath, "InstaLoader" + hr.getSinglePostID() + ".jpg", new PostsDownloadTask.PostsDownloadResponse() {
                        @Override
                        public void onDownloadStarted() {
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
                            playIcon.setVisibility(View.INVISIBLE);
                            progressBarLayout.setVisibility(View.INVISIBLE);
                            downloadProgressBar.setVisibility(View.INVISIBLE);
                            downloadProgressTextView.setVisibility(View.INVISIBLE);
                        }

                        @Override
                        public void onDownloadRunning(int progress) {
                            String showProgress = progress + "%";
                            downloadProgressTextView.setText(showProgress);
                            downloadProgressBar.setProgress(progress);
                        }

                        @Override
                        public void onDownloadFailed() {
                            progressBarLayout.setVisibility(View.INVISIBLE);
                            Toast.makeText(MainActivity.this, R.string.download_failed_text, Toast.LENGTH_SHORT).show();
                        }
                    });
                    downloadTask.execute(hr.getSingleImgUrl());
                } else {
                    //TODO: If the response is a video
                    //Download Video
                    PostsDownloadTask downloadTask = new PostsDownloadTask(this, videoSubPath, "InstaLoader" + hr.getSinglePostID() + ".mp4", new PostsDownloadTask.PostsDownloadResponse() {
                        @Override
                        public void onDownloadStarted() {
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
                            playIcon.setVisibility(View.VISIBLE);
                            progressBarLayout.setVisibility(View.VISIBLE);
                            downloadProgressBar.setVisibility(View.INVISIBLE);
                            downloadProgressTextView.setVisibility(View.INVISIBLE);

                        }

                        @Override
                        public void onDownloadRunning(int progress) {
                            downloadProgressBar.setProgress(progress);
                            String showProgress = progress + "%";
                            downloadProgressTextView.setText(showProgress);
                        }

                        @Override
                        public void onDownloadFailed() {
                            progressBarLayout.setVisibility(View.INVISIBLE);
                            Toast.makeText(MainActivity.this, R.string.download_failed_text, Toast.LENGTH_SHORT).show();
                        }
                    });
                    downloadTask.execute(hr.getSingleVidUrl());
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
                        PostsDownloadTask downloadTask = new PostsDownloadTask(this, imageSubPath, "InstaLoader" + hr.getMultipleMediaPostID(i) + ".jpg", new PostsDownloadTask.PostsDownloadResponse() {
                            @Override
                            public void onDownloadStarted() {
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
                                    downloadProgressBar.setVisibility(View.INVISIBLE);
                                    downloadProgressTextView.setVisibility(View.INVISIBLE);
                                    if (hr.getMultipleMediaIsVid(0)) {
                                        progressBarLayout.setVisibility(View.VISIBLE);
                                        playIcon.setVisibility(View.VISIBLE);
                                    } else {
                                        progressBarLayout.setVisibility(View.INVISIBLE);
                                    }
                                }
                            }

                            @Override
                            public void onDownloadRunning(int progress) {
                                downloadProgressBar.setProgress(downloadProgressBar.getProgress() + (progress / hr.getNumOfEdges()));
                                String showProgress = downloadProgressBar.getProgress() + "%";
                                downloadProgressTextView.setText(showProgress);
                            }

                            @Override
                            public void onDownloadFailed() {
                                progressBarLayout.setVisibility(View.INVISIBLE);
                                if (downloadProgressBar.getProgress() >= 91) {
                                    Toast.makeText(MainActivity.this, R.string.download_failed_text, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        downloadTask.execute(hr.getMultipleMediaImgUrl(i));
                    } else {
                        // if the first media is a video
                        //download the video
                        PostsDownloadTask downloadTask = new PostsDownloadTask(this, videoSubPath, "InstaLoader" + hr.getMultipleMediaPostID(i) + ".mp4", new PostsDownloadTask.PostsDownloadResponse() {
                            @Override
                            public void onDownloadStarted() {
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
                                    downloadProgressBar.setVisibility(View.INVISIBLE);
                                    downloadProgressTextView.setVisibility(View.INVISIBLE);
                                    if (hr.getMultipleMediaIsVid(0)) {
                                        progressBarLayout.setVisibility(View.VISIBLE);
                                        playIcon.setVisibility(View.VISIBLE);
                                    } else {
                                        progressBarLayout.setVisibility(View.INVISIBLE);
                                    }
                                }
                            }

                            @Override
                            public void onDownloadRunning(int progress) {
                                downloadProgressBar.setProgress(downloadProgressBar.getProgress() + (progress / hr.getNumOfEdges()));
                                String showProgress = downloadProgressBar.getProgress() + "%";
                                downloadProgressTextView.setText(showProgress);
                            }

                            @Override
                            public void onDownloadFailed() {
                                progressBarLayout.setVisibility(View.INVISIBLE);
                                if (downloadProgressBar.getProgress() >= 91) {
                                    Toast.makeText(MainActivity.this, R.string.download_failed_text, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        downloadTask.execute(hr.getMultipleMediaVidUrl(i));
                    }
                    i++;
                }
            }

            downloadProgressTextView.setText("0%");
            downloadProgressBar.setProgress(0);
        }
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
}