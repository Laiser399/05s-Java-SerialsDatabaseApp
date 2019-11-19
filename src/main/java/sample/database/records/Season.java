package sample.database.records;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;

public class Season {
    private int id, idSerial;
    private IntegerProperty number = new SimpleIntegerProperty(),
                            seriesCount = new SimpleIntegerProperty();
    private StringProperty torrentLink = new SimpleStringProperty();


    public Season(int id, int idSerial, int number, int seriesCount, String torrentLink) {
        this.id = id;
        this.idSerial = idSerial;
        this.number.setValue(number);
        this.seriesCount.setValue(seriesCount);
        this.torrentLink.setValue(torrentLink);
    }

    public int getId() {
        return id;
    }

    public int getIdSerial() {
        return idSerial;
    }

    public ObservableValue<Integer> numberObservable() {
        return number.asObject();
    }

    public ObservableValue<Integer> seriesCountObservable() {
        return seriesCount.asObject();
    }

    public ObservableValue<String> torrentLinkObservable() {
        return torrentLink;
    }

    public void setNumber(int number) {
        this.number.setValue(number);
    }

    public void setSeriesCount(int seriesCount) {
        this.seriesCount.setValue(seriesCount);
    }

    public void setTorrentLink(String torrentLink) {
        this.torrentLink.setValue(torrentLink);
    }
}
