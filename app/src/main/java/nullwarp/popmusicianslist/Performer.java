package nullwarp.popmusicianslist;

/**
 * Data Model of JSON file
 */
@SuppressWarnings("WeakerAccess")
class Performer {
    public final long id;
    public final String name;
    public final String[] genres;
    public final long tracks;
    public final long albums;
    public final String link;
    public final String description;
    public final String smallCover;
    public final String bigCover;

    public Performer(long id, String name, String[] genres, long tracks, long albums, String link, String description, String smallCover, String bigCover) {
        this.id = id;
        this.name = name;
        this.genres = genres;
        this.tracks = tracks;
        this.albums = albums;
        this.link = link;
        this.description = description;
        this.smallCover = smallCover;
        this.bigCover = bigCover;
    }
}
