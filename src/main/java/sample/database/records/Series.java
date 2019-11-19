package sample.database.records;

import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Series {
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

    private int id, idSeason;
    private IntegerProperty number = new SimpleIntegerProperty();
    private StringProperty name = new SimpleStringProperty();
    private StringProperty displayReleaseDate = new SimpleStringProperty();
    private ObjectProperty<Date> releaseDate = new SimpleObjectProperty<>();
    private StringProperty torrentLink = new SimpleStringProperty();

    public Series(int id, int idSeason, int number, String name, Date releaseDate, String torrent) {
        this.releaseDate.addListener((observable, oldValue, newValue) ->
                updateDisplayReleaseDate());

        this.id = id;
        this.idSeason = idSeason;
        this.number.setValue(number);
        this.name.setValue(name);
        this.releaseDate.setValue(releaseDate);
        this.torrentLink.setValue(torrent);
    }

    private void updateDisplayReleaseDate() {
        displayReleaseDate.setValue(dateFormat.format(releaseDate.getValue()));
    }

    public int getId() {
        return id;
    }

    public int getIdSeason() {
        return idSeason;
    }

    public ObservableValue<Integer> numberObservable() {
        return number.asObject();
    }

    public ObservableValue<String> nameObservable() {
        return name;
    }

    public ObservableValue<String> displayReleaseDateObservable() {
        return displayReleaseDate;
    }

    public ObservableValue<Date> releaseDateObservable() {
        return releaseDate;
    }

    public ObservableValue<String> torrentLinkObservable() {
        return torrentLink;
    }

    public void setNumber(int number) {
        this.number.set(number);
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public void setReleaseDate(Date releaseDate) {
        this.releaseDate.set(releaseDate);
    }

    public void setTorrentLink(String torrentLink) {
        this.torrentLink.set(torrentLink);
    }
}
