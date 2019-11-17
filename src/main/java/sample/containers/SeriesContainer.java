package sample.containers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import sample.records.Series;

import java.util.HashMap;
import java.util.Map;

public class SeriesContainer {
    private Map<Integer, Series> seriesById = new HashMap<>();
    private Map<Integer, ObservableList<Series> > seriesListBySeasonId = new HashMap<>();


    public void add(Series series) {
        seriesById.put(series.getId(), series);
        ObservableList<Series> seriesList = seriesListBySeasonId.computeIfAbsent(series.getIdSeason(),
                k -> FXCollections.observableArrayList());
        seriesList.add(series);
    }

    public Series getById(int id) {
        return seriesById.get(id);
    }

    public ObservableList<Series> getBySeasonId(int idSeason) {
        return seriesListBySeasonId.computeIfAbsent(idSeason,
                k -> FXCollections.observableArrayList());
    }

}
