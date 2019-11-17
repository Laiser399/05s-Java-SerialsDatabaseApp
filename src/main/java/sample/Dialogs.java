package sample;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public class Dialogs {
    private static Alert alert = new Alert(Alert.AlertType.ERROR);

    public static void showError(String message) {
        alert.setTitle("(◑‿◐)");
        alert.setHeaderText("Ошибка");
        alert.setContentText(message);
        alert.showAndWait();
    }

    // TODO del if useless
    public static void showInformation(String message) {
        alert.setTitle("");// TODO smile
        alert.setHeaderText("Ошибка");
        alert.setContentText(message);
        alert.showAndWait();
    }


    private static Alert askAlert = new Alert(Alert.AlertType.CONFIRMATION, "",
            ButtonType.YES, ButtonType.NO);

    public static ButtonType askFor(String message) {
        askAlert.setTitle("╘[◉﹃◉]╕");
        askAlert.setHeaderText("?");
        askAlert.setContentText(message);
        askAlert.showAndWait();
        return askAlert.getResult();
    }


}
