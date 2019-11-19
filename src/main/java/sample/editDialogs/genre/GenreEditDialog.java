package sample.editDialogs.genre;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import sample.database.records.Genre;

import java.io.IOException;
import java.util.Optional;

public class GenreEditDialog extends Stage {
    private GenreEditController controller;
    private Optional<GenreEditData> result = Optional.empty();

    public GenreEditDialog() {
        try {
            initModality(Modality.APPLICATION_MODAL);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/editors/genreEditor.fxml"));
            Parent root = loader.load();
            setScene(new Scene(root));
            setMinWidth(300);
            setMinHeight(140);
            setMaxHeight(140);

            controller = loader.getController();
            controller.init(this::onResult);
        }
        catch (IOException e) {
            System.out.println("Error while loading genre fxml file.");
            System.exit(-1);
        }
    }

    private void onResult(Optional<GenreEditData> result) {
        this.result = result;
        close();
    }

    public Optional<GenreEditData> edit(Genre genre) {
        controller.prepareForEdit(genre);
        showAndWait();
        return result;
    }

    public Optional<GenreEditData> create() {
        controller.prepareForCreate();
        showAndWait();
        return result;
    }
}
