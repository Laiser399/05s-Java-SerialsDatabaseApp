package sample.containers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import sample.records.Genre;
import sample.records.Serial;

import java.util.*;

public class SerialsContainer {
    private Map<Integer, Serial> serialById = new HashMap<>();
    private ObservableList<Serial> serialsObservable = FXCollections.observableArrayList();

    public void addOrUpdate(int id, String name, String offSite, double mark) {
        addOrUpdate(id, name, offSite, mark, new ArrayList<>());
    }

    public void addOrUpdate(int id, String name, String offSite, double mark, List<Genre> genres) {
        Serial serial = serialById.get(id);
        if (serial == null) {
            add(new Serial(id, name, offSite, mark, genres));
        }
        else {
            serial.setName(name);
            serial.setOfficialSite(offSite);
            serial.setMark(mark);
            serial.setGenres(genres);
        }
    }

    private void add(Serial serial) {
        serialById.put(serial.getId(), serial);
        serialsObservable.add(serial);
    }

    public void remove(int id) {
        Serial serial = serialById.remove(id);
        serialsObservable.remove(serial);
    }

    public Optional<Serial> getById(int id) {
        return Optional.ofNullable(serialById.get(id));
    }

    public ObservableList<Serial> getSerialsObservable() {
        return serialsObservable;
    }

}
