package sample.RestoreDataDialog;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class RestoreDataDialogController implements Initializable {
    @FXML private TextField genreField, stgField, serialField, seasonField, seriesField;

    private File prevDir = null;
    private FileChooser fileChooser = new FileChooser();
    private Runnable onOk, onCancel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Текстовый файл", "*.txt"));
    }

    public void init(Runnable onOk, Runnable onCancel) {
        this.onOk = onOk;
        this.onCancel = onCancel;
    }

    // methods
    public String getGenreFilename() {
        return genreField.getText();
    }

    public String getStgFilename() {
        return stgField.getText();
    }

    public String getSerialFilename() {
        return serialField.getText();
    }

    public String getSeasonFilename() {
        return seasonField.getText();
    }

    public String getSeriesFilename() {
        return seriesField.getText();
    }

    // events
    @FXML private void onOverview(ActionEvent event) {
        if (prevDir != null) fileChooser.setInitialDirectory(prevDir);
        File file = fileChooser.showOpenDialog(null);
        if (file == null) return;
        prevDir = file.getParentFile();

        switch (((Button) event.getSource()).getId()) {
            case "0":
                genreField.setText(file.getAbsolutePath());
                break;
            case "1":
                stgField.setText(file.getAbsolutePath());
                break;
            case "2":
                serialField.setText(file.getAbsolutePath());
                break;
            case "3":
                seasonField.setText(file.getAbsolutePath());
                break;
            case "4":
                seriesField.setText(file.getAbsolutePath());
                break;
        }
    }

    @FXML private void onOk() {
        onOk.run();
    }

    @FXML private void onCancel() {
        onCancel.run();
    }
}
