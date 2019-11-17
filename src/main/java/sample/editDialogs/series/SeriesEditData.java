package sample.editDialogs.series;

import java.util.Date;

public class SeriesEditData {
    public int number;
    public String name;
    public Date releaseDate;
    public String torrent;

    public SeriesEditData(int number, String name, Date releaseDate, String torrent) {
        this.number = number;
        this.name = name;
        this.releaseDate = releaseDate;
        this.torrent = torrent;
    }
}
