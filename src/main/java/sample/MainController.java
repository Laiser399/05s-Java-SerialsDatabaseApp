package sample;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import sample.controllers.*;
import sample.exceptions.AuthException;
import sample.exceptions.ConnectTimeoutException;
import sample.records.Season;
import sample.records.Serial;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

// TODO menu bar and window title
public class MainController implements Initializable {
    //auth
    @FXML private VBox authPane;
    @FXML private TextField hostField, loginField, passwordField;
    //db tab pane
    @FXML private TabPane databaseTabPane;
    @FXML private StackPane tablesStackPane;
    @FXML private Tab genresTab, usersTab;
    private SerialsController serialsController;
    private SeasonsController seasonsController;
    private SeriesController seriesController;
    private GenresController genresController;
    private UsersController usersController;

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

        genresTab.setDisable(true);
        databaseTabPane.getTabs().remove(genresTab);

        usersTab.setDisable(true);
        databaseTabPane.getTabs().remove(usersTab);
    }

    private void configureForEditor() {
        setEditableMainControllers(true);

        genresTab.setDisable(false);
        if (!databaseTabPane.getTabs().contains(genresTab))
            databaseTabPane.getTabs().add(genresTab);

        usersTab.setDisable(true);
        databaseTabPane.getTabs().remove(usersTab);
    }

    private void configureForSuperuser() {
        setEditableMainControllers(true);

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
            Database database = new Database(hostField.getText(), loginField.getText(),
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
    @FXML private void onTest() {

    }
}
