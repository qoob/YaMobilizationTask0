package nullwarp.popmusicianslist;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class DetailedInfo extends AppCompatActivity {

    private Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_info);
        Intent intent = getIntent();
        final String name, link, description, bigCover, genres, albumsTracksCount;
//        final long id;   // could be needed if an actual DB was used
//        id = intent.getLongExtra("performer id", 0);
        name = intent.getStringExtra("performer name");
        link = intent.getStringExtra("performer link");
        description = intent.getStringExtra("performer description");
        bigCover = intent.getStringExtra("performer bigcover");
        genres = intent.getStringExtra("performer genres");
        albumsTracksCount = intent.getStringExtra("performer albumstrackscount");

        TextView txtGenres = (TextView) findViewById(R.id.txtDetGenres);
        TextView txtBio = (TextView) findViewById(R.id.txtDetDescription);
        TextView txtLink = (TextView) findViewById(R.id.txtDetLink);
        TextView txtCount = (TextView) findViewById(R.id.txtDetCount);

        if (txtGenres != null) {
            txtGenres.setText(genres);
        }
        if (txtBio != null) {
            txtBio.setText(description);
        }
        if (txtLink != null) {
            txtLink.setText(link);
        }
        if (txtCount != null) {
            txtCount.setText(albumsTracksCount);
        }
        setTitle(name);
        ImageView imgCover = (ImageView) findViewById(R.id.imgDetCover);
        if (imgCover != null) {
            if (savedInstanceState == null) {
                new ImageLoader(imgCover).execute(bigCover);
            } else {
                bitmap = savedInstanceState.getParcelable("bigCover");
                if (bitmap != null) {
                    imgCover.setImageBitmap(bitmap);
                }
            }
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (bitmap != null) {
            outState.putParcelable("bigCover", bitmap);
        }
    }

    private class ImageLoader extends AsyncTask<String, String, Bitmap> {

        private final ImageView img;

        public ImageLoader(ImageView img) {
            this.img = img;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            try {
                return BitmapFactory.decodeStream((InputStream) new URL(params[0]).getContent());
            } catch (MalformedURLException e) {
                Log.e("Error", "Bad URL");
            } catch (IOException e) {
                Log.e("Error", "Network error while loading an image");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap image) {
            if (image != null) {
                img.setImageBitmap(image);
                img.setImageAlpha(255);
                DetailedInfo.this.bitmap = image;
            } else {
                img.setImageAlpha(0);
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            img.setImageAlpha(0);
        }
    }
}
