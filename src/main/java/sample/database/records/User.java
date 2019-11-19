package sample.database.records;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import sample.database.Database;

public class User {
    private StringProperty name = new SimpleStringProperty();
    private ObjectProperty<Database.Role> role = new SimpleObjectProperty<>();

    public User(String name, Database.Role role) {

        this.name.setValue(name);
        this.role.setValue(role);
    }

    public ObservableValue<String> nameObservable() {
        return name;
    }

    public ObservableValue<Database.Role> roleObservable() {
        return role;
    }

    public void setRole(Database.Role role) {
        this.role.set(role);
    }




}
