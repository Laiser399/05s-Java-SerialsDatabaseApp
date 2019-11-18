package sample.editDialogs.user;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import sample.records.User;

import java.io.IOException;
import java.util.Optional;

public class UserEditDialog extends Stage {
    private UserEditorController controller;
    private Optional<UserEditData> result = Optional.empty();

    public UserEditDialog() {
        try {
            initModality(Modality.APPLICATION_MODAL);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/editors/userEditor.fxml"));
            Parent root = loader.load();
            setScene(new Scene(root));
            setMinWidth(300);
            setMinHeight(180);
            setMaxHeight(180);

            controller = loader.getController();
            controller.init(this::onResult);
        }
        catch (IOException e) {
            System.out.println("Error loading user editor fxml file.");
            System.exit(-1);
        }
    }

    private void onResult(Optional<UserEditData> result) {
        this.result = result;
        close();
    }

    public Optional<UserEditData> editPassword(User user) {
        controller.prepareForEditPassword(user);
        showAndWait();
        return result;
    }

    public Optional<UserEditData> editRole(User user) {
        controller.prepareForEditRole(user);
        showAndWait();
        return result;
    }

    public Optional<UserEditData> create() {
        controller.prepareForCreate();
        showAndWait();
        return result;
    }

}
