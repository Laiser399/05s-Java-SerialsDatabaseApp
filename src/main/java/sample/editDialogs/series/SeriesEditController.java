package sample.editDialogs.series;

import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import sample.Dialogs;
import sample.Main;
import sample.database.records.Series;

import java.time.LocalDate;
import java.util.Date;
import java.util.Optional;
import java.util.function.Consumer;

public class SeriesEditController {
    @FXML private TextField idField, idSeasonField, numberField, nameField, torrentField;
    @FXML private DatePicker releaseDatePicker;
    private Consumer<Optional<SeriesEditData> > onResult = null;

    public void init(Consumer<Optional<SeriesEditData> > onResult) {
        this.onResult = onResult;
    }

    public void prepareForEdit(Series series) {
        idField.setDisable(false);
        idSeasonField.setDisable(false);

        idField.setText(Integer.toString(series.getId()));
        idSeasonField.setText(Integer.toString(series.getIdSeason()));
        numberField.setText(series.numberObservable().getValue().toString());
        nameField.setText(series.nameObservable().getValue());
        releaseDatePicker.setValue(Main.toLocalDate(series.releaseDateObservable().getValue()));
        torrentField.setText(series.torrentLinkObservable().getValue());
    }

    public void prepareForCreate() {
        idField.setDisable(true);
        idSeasonField.setDisable(true);

        idField.setText("");
        idSeasonField.setText("");
        numberField.setText("");
        nameField.setText("");
        releaseDatePicker.setValue(LocalDate.now());
        torrentField.setText("");
    }

    @FXML private void onOk() {
        if (onResult == null)
            return;

        try {
            int number = Integer.parseInt(numberField.getText());
            String name = nameField.getText();
            Date releaseDate = Main.toDate(releaseDatePicker.getValue());
            String torrent = torrentField.getText();

            onResult.accept(Optional.of(new SeriesEditData(
                    number, name, releaseDate, torrent
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
