package sample;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public class Dialogs {
    private static Alert alertError = new Alert(Alert.AlertType.ERROR);
    private static Alert alertInfo = new Alert(Alert.AlertType.INFORMATION);

    public static void showError(String message) {
        alertError.setTitle("(◑‿◐)");
        alertError.setHeaderText("Ошибка");
        alertError.setContentText(message);
        alertError.showAndWait();
    }

    public static void showInfo(String message) {
        alertInfo.setTitle("(ง°ل͜°)ง");
        alertInfo.setHeaderText("Информация");
        alertInfo.setContentText(message);
        alertInfo.showAndWait();
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
