package sample.containers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import sample.records.Serial;

import java.util.HashMap;
import java.util.Map;

public class SerialsContainer {
    private Map<Integer, Serial> serialById = new HashMap<>();
    private ObservableList<Serial> serialsObservable = FXCollections.observableArrayList();

    public void add(Serial serial) {
        serialById.put(serial.getId(), serial);
        serialsObservable.add(serial);
    }

    public void remove(Serial serial) {
        serialById.remove(serial.getId());
        serialsObservable.remove(serial);
    }

    public Serial getById(int id) {
        return serialById.get(id);
    }

    public ObservableList<Serial> getSerialsObservable() {
        return serialsObservable;
    }

}
