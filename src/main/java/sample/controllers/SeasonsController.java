package sample.controllers;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import sample.database.Database;
import sample.Dialogs;
import sample.editDialogs.season.SeasonEditData;
import sample.editDialogs.season.SeasonEditDialog;
import sample.database.records.Season;
import sample.database.records.Serial;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class SeasonsController implements Initializable {
    @FXML private VBox root;
    @FXML private TextField serialNameField;
    @FXML private HBox editBox;
    @FXML private TableView<Season> tableView;
    @FXML private TableColumn<Season, Integer> numberColumn, seriesCountColumn;
    @FXML private TableColumn<Season, String> torrentColumn;
    private SeasonEditDialog editorDialog = new SeasonEditDialog();
    private Database database = null;
    private Runnable onBackPressed = null;
    private Consumer<Season> onSeasonSelected = null;
    private Serial currentSerial = null;

    // init
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        numberColumn.setCellValueFactory(param -> param.getValue().numberObservable());
        seriesCountColumn.setCellValueFactory(param -> param.getValue().seriesCountObservable());
        torrentColumn.setCellValueFactory(param -> param.getValue().torrentLinkObservable());
    }

    public void init(Runnable onBackPressed, Consumer<Season> onSeasonSelected) {
        this.onBackPressed = onBackPressed;
        this.onSeasonSelected = onSeasonSelected;
    }

    public void setDatabase(Database database) {
        this.database = database;
    }

    // methods
    private Optional<Season> getSelected() {
        ObservableList<Season> selected = tableView.getSelectionModel().getSelectedItems();
        return selected.size() == 1 ? Optional.of(selected.get(0)) : Optional.empty();
    }

    public void setVisible(boolean flag) {
        root.setVisible(flag);
    }

    public void setSerial(Serial serial) {
        currentSerial = serial;
        serialNameField.setText(serial.nameObservable().getValue());
        tableView.setItems(database.getSeasonsFor(serial.getId()));
    }

    public Serial getCurrentSerial() {
        return currentSerial;
    }

    public void setSeasonsEditable(boolean editable) {
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
        Season season = getSelected().orElse(null);
        if (season == null)
            return;

        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop == null || !desktop.isSupported(Desktop.Action.BROWSE))
            return;

        try {
            desktop.browse(URI.create(season.torrentLinkObservable().getValue()));
        }
        catch (IOException e) {
            ClipboardContent content = new ClipboardContent();
            content.putString(season.torrentLinkObservable().getValue());
            Clipboard.getSystemClipboard().setContent(content);

            Dialogs.showError("Ошибка открытия. Ссылка скопирована в буфер обмена.");
        }
    }

    @FXML private void onEditPressed() {
        Season season = getSelected().orElse(null);
        if (season == null)
            return;

        SeasonEditData data = editorDialog.edit(season).orElse(null);
        if (data == null)
            return;

        if (!database.update(season, data.number, data.seriesCount, data.torrent)) {
            Dialogs.showError("Ошибка изменения сезона.");
        }
    }

    @FXML private void onCreatePressed() {
        if(currentSerial == null)
            return;
        SeasonEditData data = editorDialog.create().orElse(null);
        if (data == null)
            return;

        if (!database.createSeason(currentSerial, data.number, data.seriesCount, data.torrent)) {
             Dialogs.showError("Ошибка создания сезона.");
        }
    }

    @FXML private void onDeletePressed() {
        Season season = getSelected().orElse(null);
        if (season == null)
            return;

        if (Dialogs.askFor("Удалить сезон со всеми сериями?") == ButtonType.NO)
            return;

        if (!database.delete(season))
            Dialogs.showError("Ошибка удаления сезона.");
    }

    @FXML private void onMouseClicked(MouseEvent event) {
        if (event.getClickCount() == 2) {
            List<Season> seasons =  tableView.getSelectionModel().getSelectedItems();
            if (seasons.size() == 1 && onSeasonSelected != null)
                onSeasonSelected.accept(seasons.get(0));
        }
    }


}
