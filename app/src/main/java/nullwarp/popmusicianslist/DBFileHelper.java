package nullwarp.popmusicianslist;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

/**
 * Handles updating the JSON file.
 */
class DBFileHelper {
    private static UpdateDBTask updateDBTask = null;
    private final String jsonURL;
    private final DBFileHelperCallbackListener listener;
    private final Context context;
    private long prevModified;
    private int status = 0;

    public DBFileHelper(String jsonURL, long prevModified, Context context, DBFileHelperCallbackListener listener) {
        this.jsonURL = jsonURL;
        this.prevModified = prevModified;
        this.context = context;
        this.listener = listener;
    }

    public void updateDB(long prevModified) {
        this.prevModified = prevModified;
        if (updateDBTask == null) { // prevents multiple button clicks from starting several update tasks
            updateDBTask = new UpdateDBTask();
            updateDBTask.execute(jsonURL);
        }
    }

    private class UpdateDBTask extends AsyncTask<String, Void, Long> {

        @Override
        protected void onPostExecute(Long result) {
            DBFileHelper.updateDBTask = null;
            if (status > 0) {
                listener.onDBUpdateSuccess(prevModified, status);
            } else {
                listener.onDBUpdateFailure(prevModified, status);
            }
        }

        private void pullFile(InputStream in) throws IOException {
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(context.openFileOutput("data.json", Context.MODE_PRIVATE)));
                String str;
                while ((str = br.readLine()) != null) {
                    bw.write(str);
                }
                bw.flush();
                bw.close();
                br.close();
                status = 1;
            } catch (FileNotFoundException e) {
                status = -4;
                Log.e("Error", "File not found?!");
                throw e;
            } catch (IOException e) {
                status = -5;
                Log.e("Error", "Network error or file writing error");
                throw e;
            }

        }

        private boolean downloadDB(URL url) {
            HttpURLConnection urlConnection = null;
            try {
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                pullFile(in);
            } catch (IOException e) {
                status = -3;
                Log.e("Error", "IO Exception while fetching the DB!");
                return false;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            return true;
        }

        @Override
        protected Long doInBackground(String... params) {
            if (params.length != 1) {
                throw new IllegalArgumentException("UpdateDBTask parameter count is not 1");
            }
            HttpURLConnection httpConnection = null;
            try {
                URL url = new URL(params[0]);
                httpConnection = (HttpURLConnection) url.openConnection();
                httpConnection.setRequestMethod("HEAD");
                httpConnection.connect();
                long lastModified = httpConnection.getLastModified();
                if (!url.getHost().equals(httpConnection.getURL().getHost())) {
                    // we were redirected!
                    Log.i("Info", "Redirected while trying to update DB");
                }
                if (lastModified != 0) {
                    if (prevModified < lastModified) {
                        Log.i("Info", "Updating, received lastModified date is newer: " + new Date(lastModified));
                        if (downloadDB(url)) {
                            prevModified = lastModified;
                        }
                    } else {
                        status = 2;
                        Log.i("Info", "Update is not needed");
                    }
                } else {
                    Log.i("Info", "Last-Modified not returned, updating anyway");
                    if (downloadDB(url)) {
                        prevModified = lastModified;
                    }
                }
            } catch (MalformedURLException e) {
                status = -1;
                Log.e("Error", "Bad URL");

            } catch (IOException e) {
                status = -2;
                Log.e("Error", "Network error?");
            } finally {
                if (httpConnection != null) {
                    httpConnection.disconnect();
                }
            }
            return prevModified;
        }
    }

}
