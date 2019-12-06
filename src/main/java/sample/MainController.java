package sample;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import sample.RestoreDataDialog.RestoreDataDialog;
import sample.controllers.*;
import sample.database.Database;
import sample.database.records.Genre;
import sample.exceptions.AuthException;
import sample.exceptions.ConnectTimeoutException;
import sample.database.records.Season;
import sample.database.records.Serial;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ResourceBundle;


public class MainController implements Initializable {
    //auth
    @FXML private VBox authPane;
    @FXML private TextField hostField, loginField, passwordField;
    //db tab pane
    @FXML private TabPane databaseTabPane;
    @FXML private StackPane tablesStackPane;
    @FXML private Tab genresTab, usersTab;
    private Database database = null;
    private SerialsController serialsController;
    private SeasonsController seasonsController;
    private SeriesController seriesController;
    private GenresController genresController;
    private UsersController usersController;
    // restore/save
    @FXML private MenuItem restoreMenuItem, separatorDB, clearMenuItem;

    private DirectoryChooser directoryChooser = new DirectoryChooser();
    private RestoreDataDialog restoreDialog = new RestoreDataDialog();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            loadSerialsFxml();
            loadSeasonsFxml();
            loadSeriesFxml();
            loadGenresFxml();
            loadUsersFxml();

