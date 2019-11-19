package sample.editDialogs.genre;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import sample.database.records.Genre;

import java.util.Optional;
import java.util.function.Consumer;


public class GenreEditController {


    @FXML private TextField idField, nameField;
    private Consumer<Optional<GenreEditData> > onResult = null;

    public void init(Consumer<Optional<GenreEditData> > onResult) {
        this.onResult = onResult;
    }

    public void prepareForEdit(Genre genre) {
        idField.setDisable(false);

        idField.setText(Integer.toString(genre.getId()));
        nameField.setText(genre.nameObservable().getValue());
    }

    public void prepareForCreate() {
        idField.setDisable(true);

        idField.setText("");
        nameField.setText("");
    }

    @FXML private void onOk() {
        if (onResult == null)
            return;
        onResult.accept(Optional.of(new GenreEditData(nameField.getText())));
    }

    @FXML private void onCancel() {
        if (onResult == null)
            return;
        onResult.accept(Optional.empty());
    }
}
