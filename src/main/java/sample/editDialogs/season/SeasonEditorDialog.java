package sample.editDialogs.season;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import sample.records.Season;

import java.io.IOException;
import java.util.Optional;

public class SeasonEditorDialog extends Stage {
    private SeasonEditorController controller;
    private Optional<SeasonEditData> result = Optional.empty();

    public SeasonEditorDialog() {
        try {
            initModality(Modality.APPLICATION_MODAL);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/editors/seasonEditor.fxml"));
            Parent root = loader.load();
            setScene(new Scene(root, 380, 226));
            setMinWidth(282);
            setMinHeight(226);
            setMaxHeight(226);

            controller = loader.getController();
            controller.init(this::onResult);
        }
        catch (IOException e) {
            System.out.println("Error loading season editor fxml file.");
            System.exit(-1);
        }
    }

    private void onResult(Optional<SeasonEditData> result) {
        this.result = result;
        close();
    }

    public Optional<SeasonEditData> edit(Season season) {
        controller.prepareForEdit(season);
        showAndWait();
        return result;
    }

    public Optional<SeasonEditData> create() {
        controller.prepareForCreate();
        showAndWait();
        return result;
    }

}
