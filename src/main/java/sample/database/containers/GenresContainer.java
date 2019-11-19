package sample.database.containers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import sample.database.records.Genre;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class GenresContainer {
    private Map<Integer, Genre> genresById = new HashMap<>();
    private ObservableList<Genre> genres = FXCollections.observableArrayList();


    public void addOrUpdate(int id, String name) {
        Genre genre = genresById.get(id);
        if (genre == null)
            add(new Genre(id, name));
        else
            genre.setName(name);
    }

    public void remove(int id) {
        Genre genre = genresById.get(id);
        if (genre != null) {
            genresById.remove(id);
            genres.remove(genre);
        }
    }

    private void add(Genre genre) {
        genresById.put(genre.getId(), genre);
        genres.add(genre);
    }

    public Optional<Genre> getById(int id) {
        return Optional.ofNullable(genresById.get(id));
    }

    public ObservableList<Genre> getGenres() {
        return genres;
    }

}
