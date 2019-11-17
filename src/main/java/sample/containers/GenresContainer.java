package sample.containers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import sample.records.Genre;

import java.util.HashMap;
import java.util.Map;

public class GenresContainer {
    private Map<Integer, Genre> genresById = new HashMap<>();
    private ObservableList<Genre> genres = FXCollections.observableArrayList();

    public void add(Genre genre) {
        genresById.put(genre.getId(), genre);
        genres.add(genre);
    }

    public Genre getById(int id) {
        return genresById.get(id);
    }

    public ObservableList<Genre> getGenres() {
        return genres;
    }

}
