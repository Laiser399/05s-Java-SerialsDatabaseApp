package sample.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import sample.Database;
import sample.records.User;

import java.net.URL;
import java.util.ResourceBundle;

public class UsersController implements Initializable {
    @FXML private TableView<User> tableView;
    @FXML private TableColumn<User, String> nameColumn;
    @FXML private TableColumn<User, Database.Role> roleColumn;
    private Database database = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        nameColumn.setCellValueFactory(param -> param.getValue().nameObservable());
        roleColumn.setCellValueFactory(param -> param.getValue().roleObservable());
    }

    public void setDatabase(Database database) {
        this.database = database;
        // TODO getUsers from db
//        tableView.setItems(database.get);
    }

    // events
    @FXML private void onEditPasswordPressed() {
        // TODO
    }

    @FXML private void onEditRolePressed() {
        // TODO
    }

    @FXML private void onCreatePressed() {
        // TODO
    }

    @FXML private void onDeletePressed() {
        // TODO
    }

}
