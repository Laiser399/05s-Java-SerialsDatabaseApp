package sample.RestoreDataDialog;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class RestoreDataDialog extends Stage {
    private RestoreDataDialogController controller;
    private boolean haveResult = false;

    public RestoreDataDialog() {
        try {
            initModality(Modality.APPLICATION_MODAL);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/RestoreDataDialog.fxml"));
            Parent root = loader.load();
            setScene(new Scene(root));
            setMinHeight(282);
            setMaxHeight(282);
            setMinWidth(400);

            controller = loader.getController();
            controller.init(this::onOk, this::onCancel);
        }
        catch (IOException e) {
            System.out.println("Error while loading Restore Data Dialog fxml file.");
            System.exit(-1);
        }
    }

    // methods
    public String getGenreFilename() {
        return controller.getGenreFilename();
    }

    public String getStgFilename() {
        return controller.getStgFilename();
    }

    public String getSerialFilename() {
        return controller.getSerialFilename();
    }

    public String getSeasonFilename() {
        return controller.getSeasonFilename();
    }

    public String getSeriesFilename() {
        return controller.getSeriesFilename();
    }

    public boolean isHaveResult() {
        return haveResult;
    }

    // events
    private void onOk() {
        haveResult = true;
        close();
    }

    private void onCancel() {
        haveResult = false;
        close();
    }
}
