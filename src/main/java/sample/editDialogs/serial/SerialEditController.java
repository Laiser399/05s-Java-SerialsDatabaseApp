package sample.editDialogs.serial;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import org.controlsfx.control.CheckListView;
import sample.database.Database;
import sample.Dialogs;
import sample.database.records.Genre;
import sample.database.records.Serial;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class SerialEditController {
    @FXML private TextField idField, nameField, officialSiteField, markField;
    @FXML private CheckListView<Genre> genresView;
    private Consumer<Optional<SerialEditData> > onResult = null;

    public void init(Consumer<Optional<SerialEditData> > onResult) {
        this.onResult = onResult;
    }

    public void setDatabase(Database database) {
        genresView.setItems(database.getGenres());
    }

    // methods
    public void prepareForEdit(Serial serial) {
        idField.setDisable(false);
        idField.setText(Integer.toString(serial.getId()));
        nameField.setText(serial.nameObservable().getValue());
        officialSiteField.setText(serial.officialSiteObservable().getValue());
        markField.setText(Double.toString(serial.markObservable().getValue()));

        genresView.getCheckModel().clearChecks();
        for (Genre genre : serial.genresObservable())
            genresView.getCheckModel().check(genre);
    }

    public void prepareForCreate() {
        idField.setDisable(true);
        idField.setText("");
        nameField.setText("");
        officialSiteField.setText("");
        markField.setText("");
        genresView.getCheckModel().clearChecks();
    }

    @FXML private void onOk() {
        if (onResult == null)
            return;

        try {
            List<Genre> selectedGenres = new ArrayList<>(genresView.getCheckModel().getCheckedItems());
            double mark = Double.parseDouble(markField.getText());
            if (mark < 0 || mark > 10)
                throw new NumberFormatException("Mark must be in range 0 to 10.");

            onResult.accept(Optional.of(new SerialEditData(
                    nameField.getText(),
                    officialSiteField.getText(),
                    mark,
                    selectedGenres
            )));
        }
        catch (NumberFormatException e) {
            Dialogs.showError("Оценка должна быть в диапазоне от 0 до 10.");
        }
    }

    @FXML private void onCancel() {
        if (onResult == null)
            return;
        onResult.accept(Optional.empty());
    }

}
