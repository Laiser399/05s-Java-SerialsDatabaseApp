package sample.controllers;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import sample.database.Database;
import sample.Dialogs;
import sample.editDialogs.genre.GenreEditData;
import sample.editDialogs.genre.GenreEditDialog;
import sample.database.records.Genre;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class GenresController implements Initializable {
    @FXML private TableView<Genre> tableView;
    @FXML private TableColumn<Genre, String> nameColumn;
    private Database database = null;
    private GenreEditDialog editDialog = new GenreEditDialog();


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        nameColumn.setCellValueFactory(param -> param.getValue().nameObservable());
    }

    public void setDatabase(Database database) {
        this.database = database;
        tableView.setItems(database.getGenres());
    }

    // methods
    private Optional<Genre> getSelected() {
        ObservableList<Genre> selected = tableView.getSelectionModel().getSelectedItems();
        return selected.size() == 1 ? Optional.of(selected.get(0)) : Optional.empty();
    }

    // events
    @FXML private void onEditPressed() {
        Genre genre = getSelected().orElse(null);
        if (genre == null)
            return;

        GenreEditData data = editDialog.edit(genre).orElse(null);
        if (data == null)
            return;

        if (!database.update(genre, data.name)) {
            Dialogs.showError("Ошибка изменения жанра.");
        }
    }

    @FXML private void onCreatePressed() {
        GenreEditData data = editDialog.create().orElse(null);
        if (data == null)
            return;

        if (!database.createGenre(data.name)) {
            Dialogs.showError("Ошибка создания жанра.");
        }
    }

    @FXML private void onDeletePressed() {
        Genre genre = getSelected().orElse(null);
        if (genre == null)
            return;

        if (Dialogs.askFor("Удалить жанр \"" + genre.nameObservable().getValue() + "\"?") == ButtonType.NO)
            return;

        if (!database.delete(genre)) {
            Dialogs.showError("Ошибка удаления жанра.");
        }
    }
}
