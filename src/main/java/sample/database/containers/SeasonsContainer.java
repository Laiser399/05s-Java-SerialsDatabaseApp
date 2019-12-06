package sample.database.containers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import sample.database.records.Season;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class SeasonsContainer {
    private Map<Integer, Season> seasonById = new HashMap<>();
    private Map<Integer, ObservableList<Season> > seasonsBySerialId = new HashMap<>();

    public void addOrUpdate(int id, int idSerial, int number, int seriesCount, String torrent) {
        Season season = seasonById.get(id);
        if (season == null) {
            add(new Season(id, idSerial, number, seriesCount, torrent));
        }
        else {
            season.setNumber(number);
            season.setSeriesCount(seriesCount);
            season.setTorrentLink(torrent);
        }
    }

    private void add(Season season) {
        seasonById.put(season.getId(), season);
        ObservableList<Season> seasonsForCurrentSerial = seasonsBySerialId.computeIfAbsent(
                season.getIdSerial(), k -> FXCollections.observableArrayList());
        seasonsForCurrentSerial.add(season);
    }

    public void remove(int id) {
        Season season = seasonById.remove(id);
        if (season != null) {
            seasonsBySerialId.get(season.getIdSerial()).remove(season);
        }
    }

    public Collection<Season> getSeasons() {
        return seasonById.values();
    }

    public ObservableList<Season> getBySerialId(int serialId) {
        return seasonsBySerialId.computeIfAbsent(serialId,
                k -> FXCollections.observableArrayList());
    }
}
