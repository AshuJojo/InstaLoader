package com.trickdarinda.instaloader.asynctasks;

import android.app.DownloadManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.trickdarinda.instaloader.R;

public class PostsDownloadTask extends AsyncTask<String, Integer, Boolean> {
    private static final String TAG = "PostsDownloadTask";
    private DownloadManager dm;
    private String subPath;
    private String fileName;
    private PostsDownloadResponse downloadResponse;

    public PostsDownloadTask(Context context) {
        dm = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
    }

    public void setDownloadAttr(String subPath, String fileName) {
        this.subPath = subPath;
        this.fileName = fileName;
    }

    public void onDownloadResponse(PostsDownloadResponse downloadResponse) {
        this.downloadResponse = downloadResponse;
    }

    @Override
    protected Boolean doInBackground(String... strings) {
        if (strings.length > 0) {
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(strings[0]));
            request.setTitle(fileName);
            request.setDescription("Downloading In Progress");
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, subPath + fileName);

            long downloadID = -1;

            if (dm != null) {
                downloadID = dm.enqueue(request);
            }

            if (downloadID != -1) {
                while (true) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Cursor c = dm.query(new DownloadManager.Query().setFilterById(downloadID));

                    if (c.moveToFirst()) {
                        long downloadedSize = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                        long totalSize = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));

                        int progress = 0;

                        if (totalSize > 0) {
                            progress = (int) ((downloadedSize * 100) / (totalSize));
                        }

                        if (c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                            publishProgress(100);
                            return true;
                        } else if (c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_FAILED) {
                            Log.e(TAG, "Reason: " + c.getInt(c.getColumnIndex(DownloadManager.COLUMN_REASON)));
                            return false;
                        } else if (c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_RUNNING) {
                            if (totalSize > 0) {
                                publishProgress(progress);
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        //Send progress update
        downloadResponse.onDownloadRunning(values[0]);
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);

        if (aBoolean) {
            downloadResponse.onDownloadSuccessful();
        } else {
            downloadResponse.onDownloadFailed();
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        downloadResponse.onDownloadStarted();
    }

    public interface PostsDownloadResponse {
        void onDownloadStarted();

        void onDownloadSuccessful();

        void onDownloadRunning(int progress);

        void onDownloadFailed();
    }

}
