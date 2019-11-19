package sample.database.containers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import sample.database.Database;
import sample.database.records.User;

import java.util.*;

public class UsersContainer {
    private Map<String, User> usersByName = new HashMap<>();
    private ObservableList<User> users = FXCollections.observableArrayList();

    public void addOrUpdate(String name, Database.Role role) {
        User user = usersByName.get(name);
        if (user == null) {
            add(new User(name, role));
        }
         else {
             user.setRole(role);
        }
    }

    private void add(User user) {
        usersByName.put(user.nameObservable().getValue(), user);
        users.add(user);
    }

    public void retainWithNames(Set<String> names) {
        for (String keyName : new ArrayList<>(usersByName.keySet())) {
            if (!names.contains(keyName)) {
                User user = usersByName.remove(keyName);
                users.remove(user);
            }
        }
    }

    public void remove(User user) {
        usersByName.remove(user.nameObservable().getValue());
        users.remove(user);
    }

    public ObservableList<User> getUsersObservable() {
        return users;
    }
}