            serialsController.setVisible(true);
            seasonsController.setVisible(false);
            seriesController.setVisible(false);
        }
        catch (IOException e) {
            System.out.println("Error while loading serials fxml file.");
            System.exit(-1);
        }
    }

    // fxml
    private void loadSerialsFxml() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/tables/serials.fxml"));
        Parent root = loader.load();
        serialsController = loader.getController();
        tablesStackPane.getChildren().add(root);

        serialsController.init(this::onSerialSelected);
    }

    private void loadSeasonsFxml() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/tables/seasons.fxml"));
        Parent root = loader.load();
        seasonsController = loader.getController();
        tablesStackPane.getChildren().add(root);

        seasonsController.init(this::onBackFromSeason, this::onSeasonSelected);
    }

    private void loadSeriesFxml() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/tables/series.fxml"));
        Parent root = loader.load();
        seriesController = loader.getController();
        tablesStackPane.getChildren().add(root);

        seriesController.init(this::onBackFromSeries);
    }

    private void loadGenresFxml() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/tables/genres.fxml"));
        Parent root = loader.load();
        genresController = loader.getController();
        genresTab.setContent(root);
    }

    private void loadUsersFxml() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/tables/users.fxml"));
        Parent root = loader.load();
        usersController = loader.getController();
        usersTab.setContent(root);
    }

    // methods
    private void configureForGuest() {
        setEditableMainControllers(false);
        setDisabledRestoreMenuItems(true);

        genresTab.setDisable(true);
        databaseTabPane.getTabs().remove(genresTab);

        usersTab.setDisable(true);
        databaseTabPane.getTabs().remove(usersTab);
    }

    private void configureForEditor() {
        setEditableMainControllers(true);
        setDisabledRestoreMenuItems(true);

        genresTab.setDisable(false);
        if (!databaseTabPane.getTabs().contains(genresTab))
            databaseTabPane.getTabs().add(genresTab);

        usersTab.setDisable(true);
        databaseTabPane.getTabs().remove(usersTab);
    }

    private void configureForSuperuser() {
        setEditableMainControllers(true);
        setDisabledRestoreMenuItems(false);

        genresTab.setDisable(false);
        if (!databaseTabPane.getTabs().contains(genresTab))
            databaseTabPane.getTabs().add(genresTab);

        usersTab.setDisable(false);
        if (!databaseTabPane.getTabs().contains(usersTab))
            databaseTabPane.getTabs().add(usersTab);
    }

    private void setEditableMainControllers(boolean editable) {
        serialsController.setSerialsEditable(editable);
        seasonsController.setSeasonsEditable(editable);
        seriesController.setSeriesEditable(editable);
    }

    private void setDisabledRestoreMenuItems(boolean disable) {
        restoreMenuItem.setDisable(disable);
        separatorDB.setDisable(disable);
        clearMenuItem.setDisable(disable);
    }


    // fxml's events
    private void onSerialSelected(Serial serial) {
        serialsController.setVisible(false);
        seasonsController.setSerial(serial);
        seasonsController.setVisible(true);
    }

    private void onBackFromSeason() {
        seasonsController.setVisible(false);
        serialsController.setVisible(true);
    }

    private void onSeasonSelected(Season season) {
        seasonsController.setVisible(false);
        seriesController.setSeason(seasonsController.getCurrentSerial(), season);
        seriesController.setVisible(true);
    }

    private void onBackFromSeries() {
        seriesController.setVisible(false);
        seasonsController.setVisible(true);
    }

    // auth events
    @FXML private void onEnter() {
        try {
            database = new Database(hostField.getText(), loginField.getText(),
                    passwordField.getText());
            serialsController.setDatabase(database);
            seasonsController.setDatabase(database);
            seriesController.setDatabase(database);
            genresController.setDatabase(database);
            usersController.setDatabase(database);

            switch (database.getRole()) {
                case Guest: configureForGuest(); break;
                case Editor: configureForEditor(); break;
                case Superuser: configureForSuperuser(); break;
            }
            authPane.setVisible(false);
            databaseTabPane.setVisible(true);
        }
        catch (ConnectTimeoutException e) {
            Dialogs.showError("Ошибка подключения к базе данных.");
        }
        catch (AuthException e) {
            Dialogs.showError("Неверный логин или пароль.");
        }
    }

    @FXML private void onRootPressed() {
        loginField.setText("root");
        passwordField.setText("jcenjg");
        onEnter();
    }

    @FXML private void onEditor1Pressed() {
        loginField.setText("e1");
        passwordField.setText("qwe");
        onEnter();
    }

    @FXML private void onGuest1Pressed() {
        loginField.setText("g1");
        passwordField.setText("qwe");
        onEnter();
    }

    // menu bar
    @FXML private void onChangeUser() {
        if (database != null)
            database.stopCheckUpdates();
        database = null;

        databaseTabPane.setVisible(false);
        authPane.setVisible(true);
    }

    @FXML private void onExit() {
        System.exit(0);
    }

    @FXML private void onSaveData() {
        if (database == null) return;

        File file = directoryChooser.showDialog(null);
        if (file == null) return;

        boolean result;
        database.stopCheckUpdates();
        result = DataSaver.saveGenres(database.getGenres(), new File(file, "genre.txt"));
        result &= DataSaver.saveSerials(database.getSerials(), new File(file, "serial.txt"));
        result &= DataSaver.saveSeasons(database.getSeasons(), new File(file, "season.txt"));
        result &= DataSaver.saveSeries(database.getSeries(), new File(file, "series.txt"));
        result &= DataSaver.saveSTG(database.getSerials(), new File(file, "serial_to_genre.txt"));
        database.startCheckUpdates();

        if (!result) {
            Dialogs.showError("Ошибка сохранения одной из таблиц.");
        }
        else {
            Dialogs.showInfo("Данные сохранены.");
        }
    }

    @FXML private void onRestoreData() {
        if (database == null) return;

        restoreDialog.showAndWait();
        if (!restoreDialog.isHaveResult()) return;

        database.stopCheckUpdates();

        boolean res;
        res = database.restoreGenres(new File(restoreDialog.getGenreFilename()));
        res &= database.restoreSerials(new File(restoreDialog.getSerialFilename()));
        res &= database.restoreSeasons(new File(restoreDialog.getSeasonFilename()));
        res &= database.restoreSeries(new File(restoreDialog.getSeriesFilename()));
        res &= database.restoreSTG(new File(restoreDialog.getStgFilename()));

        if (!res) {
            Dialogs.showError("Ошибка восстановления данных.");
        }
        else {
            Dialogs.showInfo("Данные восстановлены.");
        }
        database.startCheckUpdates();
    }

    @FXML private void onClearData() {
        if (database == null) return;
        if (Dialogs.askFor("Удалить все сериалы и жанры?") == ButtonType.NO) return;

        database.stopCheckUpdates();
        if (database.deleteAll()) {
            Dialogs.showInfo("Данные удалены.");
        }
        else {
            Dialogs.showError("Ошибка удаления данных.");
        }
        database.startCheckUpdates();
    }

    @FXML private void onAuthor() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Студент МАИ\n" +
                "Группы М8О-313Б-17\nСеменов Сергей");
        alert.setTitle("( ͡° ͜ʖ ͡°)");
        alert.setHeaderText("Автор");
        alert.showAndWait();
    }

}
