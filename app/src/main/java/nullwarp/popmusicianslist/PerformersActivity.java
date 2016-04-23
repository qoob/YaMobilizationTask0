package nullwarp.popmusicianslist;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.List;

public class PerformersActivity extends AppCompatActivity implements DBFileHelperCallbackListener {

    private static final String p_jsonURL = "http://download.cdn.yandex.net/mobilization-2016/artists.json";
    private static Context context;
    private DBFileHelper p_dbFileHelper;
    private long lastDBupdate = 0;
    private SharedPreferences preferences;
    private List<Performer> performers = null;
    private ListView listViewPerformers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_performers);
        context = this;
        preferences = getPreferences(MODE_PRIVATE);
        lastDBupdate = preferences.getLong("lastupdate", 0);
        p_dbFileHelper = new DBFileHelper(p_jsonURL, lastDBupdate, this, this);

        Button m_button = (Button) findViewById(R.id.button);
        if (m_button != null) {
            m_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toast(false, "Checking for an update");
                    p_dbFileHelper.updateDB(lastDBupdate);
                }
            });
        }

        Button mButtonDelete = (Button) findViewById(R.id.buttonDelete);
        if (mButtonDelete != null) {
            mButtonDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteJSONFile();
                }
            });
        }

        listViewPerformers = (ListView) findViewById(R.id.listView);
        refreshList();

        listViewPerformers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //toast(false, performers.get(position).name);
                PerformerArrayAdapter.ViewHolder itemTag = (PerformerArrayAdapter.ViewHolder) (view.getTag());
                Intent intent = new Intent(context, DetailedInfo.class);
                Performer performer = performers.get(position);
                intent.putExtra("performer id", performer.id); // this is long, following extras are Strings
                intent.putExtra("performer name", performer.name);
                intent.putExtra("performer link", performer.link);
                intent.putExtra("performer description", performer.description);
                intent.putExtra("performer bigcover", performer.bigCover);
                intent.putExtra("performer genres", itemTag.textViewGenres.getText().toString()); // too lazy to recalculate strings
                intent.putExtra("performer albumstrackscount", itemTag.textViewSongsAlbumsCount.getText().toString());

                startActivity(intent);
            }
        });
    }

    private void deleteJSONFile() {
        if (deleteFile("data.json")) {
            lastDBupdate = -1;
            preferences.edit().putLong("lastupdate", lastDBupdate).apply();
            toast(false, "JSON file deleted");
            performers.clear();
            ((PerformerArrayAdapter) listViewPerformers.getAdapter()).notifyDataSetChanged();
        } else {
            toast(false, "Could not delete JSON file!");
        }
    }


    private boolean refreshList() {
        try {
            performers = PerformerJSONParser.readJsonStream(new BufferedInputStream(openFileInput("data.json")));
            PerformerArrayAdapter performerArrayAdapter = new PerformerArrayAdapter(this, performers);
            listViewPerformers.setAdapter(performerArrayAdapter);
            return true;
        } catch (FileNotFoundException e) {
            lastDBupdate = -1;
            preferences.edit().putLong("lastupdate", lastDBupdate).apply();
            toast(true, "JSON file not found!");
        } catch (IOException e) {
            toast(true, "JSON parsing failed!");
        }
        return false;
    }

    @Override
    public void onDBUpdateSuccess(long prevModified, int status) {
        lastDBupdate = prevModified;
        if (refreshList()) {
            preferences.edit().putLong("lastupdate", prevModified).apply();
            switch (status) {
                case 1:
                    toast(true, "Data updated to " + new Date(prevModified).toString());
                    break;
                case 2:
                    toast(true, "Update is not needed!\nRefreshed list.");
                    break;
                default:
                    break;
            }

        }
    }

    private void toast(boolean length_long, String toShow) {
        Toast.makeText(context, toShow, (length_long) ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDBUpdateFailure(long prevModified, int status) {
        toast(true, "Could not update data!");
        switch (status) {
            case -1:
                toast(false, "Bad URL");
                break;
            case -2:
                toast(false, "Network error?");
                break;
            case -3:
                toast(false, "IO Exception while fetching the DB!");
                break;
            case -4:
                toast(false, "Could not write to file");
                break;
            case -5:
                toast(false, "Network error or file writing error");
                break;
            default:
                break;
        }
    }

}
