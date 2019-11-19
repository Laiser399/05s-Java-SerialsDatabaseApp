package sample.database;

import javafx.collections.ObservableList;
import sample.RunnableTask;
import sample.database.containers.*;
import sample.exceptions.AuthException;
import sample.exceptions.ConnectTimeoutException;
import sample.database.records.*;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

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
    private int lastMainChangeId = 0, lastStgChangeId = 0;
    private Timer timer = new Timer(true);
    private GenresContainer genres = new GenresContainer();
    private SerialsContainer serials = new SerialsContainer();
    private SeasonsContainer seasons = new SeasonsContainer();
    private SeriesContainer series = new SeriesContainer();
    private UsersContainer users = new UsersContainer();


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
            initUsersIfNeed();
            initLastChangeIds(connection);

            timer.schedule(new RunnableTask(this::checkUpdates), 0, 1000);

            if (role == Role.Superuser) {
                timer.schedule(new RunnableTask(this::checkUsersUpdates), 0, 5000);
            }
        }
        catch (SQLTimeoutException e) {
            throw new ConnectTimeoutException();
        }
        catch (SQLException e) {
            throw new AuthException();
        }
    }

    public void close() {
        if (timer != null) {
            timer.cancel();
            timer = null;
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
                genres.addOrUpdate(id, name);
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
                serials.addOrUpdate(id, name, officialSite, mark, genres);
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

                seasons.addOrUpdate(id, idSerial, number, seriesCount, torrentLink);
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

                series.addOrUpdate(id, idSeason, number, name, releaseDate, torrentLink);
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

    private void initUsersIfNeed() {
        if (role != Role.Superuser)
            return;
        checkUsersUpdates();
    }

    private void initLastChangeIds(Connection connection) throws SQLException {
        Statement statement = connection.createStatement();
        statement.execute("CALL get_last_change_ids();");
        ResultSet result = statement.getResultSet();
        while (result.next()) {
            lastMainChangeId = result.getInt("id_main");
            lastStgChangeId = result.getInt("id_stg");
        }
    }

    // methods
    public Role getRole() {
        return role;
    }

    // check db updates
    private void checkUpdates() {
        try (Connection connection = getConnection()) {
            checkMainUpdates(connection);
            checkStgUpdates(connection);
        }
        catch (SQLException e) {
            System.out.println("Error check updates: " + e.getMessage());
        }
    }

    private void checkMainUpdates(Connection connection) throws SQLException {
        Statement statement = connection.createStatement();
        statement.execute("CALL get_changes_after(" +
                lastMainChangeId +
                ");");
        ResultSet result = statement.getResultSet();
        while (result.next()) {
            lastMainChangeId = result.getInt("id");
            String tableName = result.getString("table_name");
            int id = result.getInt("id_row");
            applyMainChange(connection, tableName, id);
        }
    }

    private void checkStgUpdates(Connection connection) throws SQLException {
        Statement statement = connection.createStatement();
        statement.execute("CALL get_stg_changes_after(" +
                lastStgChangeId +
                ");");
        ResultSet result = statement.getResultSet();
        while (result.next()) {
            lastStgChangeId = result.getInt("id");
            int idSerial = result.getInt("id_serial");
            int idGenre = result.getInt("id_genre");
            String type = result.getString("type");
            applyStgChange(idSerial, idGenre, type);
        }
    }

    private void applyMainChange(Connection connection, String tableName, int id) throws SQLException {
        switch (tableName) {
            case "genre":  applyGenreChange(connection, id); break;
            case "serial": applySerialChange(connection, id); break;
            case "season": applySeasonChange(connection, id); break;
            case "series": applySeriesChange(connection, id); break;
        }
    }

    private void applyGenreChange(Connection connection, int id) throws SQLException {
        Statement statement = connection.createStatement();
        statement.execute("CALL get_genre_by_id(" + id + ");");
        ResultSet result = statement.getResultSet();
        if (result.next()) {
            String name = result.getString("name");
            genres.addOrUpdate(id, name);
        }
        else {
            genres.remove(id);
        }
    }

    private void applySerialChange(Connection connection, int id) throws SQLException {
        Statement statement = connection.createStatement();
        statement.execute("CALL get_serial_by_id(" + id + ");");
        ResultSet result = statement.getResultSet();
        if (result.next()) {
            String name = result.getString("name");
            String offSite = result.getString("official_site");
            double mark = result.getDouble("mark");
            serials.addOrUpdate(id, name, offSite, mark);
        }
        else {
            serials.remove(id);
        }
    }

    private void applySeasonChange(Connection connection, int id) throws SQLException {
        Statement statement = connection.createStatement();
        statement.execute("CALL get_season_by_id(" + id + ");");
        ResultSet result = statement.getResultSet();
        if (result.next()) {
            int idSerial = result.getInt("id_serial");
            int number = result.getInt("number");
            int seriesCount = result.getInt("series_count");
            String torrent = result.getString("torrent_link");
            seasons.addOrUpdate(id, idSerial, number, seriesCount, torrent);
        }
        else {
            seasons.remove(id);
        }
    }

    private void applySeriesChange(Connection connection, int id) throws SQLException {
        Statement statement = connection.createStatement();
        statement.execute("CALL get_series_by_id(" + id + ");");
        ResultSet result = statement.getResultSet();
        if (result.next()) {
            int idSeason = result.getInt("id_season");
            int number = result.getInt("number");
            String name = result.getString("name");
            Date releaseDate = result.getDate("release_date");
            String torrent = result.getString("torrent_link");
            series.addOrUpdate(id, idSeason, number, name, releaseDate, torrent);
        }
        else {
            series.remove(id);
        }
    }

    private void applyStgChange(int idSerial, int idGenre, String type) {
        Serial serial = serials.getById(idSerial).orElse(null);
        if (serial == null) return;

        switch (type) {
            case "insert":
                genres.getById(idGenre).ifPresent(serial::addGenre);
                break;
            case "delete":
                serial.removeGenre(idGenre);
                break;
        }
    }

    // check users updates
    private void checkUsersUpdates() {
        Set<String> validNames = new HashSet<>();
        try (Connection connection = getConnection()) {
            Statement statement = connection.createStatement();
            statement.execute("CALL get_common_users();");
            ResultSet result = statement.getResultSet();
            while (result.next()) {
                String name = result.getString("user");
                Role role = null;
                switch (result.getString("role")) {
                    case "guest": role = Role.Guest; break;
                    case "editor": role = Role.Editor; break;
                    case "superuser": role = Role.Superuser; break;
                }
                if (role == null)
                    continue;

                validNames.add(name);
                users.addOrUpdate(name, role);
            }
            users.retainWithNames(validNames);
        }
        catch (SQLException e) {
            System.out.println("Error check users updates: " + e.getMessage());
        }
    }

    // observable
    private List<Genre> getSerialGenres(int idSerial, Connection connection) throws SQLException {
        Statement genresStatement = connection.createStatement();
        List<Genre> result = new ArrayList<>();
        if (genresStatement.execute("CALL get_genres_id_for(" + idSerial + ");")) {
            ResultSet resultIds = genresStatement.getResultSet();
            while (resultIds.next()) {
                int idGenre = resultIds.getInt("id_genre");
                genres.getById(idGenre).ifPresent(result::add);
            }
        }
        return result;
    }

    public ObservableList<Genre> getGenres() {
        return genres.getGenresObservable();
    }

    public ObservableList<Serial> getSerials() {
        return serials.getSerialsObservable();
    }

    public ObservableList<Season> getSeasonsFor(int serialId) {
        return seasons.getBySerialId(serialId);
    }

    public ObservableList<Series> getSeriesFor(int seasonId) {
        return series.getBySeasonId(seasonId);
    }

    public ObservableList<User> getUsers() {
        return users.getUsersObservable();
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

            series.setNumber(number);
            series.setName(name);
            series.setReleaseDate(releaseDate);
            series.setTorrentLink(torrent);
            return true;
        }
        catch (SQLException e) {
            return false;
        }
    }

    public boolean update(Genre genre, String name) {
        try (Connection connection = getConnection()) {
            Statement statement = connection.createStatement();
            statement.execute("CALL update_genre(" +
                    genre.getId() + ", " +
                    "\"" + name + "\"" +
                    ");");

            genre.setName(name);
            return true;
        }
        catch (SQLException e) {
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

                serials.addOrUpdate(idSerial, name, officialSite, mark, genres);
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

    public boolean createSeason(Serial owner, int number, int seriesCount, String torrent) {
        try (Connection connection = getConnection()) {
            Statement statement = connection.createStatement();
            statement.execute("CALL create_season(" +
                    owner.getId() + ", " +
                    number + ", " +
                    seriesCount + ", " +
                    "\"" + torrent + "\"" +
                    ");");
            int id = 0;
            ResultSet result = statement.getResultSet();
            if (result.next()) id = result.getInt(1);

            seasons.addOrUpdate(id, owner.getId(), number, seriesCount, torrent);
            return true;
        }
        catch (SQLException e) {
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
            int id = 0;
            ResultSet result = statement.getResultSet();
            if (result.next()) id = result.getInt(1);

            series.addOrUpdate(id, owner.getId(), number, name, releaseDate, torrent);
            return true;
        }
        catch (SQLException e) {
            return false;
        }
    }

    public boolean createGenre(String name) {
        try (Connection connection = getConnection()) {
            Statement statement = connection.createStatement();
            statement.execute("CALL create_genre(" +
                    "\"" + name + "\"" +
                    ");");
            int id = 0;
            ResultSet result = statement.getResultSet();
            if (result.next()) id = result.getInt(1);

            genres.addOrUpdate(id, name);
            return true;
        }
        catch (SQLException e) {
            return false;
        }
    }

    public boolean delete(Serial serial) {
        try (Connection connection = getConnection()) {
            Statement statement = connection.createStatement();
            statement.execute("CALL delete_serial(" +
                    serial.getId() +
                    ");");

            serials.remove(serial.getId());
            return true;
        }
        catch (SQLException e) {
            return false;
        }
    }

    public boolean delete(Season season) {
        try (Connection connection = getConnection()) {
            Statement statement = connection.createStatement();
            statement.execute("CALL delete_season(" +
                    season.getId() +
                    ");");

            seasons.remove(season.getId());
            return true;
        }
        catch (SQLException e) {
            return false;
        }
    }

    public boolean delete(Series series) {
        try (Connection connection = getConnection()) {
            Statement statement = connection.createStatement();
            statement.execute("CALL delete_series(" +
                    series.getId() +
                    ");");

            this.series.remove(series.getId());
            return true;
        }
        catch (SQLException e) {
            return false;
        }
    }

    public boolean delete(Genre genre) {
        try (Connection connection = getConnection()) {
            Statement statement = connection.createStatement();
            statement.execute("CALL delete_genre(" +
                    genre.getId() +
                    ");");

            genres.remove(genre.getId());
            return true;
        }
        catch (SQLException e) {
            return false;
        }
    }

    // users
    public boolean updateUserPassword(User user, String password) {
        try (Connection connection = getConnection()) {
            Statement statement = connection.createStatement();
            statement.execute("CALL change_user_password(" +
                    "\"" + user.nameObservable().getValue() + "\", " +
                    "\"" + password + "\"" +
                    ");");
            return true;
        }
        catch (SQLException e) {
            return false;
        }
    }

    public boolean updateUserRole(User user, Role role) {
        try (Connection connection = getConnection()) {
            Statement statement = connection.createStatement();
            switch (role) {
                case Guest: statement.execute("CALL make_user_guest(" +
                        "\"" + user.nameObservable().getValue() + "\"" +
                        ");");
                    break;
                case Editor: statement.execute("CALL make_user_editor(" +
                        "\"" + user.nameObservable().getValue() + "\"" +
                        ");");
                    break;
                default:
                    return false;
            }

            user.setRole(role);
            return true;
        }
        catch (SQLException e) {
            return false;
        }
    }

    public boolean createUser(String name, String password, Role role) {
        try (Connection connection = getConnection()) {
            Statement statement = connection.createStatement();
            switch (role) {
                case Guest: statement.execute("CALL create_user_guest(" +
                        "\"" + name + "\", " +
                        "\"" + password + "\"" +
                        ");");
                    break;
                case Editor: statement.execute("CALL create_user_editor(" +
                        "\"" + name + "\", " +
                        "\"" + password + "\"" +
                        ");");
                    break;
                default:
                    return false;
            }

            users.addOrUpdate(name, role);
            return true;
        }
        catch (SQLException e) {
            return false;
        }
    }

    public boolean delete(User user) {
        try (Connection connection = getConnection()) {
            Statement statement = connection.createStatement();
            statement.execute("CALL delete_user(" +
                    "\"" + user.nameObservable().getValue() + "\"" +
                    ");");

            users.remove(user);
            return true;
        }
        catch (SQLException e) {
            return false;
        }
    }

}
