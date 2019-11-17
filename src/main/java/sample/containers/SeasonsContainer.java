package sample.containers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import sample.records.Season;

import java.util.HashMap;
import java.util.Map;

public class SeasonsContainer {
    private Map<Integer, Season> seasonById = new HashMap<>();
    private Map<Integer, ObservableList<Season> > seasonsBySerialId = new HashMap<>();


    public void add(Season season) {
        seasonById.put(season.getId(), season);
        ObservableList<Season> seasonsForCurrentSerial = seasonsBySerialId.computeIfAbsent(
                season.getIdSerial(), k -> FXCollections.observableArrayList());
        seasonsForCurrentSerial.add(season);
    }

    public Season getById(int id) {
        return seasonById.get(id);
    }

    public ObservableList<Season> getBySerialId(int serialId) {
        return seasonsBySerialId.computeIfAbsent(serialId,
                k -> FXCollections.observableArrayList());
    }
}
