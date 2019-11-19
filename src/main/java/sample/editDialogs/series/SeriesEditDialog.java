package sample.editDialogs.series;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import sample.database.records.Series;

import java.io.IOException;
import java.util.Optional;

public class SeriesEditDialog extends Stage {
    private SeriesEditController controller;
    private Optional<SeriesEditData> result = Optional.empty();

    public SeriesEditDialog() {
        try {
            initModality(Modality.APPLICATION_MODAL);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/editors/seriesEditor.fxml"));
            Parent root = loader.load();
            setScene(new Scene(root));
            setMinWidth(300);
            setMinHeight(256);
            setMaxHeight(256);

            controller = loader.getController();
            controller.init(this::onResult);
        }
        catch (IOException e) {
            System.out.println("Error loading series editor fxml file.");
            System.exit(-1);
        }
    }

    private void onResult(Optional<SeriesEditData> result) {
        this.result = result;
        close();
    }

    public Optional<SeriesEditData> edit(Series series) {
        controller.prepareForEdit(series);
        showAndWait();
        return result;
    }

    public Optional<SeriesEditData> create() {
        controller.prepareForCreate();
        showAndWait();
        return result;
    }
}
