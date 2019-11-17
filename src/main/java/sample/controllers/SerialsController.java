package sample.controllers;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import sample.Database;
import sample.Dialogs;
import sample.records.Serial;
import sample.editDialogs.serial.SerialEditData;
import sample.editDialogs.serial.SerialEditorDialog;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class SerialsController implements Initializable {
    @FXML private VBox root;
    @FXML private HBox editBox;
    @FXML private TableView<Serial> tableView;
    @FXML private TableColumn<Serial, String> nameColumn, officialSiteColumn, genreColumn;
    @FXML private TableColumn<Serial, Double> markColumn;
    private SerialEditorDialog editorDialog = new SerialEditorDialog();;
    private Consumer<Serial> onSerialSelected = null;
    private Database database = null;

    // init
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        nameColumn.setCellValueFactory(param -> param.getValue().nameObservable());
        officialSiteColumn.setCellValueFactory(param -> param.getValue().officialSiteObservable());
        markColumn.setCellValueFactory(param -> param.getValue().markObservable());
        genreColumn.setCellValueFactory(param -> param.getValue().displayGenresObservable());
    }

    public void init(Consumer<Serial> onSerialSelected) {
        this.onSerialSelected = onSerialSelected;
    }

    public void setDatabase(Database database) {
        this.database = database;
        tableView.setItems(database.getSerials());
        editorDialog.setDatabase(database);
    }

    // methods
    private Optional<Serial> getSelected() {
        ObservableList<Serial> selected = tableView.getSelectionModel().getSelectedItems();
        return selected.size() == 1 ? Optional.of(selected.get(0)) : Optional.empty();
    }

    public void setVisible(boolean flag) {
        root.setVisible(flag);
    }

    public void setSerialsEditable(boolean editable) {
        editBox.setDisable(!editable);
        editBox.setVisible(editable);
        editBox.setManaged(editable);
    }

    // events
    @FXML private void onOfficialSitePressed() {
        Serial serial = getSelected().orElse(null);
        if (serial == null)
            return;

        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop == null || !desktop.isSupported(Desktop.Action.BROWSE))
            return;

        try {
            desktop.browse(URI.create(serial.officialSiteObservable().getValue()));
        }
        catch (IOException e) {
            ClipboardContent content = new ClipboardContent();
            content.putString(serial.officialSiteObservable().getValue());
            Clipboard.getSystemClipboard().setContent(content);

            Dialogs.showError("Ошибка открытия. Ссылка скопирована в буфер обмена.");
        }
    }

    @FXML private void onEditPressed() {
        Serial serial = getSelected().orElse(null);
        if (serial == null)
            return;

        SerialEditData data = editorDialog.edit(serial).orElse(null);
        if (data == null)
            return;

        if (!database.update(serial, data.name, data.officialSite, data.mark, data.genres)) {
            Dialogs.showError("Ошибка изменения сериала.");
        }
    }

    @FXML private void onCreatePressed() {
        SerialEditData data = editorDialog.create().orElse(null);
        if (data == null)
            return;
        if (!database.createSerial(data.name, data.officialSite, data.mark, data.genres)) {
            Dialogs.showError("Ошибка создания сериала.");
        }
    }

    @FXML private void onDeletePressed() {
        Serial serial = getSelected().orElse(null);
        if (serial == null)
            return;

        if (Dialogs.askFor("Удалить сериал со всеми сезонами и сериями?") == ButtonType.NO)
            return;

        if (!database.delete(serial))
            Dialogs.showError("Ошибка удаления сериала.");
    }

    @FXML private void onMouseClicked(MouseEvent event) {
        if (event.getClickCount() == 2) {
            List<Serial> serials =  tableView.getSelectionModel().getSelectedItems();
            if (serials.size() == 1 && onSerialSelected != null)
                onSerialSelected.accept(serials.get(0));
        }
    }

}
