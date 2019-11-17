package sample.editDialogs.user;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import sample.Database;
import sample.records.User;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class UserEditorController implements Initializable {
    @FXML private Label newPasswordLabel, roleLabel;
    @FXML private TextField nameField, passwordField;
    @FXML private ChoiceBox<Database.Role> roleChoiceBox;
    private Consumer<Optional<UserEditData> > onResult = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        roleChoiceBox.getItems().addAll(Database.Role.Guest, Database.Role.Editor);
    }

    public void init(Consumer<Optional<UserEditData> > onResult) {
        this.onResult = onResult;
    }

    public void prepareForEditPassword(User user) {
        nameField.setEditable(false);
        setPasswordItemsManaged(true);
        setRoleItemsManaged(false);

        nameField.setText(user.nameObservable().getValue());
        passwordField.setText("");
    }

    public void prepareForEditRole(User user) {
        nameField.setEditable(false);
        setPasswordItemsManaged(false);
        setRoleItemsManaged(true);

        nameField.setText(user.nameObservable().getValue());
        passwordField.setText("");
        roleChoiceBox.getSelectionModel().select(Database.Role.Guest);
    }

    public void prepareForCreate() {
        nameField.setEditable(true);
        setPasswordItemsManaged(true);
        setRoleItemsManaged(true);

        nameField.setText("");
        passwordField.setText("");
    }

    private void setPasswordItemsManaged(boolean managed) {
        newPasswordLabel.setVisible(managed);
        newPasswordLabel.setManaged(managed);
        passwordField.setVisible(managed);
        passwordField.setManaged(managed);
    }

    private void setRoleItemsManaged(boolean managed) {
        roleLabel.setVisible(managed);
        roleLabel.setManaged(managed);
        roleChoiceBox.setVisible(managed);
        roleChoiceBox.setManaged(managed);
    }

    @FXML private void onOk() {
        if (onResult == null)
            return;
        onResult.accept(Optional.of(new UserEditData(nameField.getText(),
                passwordField.getText(), roleChoiceBox.getSelectionModel().getSelectedItem())));
    }

    @FXML private void onCancel() {
        if (onResult == null)
            return;
        onResult.accept(Optional.empty());
    }

}
