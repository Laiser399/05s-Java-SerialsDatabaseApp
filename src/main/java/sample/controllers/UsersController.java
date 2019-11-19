package sample.controllers;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import sample.database.Database;
import sample.Dialogs;
import sample.editDialogs.user.UserEditData;
import sample.editDialogs.user.UserEditDialog;
import sample.database.records.User;

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

        if (!database.updateUserPassword(user, data.password)) {
            Dialogs.showError("Ошибка изменения пароля.");
        }
    }

    @FXML private void onEditRolePressed() {
        User user = getSelected().orElse(null);
        if (user == null)
            return;

        UserEditData data = editDialog.editRole(user).orElse(null);
        if (data == null)
            return;

        if (!database.updateUserRole(user, data.role)) {
            Dialogs.showError("Ошибка изменения роли.");
        }
    }

    @FXML private void onCreatePressed() {
        UserEditData data = editDialog.create().orElse(null);
        if (data == null)
            return;

        if (!database.createUser(data.name, data.password, data.role)) {
            Dialogs.showError("Ошибка создания пользователя.");
        }
    }

    @FXML private void onDeletePressed() {
        User user = getSelected().orElse(null);
        if (user == null)
            return;

        String askString = "Удалить пользователя \"" + user.nameObservable().getValue() + "\"?";
        if (Dialogs.askFor(askString) == ButtonType.NO)
            return;

        if (!database.delete(user)) {
            Dialogs.showError("Ошибка удаления пользователя.");
        }
    }

}
