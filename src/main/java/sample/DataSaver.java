package sample;

import sample.database.records.Genre;
import sample.database.records.Season;
import sample.database.records.Serial;
import sample.database.records.Series;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Collection;

public class DataSaver {
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public static boolean saveGenres(Collection<Genre> genres, File file) {
        try (BufferedWriter writer = Files.newBufferedWriter(file.toPath())) {
            boolean isFirst = true;
            for (Genre genre : genres) {
                if (!isFirst)
                    writer.write("\n");
                isFirst = false;

                writer.write(Integer.toString(genre.getId()));
                writer.write(", ");
                writer.write("\"" + genre.nameObservable().getValue().replace("\"", "\\\"") + "\"");
            }
            return true;
        }
        catch (IOException e) {
            return false;
        }
    }

    public static boolean saveSerials(Collection<Serial> serials, File file) {
        try (BufferedWriter writer = Files.newBufferedWriter(file.toPath())) {
            boolean isFirst = true;
            for (Serial serial : serials) {
                if (!isFirst)
                    writer.write("\n");
                isFirst = false;

                writer.write(Integer.toString(serial.getId()));
                writer.write(", ");
                writer.write("\"" + serial.nameObservable().getValue().replace("\"", "\\\"") + "\"");
                writer.write(", ");
                writer.write("\"" + serial.officialSiteObservable().getValue().replace("\"", "\\\"") + "\"");
                writer.write(", ");
                writer.write(Double.toString(serial.markObservable().getValue()));
            }
            return true;
        }
        catch (IOException e) {
            return false;
        }
    }

    public static boolean saveSeasons(Collection<Season> seasons, File file) {
        try (BufferedWriter writer = Files.newBufferedWriter(file.toPath())) {
            boolean isFirst = true;
            for (Season season : seasons) {
                if (!isFirst)
                    writer.write("\n");
                isFirst = false;

                writer.write(Integer.toString(season.getId()));
                writer.write(", ");
                writer.write(Integer.toString(season.getIdSerial()));
                writer.write(", ");
                writer.write(season.numberObservable().getValue().toString());
                writer.write(", ");
                writer.write(season.seriesCountObservable().getValue().toString());
                writer.write(", ");
                writer.write("\"" + season.torrentLinkObservable().getValue().replace("\"", "\\\"") + "\"");
            }
            return true;
        }
        catch (IOException e) {
            return false;
        }
    }

    public static boolean saveSeries(Collection<Series> series, File file) {
        try (BufferedWriter writer = Files.newBufferedWriter(file.toPath())) {
            boolean isFirst = true;
            for (Series serie : series) {
                if (!isFirst)
                    writer.write("\n");
                isFirst = false;

                writer.write(Integer.toString(serie.getId()));
                writer.write(", ");
                writer.write(Integer.toString(serie.getIdSeason()));
                writer.write(", ");
                writer.write(serie.numberObservable().getValue().toString());
                writer.write(", ");
                writer.write("\"" + serie.nameObservable().getValue().replace("\"", "\\\"") + "\"");
                writer.write(", ");
                String date = dateFormat.format(serie.releaseDateObservable().getValue());
                writer.write("\"" + date + "\"");
                writer.write(", ");
                writer.write("\"" + serie.torrentLinkObservable().getValue().replace("\"", "\\\"") + "\"");

            }
            return true;
        }
        catch (IOException e) {
            return false;
        }
    }

    public static boolean saveSTG(Collection<Serial> serials, File file) {
        try (BufferedWriter writer = Files.newBufferedWriter(file.toPath())) {
            boolean isFirst = true;
            for (Serial serial : serials) {
                for (Genre genre : serial.genresObservable()) {
                    if (!isFirst)
                        writer.write("\n");
                    isFirst = false;

                    writer.write(Integer.toString(serial.getId()));
                    writer.write(", ");
                    writer.write(Integer.toString(genre.getId()));
                }
            }
            return true;
        }
        catch (IOException e) {
            return false;
        }
    }

}
