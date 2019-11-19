package sample.editDialogs.serial;

import sample.database.records.Genre;

import java.util.List;

public class SerialEditData {
    public String name, officialSite;
    public double mark;
    public List<Genre> genres;

    public SerialEditData(String name, String officialSite, double mark, List<Genre> genres) {
        this.name = name;
        this.officialSite = officialSite;
        this.mark = mark;
        this.genres = genres;
    }
}
