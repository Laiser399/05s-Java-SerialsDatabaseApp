package sample.records;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import sample.Database;

public class User {
    private StringProperty name = new SimpleStringProperty(),
                           displayRole = new SimpleStringProperty();
    private ObjectProperty<Database.Role> role = new SimpleObjectProperty<>();

    public User(String name, Database.Role role) {
        this.role.addListener((observable, oldValue, newValue) ->
                displayRole.setValue(role.toString()));

        this.name.setValue(name);
        this.role.setValue(role);
    }

    public ObservableValue<String> nameObservable() {
        return name;
    }

    // TODO delete
    public ObservableValue<String> displayRoleObservable() {
        return displayRole;
    }

    public ObservableValue<Database.Role> roleObservable() {
        return role;
    }

}
