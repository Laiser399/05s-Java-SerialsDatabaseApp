package sample.editDialogs.season;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import sample.Dialogs;
import sample.records.Season;

import java.util.Optional;
import java.util.function.Consumer;

public class SeasonEditorController {
    @FXML private TextField idField, idSerialField, numberField, seriesCountField, torrentField;
    private Consumer<Optional<SeasonEditData> > onResult = null;

    public void init(Consumer<Optional<SeasonEditData> > onResult) {
        this.onResult = onResult;
    }

    public void prepareForEdit(Season season) {
        idField.setDisable(false);
        idSerialField.setDisable(false);
        idField.setText(Integer.toString(season.getId()));
        idSerialField.setText(Integer.toString(season.getIdSerial()));
        numberField.setText(season.numberObservable().getValue().toString());
        seriesCountField.setText(season.seriesCountObservable().getValue().toString());
        torrentField.setText(season.torrentLinkObservable().getValue());
    }

    public void prepareForCreate() {
        idField.setDisable(true);
        idSerialField.setDisable(true);
        idField.setText("");
        idSerialField.setText("");
        numberField.setText("");
        seriesCountField.setText("");
        torrentField.setText("");
    }

    @FXML private void onOk() {
        if (onResult == null)
            return;

        try {
            int number = Integer.parseInt(numberField.getText());
            int seriesCount = Integer.parseInt(seriesCountField.getText());
            String torrent = torrentField.getText();

            onResult.accept(Optional.of(new SeasonEditData(
                    number, seriesCount, torrent
            )));
        }
        catch (NumberFormatException e) {
            Dialogs.showError("Неверный формат номера.");
        }
    }

    @FXML private void onCancel() {
        if (onResult == null)
            return;
        onResult.accept(Optional.empty());
    }

}
