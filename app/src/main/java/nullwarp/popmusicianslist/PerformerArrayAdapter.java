package nullwarp.popmusicianslist;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.ArrayMap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * Converts data from a list of Performer to views to be displayed in a Listview. Also loads small covers to heap as needed.
 */
class PerformerArrayAdapter extends ArrayAdapter<Performer> {

    private final Activity context;
    private final List<Performer> performers;
    private final ArrayMap<Integer, Bitmap> images;


    public PerformerArrayAdapter(Activity context, List<Performer> performers) {
        super(context, R.layout.performer_list_entry, performers);
        this.context = context;
        this.performers = performers;
        this.images = new ArrayMap<>(performers.size());
    }


    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        View rowView = convertView;
        if (rowView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            rowView = inflater.inflate(R.layout.performer_list_entry, null, true);
            holder = new ViewHolder();
            holder.textViewName = (TextView) rowView.findViewById(R.id.txtName);
            holder.imageView = (ImageView) rowView.findViewById(R.id.imgSmallCover);
            holder.textViewGenres = (TextView) rowView.findViewById(R.id.txtGenres);
            holder.textViewLink = (TextView) rowView.findViewById(R.id.txtLink);
            holder.textViewSongsAlbumsCount = (TextView) rowView.findViewById(R.id.txtSongsAlbumsCount);
            rowView.setTag(holder);
        } else {
            holder = (ViewHolder) rowView.getTag();
        }
        Performer ourGuy = performers.get(position);
        if (ourGuy.name != null) {
            holder.textViewName.setText(ourGuy.name);
        } else {
            holder.textViewName.setText("");
        }
        if (ourGuy.link != null) {
            holder.textViewLink.setText(ourGuy.link);
        } else {
            holder.textViewLink.setText("");
        }
        if (ourGuy.genres != null) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < ourGuy.genres.length - 1; ++i) {
                sb.append(ourGuy.genres[i]);
                sb.append(", ");
            }
            if (ourGuy.genres.length > 0) {
                sb.append(ourGuy.genres[ourGuy.genres.length - 1]);
            }
            holder.textViewGenres.setText(sb.toString());
        } else {
            holder.textViewGenres.setText("");
        }
        StringBuilder sb = new StringBuilder();
        final long albums = ourGuy.albums;
        final long tracks = ourGuy.tracks;
        if (albums > 0) {
            sb.append(albums);
            sb.append(" альбом");
            final long rem = albums % 10;
            final long bigrem = albums % 100;
            int selftest = 0;
            if (albums > 1) {
                if (((bigrem >= 5) && (bigrem <= 20)) || (rem > 4) || (rem == 0) || (bigrem == 11)) {
                    //5 to 20,25,26,27,28,29,30,35,36,37,38,39,40, etc
                    sb.append("ов");
                    selftest++;
                }
                if ((rem > 1) && (rem < 5) && ((bigrem > 20 || bigrem < 5))) {
                    sb.append("а");  //2,3,4,22,23,24,32,33,34, etc
                    selftest++;
                }
                if (selftest > 1) {
                    Log.d("Error", Long.toString(albums) + sb.toString());
                }
            }
            if (tracks > 0) {
                sb.append(", ");
            }
        }
        if (tracks > 0) {
            sb.append(tracks);
            sb.append(" пес"); // BTW, look at your example: "24 песени" ...
            int selftest = 0;
            final long rem = tracks % 10;
            final long bigrem = tracks % 100;
            if ((rem == 1) && (bigrem != 11)) {
                sb.append("ня"); // :3  1 песня, 21 песня, 31 песня, ..., 101, 121, etc
                selftest++;
            }
            if ((rem > 1) && (rem < 5) && ((bigrem > 20) || (bigrem < 5))) {
                sb.append("ни");  //2,3,4,22,23,24,32,33,34, etc
                selftest++;
            }
            if ((rem == 0) || (rem >= 5) || ((bigrem > 5) && (bigrem < 20))) {
                sb.append("ен"); // 5 to 20,25,26,27,28,29,30,35,36,37,38,39,40, etc
                selftest++;
            }
            if (selftest != 1) {
                Log.d("Error", Long.toString(tracks) + sb.toString());
            }
        }
        holder.textViewSongsAlbumsCount.setText(sb.toString());
        if (images.containsKey(position)) { // load the image from heap memory
            holder.imageView.setImageBitmap(images.get(position));
        } else { // schedule image loading from URL
            new SmallCoverLoader(holder.imageView, position).execute(ourGuy.smallCover);
            // surely not the best way to do it, but it works ok
        }
        return rowView;
    }

    static class ViewHolder {
        public ImageView imageView;
        public TextView textViewName;
        public TextView textViewLink;
        public TextView textViewGenres;
        public TextView textViewSongsAlbumsCount;
    }

    private class SmallCoverLoader extends AsyncTask<String, String, Bitmap> {

        private final ImageView img;
        private final int id;

        public SmallCoverLoader(ImageView img, int id) {
            this.img = img;
            this.id = id;
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
                PerformerArrayAdapter.this.images.put(id, image);
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
