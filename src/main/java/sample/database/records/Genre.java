package sample.database.records;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;

public class Genre {
    private int id;
    private StringProperty name = new SimpleStringProperty();

    public Genre(int id, String name) {
        this.id = id;
        this.name.setValue(name);
    }

    public int getId() {
        return id;
    }

    public ObservableValue<String> nameObservable() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    @Override
    public String toString() {
        return name.getValue();
    }
}
