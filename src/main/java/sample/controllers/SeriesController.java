package sample.controllers;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import sample.Database;
import sample.Dialogs;
import sample.editDialogs.series.SeriesEditData;
import sample.editDialogs.series.SeriesEditorDialog;
import sample.records.Season;
import sample.records.Serial;
import sample.records.Series;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;


// TODO мб сделать добавление/удаление в create/delete
public class SeriesController implements Initializable {
    @FXML private VBox root;
    @FXML private TextField serialNameField, seasonNumberField;
    @FXML private HBox editBox;
    @FXML private TableView<Series> tableView;
    @FXML private TableColumn<Series, Integer> numberColumn;
    @FXML private TableColumn<Series, String> nameColumn, releaseDateColumn, torrentColumn;
    private Database database = null;
    private Runnable onBackPressed = null;
    private Season currentSeason = null;
    private SeriesEditorDialog editorDialog = new SeriesEditorDialog();

    // init
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        numberColumn.setCellValueFactory(param -> param.getValue().numberObservable());
        nameColumn.setCellValueFactory(param -> param.getValue().nameObservable());
        releaseDateColumn.setCellValueFactory(param -> param.getValue().displayReleaseDateObservable());
        torrentColumn.setCellValueFactory(param -> param.getValue().torrentLinkObservable());
    }

    public void init(Runnable onBackPressed) {
        this.onBackPressed = onBackPressed;
    }

    public void setDatabase(Database database) {
        this.database = database;
    }

    // methods
    private Optional<Series> getSelected() {
        ObservableList<Series> selected = tableView.getSelectionModel().getSelectedItems();
        return selected.size() == 1 ? Optional.of(selected.get(0)) : Optional.empty();
    }

    public void setVisible(boolean flag) {
        root.setVisible(flag);
    }

    public void setSeason(Serial serial, Season season) {
        currentSeason = season;
        serialNameField.setText(serial.nameObservable().getValue());
        seasonNumberField.setText(season.numberObservable().getValue().toString());
        tableView.setItems(database.getSeriesFor(season.getId()));
    }

    public void setSeriesEditable(boolean editable) {
        editBox.setDisable(!editable);
        editBox.setVisible(editable);
        editBox.setManaged(editable);
    }

    //------------|
    //   events   |
    //------------|
    @FXML private void onBackPressed() {
        if (onBackPressed != null)
            onBackPressed.run();
    }

    @FXML private void onTorrentPressed() {
        Series series = getSelected().orElse(null);
        if (series == null)
            return;

        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop == null || !desktop.isSupported(Desktop.Action.BROWSE))
            return;

        try {
            desktop.browse(URI.create(series.torrentLinkObservable().getValue()));
        }
        catch (IOException e) {
            ClipboardContent content = new ClipboardContent();
            content.putString(series.torrentLinkObservable().getValue());
            Clipboard.getSystemClipboard().setContent(content);

            Dialogs.showError("Ошибка открытия. Ссылка скопирована в буфер обмена.");
        }
    }

    @FXML private void onEditPressed() {
        Series series = getSelected().orElse(null);
        if (series == null)
            return;

        SeriesEditData data = editorDialog.edit(series).orElse(null);
        if (data == null)
            return;

        if (!database.update(series, data.number, data.name, data.releaseDate, data.torrent)) {
            Dialogs.showError("Ошибка изменения серии.");
        }
    }

    @FXML private void onCreatePressed() {
        if (currentSeason == null)
            return;
        SeriesEditData data = editorDialog.create().orElse(null);
        if (data == null)
            return;

        if (!database.createSeries(currentSeason, data.number, data.name, data.releaseDate, data.torrent)) {
            Dialogs.showError("Ошибка создания серии.");
        }
    }

    @FXML private void onDeletePressed() {
        Series series = getSelected().orElse(null);
        if (series == null)
            return;

        if (Dialogs.askFor("Удалить серию?") == ButtonType.NO)
            return;

        if (!database.delete(series))
            Dialogs.showError("Ошибка удаления серии.");
    }

}
