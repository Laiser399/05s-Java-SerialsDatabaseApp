package sample;

import javafx.collections.ObservableList;
import sample.containers.GenresContainer;
import sample.containers.SeasonsContainer;
import sample.containers.SerialsContainer;
import sample.containers.SeriesContainer;
import sample.exceptions.AuthException;
import sample.exceptions.ConnectTimeoutException;
import sample.records.Genre;
import sample.records.Season;
import sample.records.Serial;
import sample.records.Series;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Database {
    public enum Role {
        Guest("Гость"), Editor("Редактор"), Superuser("Царь");

        private String displayRole;

        Role(String displayRole) {
            this.displayRole = displayRole;
        }

        @Override
        public String toString() {
            return displayRole;
        }
    }

    private Role role = Role.Guest;
    private String host, login, password;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private GenresContainer genres = new GenresContainer();
    private SerialsContainer serials = new SerialsContainer();
    private SeasonsContainer seasons = new SeasonsContainer();
    private SeriesContainer seriesContainer = new SeriesContainer();

    // init
    public Database(String host, String login, String password) throws ConnectTimeoutException, AuthException {
        this.host = host;
        this.login = login;
        this.password = password;

        try (Connection connection = getConnection()) {
            initGenres(connection);
            initSerials(connection);
            initSeasons(connection);
            initSeries(connection);
            initRole(connection);
        }
        catch (SQLTimeoutException e) {
            throw new ConnectTimeoutException();
        }
        catch (SQLException e) {
            throw new AuthException();
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                "jdbc:mysql://" + host + "/serials_cw?serverTimezone=Europe/Moscow",
                login, password);
    }

    private void initGenres(Connection connection) throws SQLException {
        Statement statement = connection.createStatement();
        if (statement.execute("CALL get_all_genres();")) {
            ResultSet result = statement.getResultSet();
            while (result.next()) {
                int id = result.getInt("id");
                String name = result.getString("name");
                genres.add(new Genre(id, name));
            }
        }
    }

    private void initSerials(Connection connection) throws SQLException {
        Statement statement = connection.createStatement();
        if (statement.execute("CALL get_all_serials();")) {
            ResultSet result = statement.getResultSet();
            while (result.next()) {
                int id = result.getInt("id");
                String name = result.getString("name");
                String officialSite = result.getString("official_site");
                double mark = result.getDouble("mark");

                List<Genre> genres = getSerialGenres(id, connection);
                serials.add(new Serial(id, name, officialSite, mark, genres));
            }
        }
    }

    private void initSeasons(Connection connection) throws SQLException {
        Statement statement = connection.createStatement();
        if (statement.execute("CALL get_all_seasons();")) {
            ResultSet result = statement.getResultSet();
            while (result.next()) {
                int id = result.getInt("id");
                int idSerial = result.getInt("id_serial");
                int number = result.getInt("number");
                int seriesCount = result.getInt("series_count");
                String torrentLink = result.getString("torrent_link");

                seasons.add(new Season(id, idSerial, number, seriesCount, torrentLink));
            }
        }
    }

    private void initSeries(Connection connection) throws SQLException {
        Statement statement = connection.createStatement();
        if (statement.execute("CALL get_all_series();")) {
            ResultSet result = statement.getResultSet();
            while (result.next()) {
                int id = result.getInt("id");
                int idSeason = result.getInt("id_season");
                int number = result.getInt("number");
                String name = result.getString("name");
                Date releaseDate = result.getDate("release_date");
                String torrentLink = result.getString("torrent_link");

                seriesContainer.add(new Series(id, idSeason, number, name, releaseDate, torrentLink));
            }
        }
    }

    private void initRole(Connection connection) throws SQLException {
        Statement statement = connection.createStatement();
        statement.execute("CALL get_role();");
        ResultSet result = statement.getResultSet();
        String stringRole = "";
        while (result.next())
            stringRole = result.getString("role");

        switch (stringRole) {
            case "guest":
                role = Role.Guest; break;
            case "editor":
                role = Role.Editor; break;
            case "superuser":
                role = Role.Superuser; break;
        }
    }

    // methods
    public Role getRole() {
        return role;
    }

    // observable
    private List<Genre> getSerialGenres(int idSerial, Connection connection) throws SQLException {
        Statement genresStatement = connection.createStatement();
        List<Genre> result = new ArrayList<>();
        if (genresStatement.execute("CALL get_genres_id_for(" + idSerial + ");")) {
            ResultSet resultIds = genresStatement.getResultSet();
            while (resultIds.next()) {
                result.add(genres.getById(resultIds.getInt("id_genre")));
            }
        }
        return result;
    }

    public ObservableList<Genre> getGenres() {
        return genres.getGenres();
    }

    public ObservableList<Serial> getSerials() {
        return serials.getSerialsObservable();
    }

    public ObservableList<Season> getSeasonsFor(int serialId) {
        return seasons.getBySerialId(serialId);
    }

    public ObservableList<Series> getSeriesFor(int seasonId) {
        return seriesContainer.getBySeasonId(seasonId);
    }

    // update, create, delete
    public boolean update(Serial serial, String name, String officialSite, double mark, List<Genre> genres) {
        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);
            Statement statement = connection.createStatement();
            try {
                statement.execute("CALL update_serial(" +
                        serial.getId() + ", " +
                        "\"" + name + "\", " +
                        "\"" + officialSite + "\", " +
                        mark +
                        ");");
                statement.execute("CALL clear_genres(" + serial.getId() + ");");
                for (Genre genre : genres) {
                    statement.execute("CALL add_genre_to_serial(" +
                            serial.getId() + ", " +
                            genre.getId() +
                            ");");
                }
                connection.commit();

                serial.setName(name);
                serial.setOfficialSite(officialSite);
                serial.setMark(mark);
                serial.setGenres(genres);
                return true;
            }
            catch (SQLException e) {
                connection.rollback();
                return false;
            }
        }
        catch (SQLException e) {
            System.out.println("Rollback error.");
            return false;
        }
    }

    public boolean createSerial(String name, String officialSite, double mark, List<Genre> genres) {
        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);
            Statement statement = connection.createStatement();
            try {
                statement.execute("CALL create_serial(" +
                        "\"" + name + "\", " +
                        "\"" + officialSite + "\", " +
                        mark +
                        ");");
                int idSerial = 0;
                ResultSet result = statement.getResultSet();
                while (result.next())
                    idSerial = result.getInt(1);

                for (Genre genre : genres) {
                    statement.execute("CALL add_genre_to_serial(" +
                            idSerial + ", " +
                            genre.getId() +
                            ");");
                }
                connection.commit();
                return true;
            }
            catch (SQLException e) {
                System.out.println(e);
                connection.rollback();
                return false;
            }
        }
        catch (SQLException e) {
            System.out.println("Rollback error.");
            return false;
        }
    }

    public boolean delete(Serial serial) {
        try (Connection connection = getConnection()) {
            Statement statement = connection.createStatement();
            statement.execute("CALL delete_serial(" +
                    serial.getId() +
                    ");");
            return true;
        }
        catch (SQLException e) {
            return false;
        }
    }

    public boolean update(Season season, int number, int seriesCount, String torrent) {
        try (Connection connection = getConnection()) {
            Statement statement = connection.createStatement();
            statement.execute("CALL update_season(" +
                    season.getId() + ", " +
                    number + ", " +
                    seriesCount + ", " +
                    "\"" + torrent + "\"" +
                    ");");

            season.setNumber(number);
            season.setSeriesCount(seriesCount);
            season.setTorrentLink(torrent);
            return true;
        }
        catch (SQLException e) {
            return false;
        }
    }

    public boolean createSeason(Serial owner, int number, int seriesCount, String torrent) {
        try (Connection connection = getConnection()) {
            Statement statement = connection.createStatement();
            statement.execute("CALL create_season(" +
                    owner.getId() + ", " +
                    number + ", " +
                    seriesCount + ", " +
                    "\"" + torrent + "\"" +
                    ");");
//            ResultSet result = statement.getResultSet();
//            int idSeason = 0;
//            while (result.next())
//                idSeason = result.getInt(1);
            // TODO add to seasons container

            return true;
        }
        catch (SQLException e) {
            System.out.println(e); // TODO delete
            return false;
        }
    }

    public boolean delete(Season season) {
        try (Connection connection = getConnection()) {
            Statement statement = connection.createStatement();
            statement.execute("CALL delete_season(" +
                    season.getId() +
                    ");");
            return true;
        }
        catch (SQLException e) {
            return false;
        }
    }

    public boolean update(Series series, int number, String name, Date releaseDate, String torrent) {
        try (Connection connection = getConnection()) {
            Statement statement = connection.createStatement();
            statement.execute("CALL update_series(" +
                    series.getId() + ", " +
                    number + ", " +
                    "\"" + name + "\", " +
                    "\"" + dateFormat.format(releaseDate) + "\", " +
                    "\"" + torrent + "\"" +
                    ");");
            // TODO update in container (maybe)
            return true;
        }
        catch (SQLException e) {
            System.out.println(e);// TODO delete
            return false;
        }
    }

    public boolean createSeries(Season owner, int number, String name, Date releaseDate, String torrent) {
        try (Connection connection = getConnection()) {
            Statement statement = connection.createStatement();
            statement.execute("CALL create_series(" +
                    owner.getId() + ", " +
                    number + ", " +
                    "\"" + name + "\", " +
                    "\"" + dateFormat.format(releaseDate) + "\", " +
                    "\"" + torrent + "\"" +
                    ");");
            ResultSet result = statement.getResultSet();
            int idInserted = 0;
            while (result.next())
                idInserted = result.getInt(1);
            // TODO insert into container (maybe)


            return true;
        }
        catch (SQLException e) {
            System.out.println(e); // TODO delete
            return false;
        }
    }

    public boolean delete(Series series) {
        try (Connection connection = getConnection()) {
            Statement statement = connection.createStatement();
            statement.execute("CALL delete_series(" +
                    series.getId() +
                    ");");
            // TODO delete from container (maybe)
            return true;
        }
        catch (SQLException e) {
            System.out.println(e); // TODO delete
            return false;
        }
    }


}
