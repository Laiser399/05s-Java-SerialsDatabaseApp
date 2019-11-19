package sample.editDialogs.user;

import sample.database.Database;

public class UserEditData {
    public String name, password;
    public Database.Role role;

    public UserEditData(String name, String password, Database.Role role) {
        this.name = name;
        this.password = password;
        this.role = role;
    }
}
