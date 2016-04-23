package nullwarp.popmusicianslist;

import android.util.JsonReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Produces a list of Performer objects with data filled from a JSON inputStream.
 */
class PerformerJSONParser {
    public static List<Performer> readJsonStream(InputStream in) throws IOException {
        try (JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"))) {
            return readPerformerArray(reader);
        }
    }

    private static List<Performer> readPerformerArray(JsonReader reader) throws IOException {
        List<Performer> performers = new ArrayList<>();
        reader.beginArray();
        while (reader.hasNext()) {
            performers.add(readPerformer(reader));
        }
        reader.endArray();
        return performers;
    }

    private static Performer readPerformer(JsonReader reader) throws IOException {
        long id = -1;
        String name = null;
        String[] genres = null;
        long tracks = 0;
        long albums = 0;
        String link = null;
        String description = null;
        String small_cover = null;
        String big_cover = null;

        reader.beginObject();
        while (reader.hasNext()) {
            String propertyName = reader.nextName();
            switch (propertyName) {
                case "id":
                    id = reader.nextLong();
                    break;
                case "name":
                    name = reader.nextString();
                    break;
                case "genres":
                    reader.beginArray();
                    ArrayList<String> genresList = new ArrayList<>();
                    while (reader.hasNext()) {
                        genresList.add(reader.nextString());
                    }
                    genres = genresList.toArray(new String[0]);
                    reader.endArray();
                    break;
                case "tracks":
                    tracks = reader.nextLong();
                    break;
                case "albums":
                    albums = reader.nextLong();
                    break;
                case "link":
                    link = reader.nextString();
                    break;
                case "description":
                    description = reader.nextString();
                    break;
                case "cover":
                    reader.beginObject();
                    while (reader.hasNext()) {
                        String coverName = reader.nextName();
                        switch (coverName) {
                            case "small":
                                small_cover = reader.nextString();
                                break;
                            case "big":
                                big_cover = reader.nextString();
                                break;
                            default:
                                reader.skipValue();
                                break;
                        }
                    }
                    reader.endObject();
                    break;
                default:
                    reader.skipValue();
                    break;
            }
        }
        reader.endObject();

        return new Performer(id, name, genres, tracks, albums, link, description, small_cover, big_cover);
    }
}
