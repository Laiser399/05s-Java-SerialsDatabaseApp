package sample.database.containers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import sample.database.records.Series;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class SeriesContainer {
    private Map<Integer, Series> seriesById = new HashMap<>();
    private Map<Integer, ObservableList<Series> > seriesListBySeasonId = new HashMap<>();

    public void addOrUpdate(int id, int idSeason, int number, String name, Date releaseDate, String torrent) {
        Series series = seriesById.get(id);
        if (series == null) {
            add(new Series(id, idSeason, number, name, releaseDate, torrent));
        }
        else {
            series.setNumber(number);
            series.setName(name);
            series.setReleaseDate(releaseDate);
            series.setTorrentLink(torrent);
        }
    }

    private void add(Series series) {
        seriesById.put(series.getId(), series);
        ObservableList<Series> seriesList = seriesListBySeasonId.computeIfAbsent(series.getIdSeason(),
                k -> FXCollections.observableArrayList());
        seriesList.add(series);
    }

    public void remove(int id) {
        Series series = seriesById.remove(id);
        if (series != null)
            seriesListBySeasonId.get(series.getIdSeason()).remove(series);
    }

    public Collection<Series> getSeries() {
        return seriesById.values();
    }

    public ObservableList<Series> getBySeasonId(int idSeason) {
        return seriesListBySeasonId.computeIfAbsent(idSeason,
                k -> FXCollections.observableArrayList());
    }

}
