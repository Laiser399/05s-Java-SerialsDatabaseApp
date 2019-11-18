package sample.controllers;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import sample.Database;
import sample.Dialogs;
import sample.editDialogs.user.UserEditData;
import sample.editDialogs.user.UserEditDialog;
import sample.records.User;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class UsersController implements Initializable {
    @FXML private TableView<User> tableView;
    @FXML private TableColumn<User, String> nameColumn;
    @FXML private TableColumn<User, Database.Role> roleColumn;
    private Database database = null;
    private UserEditDialog editDialog = new UserEditDialog();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        nameColumn.setCellValueFactory(param -> param.getValue().nameObservable());
        roleColumn.setCellValueFactory(param -> param.getValue().roleObservable());
    }

    public void setDatabase(Database database) {
        this.database = database;
        tableView.setItems(database.getUsers());
    }

    // methods
    private Optional<User> getSelected() {
        ObservableList<User> selected = tableView.getSelectionModel().getSelectedItems();
        return selected.size() == 1 ? Optional.of(selected.get(0)) : Optional.empty();
    }

    // events
    @FXML private void onEditPasswordPressed() {
        User user = getSelected().orElse(null);
        if (user == null)
            return;

        UserEditData data = editDialog.editPassword(user).orElse(null);
        if (data == null)
            return;

        System.out.println(data.name);
        System.out.println(data.password);
        // TODO use database to update password
    }

    @FXML private void onEditRolePressed() {
        User user = getSelected().orElse(null);
        if (user == null)
            return;

        UserEditData data = editDialog.editRole(user).orElse(null);
        if (data == null)
            return;

        System.out.println(data.name);
        System.out.println(data.role);
        // TODO use database to update role
    }

    @FXML private void onCreatePressed() {
        UserEditData data = editDialog.create().orElse(null);
        if (data == null)
            return;

        System.out.println(data.name);
        System.out.println(data.password);
        System.out.println(data.role);
        // TODO use database to create user
    }

    @FXML private void onDeletePressed() {
        User user = getSelected().orElse(null);
        if (user == null)
            return;

        String askString = "Удалить пользователя \"" + user.nameObservable().getValue() + "\"?";
        if (Dialogs.askFor(askString) == ButtonType.NO)
            return;

        System.out.println("fake delete user \"" + user.nameObservable().getValue() + "\"");
        // TODO use database to delete user
    }

}
