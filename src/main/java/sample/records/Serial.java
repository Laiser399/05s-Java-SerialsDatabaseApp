package sample.records;

import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.util.List;

public class Serial {
    private int id;
    private StringProperty name = new SimpleStringProperty(),
                           officialSite = new SimpleStringProperty();
    private DoubleProperty mark = new SimpleDoubleProperty();
    private StringProperty displayGenres = new SimpleStringProperty();
    private ObservableList<Genre> genres = FXCollections.observableArrayList();

    public Serial(int id, String name, String officialSite, double mark, List<Genre> genres) {
        this.id = id;
        this.name.setValue(name);
        this.officialSite.setValue(officialSite);
        this.mark.setValue(mark);
        this.genres.addAll(genres);
        updateDisplayGenres();
        this.genres.addListener((ListChangeListener<? super Genre>) c -> updateDisplayGenres());
    }

    public int getId() {
        return id;
    }

    public ObservableValue<String> nameObservable() {
        return name;
    }

    public ObservableValue<String> officialSiteObservable() {
        return officialSite;
    }

    public ObservableValue<Double> markObservable() {
        return mark.asObject();
    }

    public ObservableValue<String> displayGenresObservable() {
        return displayGenres;
    }

    public ObservableList<Genre> genresObservable() {
        return genres;
    }

    public void setName(String name) {
        this.name.setValue(name);
    }

    public void setOfficialSite(String officialSite) {
        this.officialSite.setValue(officialSite);
    }

    public void setMark(double mark) {
        this.mark.setValue(mark);
    }

    public void setGenres(List<Genre> genres) {
        this.genres.clear();
        this.genres.addAll(genres);
    }

    private void updateDisplayGenres() {
        StringBuilder builder = new StringBuilder();
        for (Genre genre : genres) {
            if (builder.length() > 0)
                builder.append(", ");
            builder.append(genre.nameObservable().getValue());
        }
        displayGenres.setValue(builder.toString());
    }
}
