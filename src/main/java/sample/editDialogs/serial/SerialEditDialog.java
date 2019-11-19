package sample.editDialogs.serial;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import sample.database.Database;
import sample.database.records.Serial;

import java.io.IOException;
import java.util.Optional;

public class SerialEditDialog extends Stage {
    private SerialEditController controller;
    private Optional<SerialEditData> result = Optional.empty();

    public SerialEditDialog() {
        try {
            initModality(Modality.APPLICATION_MODAL);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/editors/serialEditor.fxml"));
            Parent root = loader.load();
            setScene(new Scene(root, 340, 400));
            setMinWidth(200);
            setMinHeight(300);
            controller = loader.getController();
            controller.init(this::onResult);
        }
        catch (IOException e) {
            System.out.println("Error loading serial editor fxml file.");
            System.exit(-1);
        }
    }

    public void setDatabase(Database database) {
        controller.setDatabase(database);
    }

    private void onResult(Optional<SerialEditData> result) {
        this.result = result;
        close();
    }

    public Optional<SerialEditData> edit(Serial serial) {
        controller.prepareForEdit(serial);
        showAndWait();
        return result;
    }

    public Optional<SerialEditData> create() {
        controller.prepareForCreate();
        showAndWait();
        return result;
    }



}
