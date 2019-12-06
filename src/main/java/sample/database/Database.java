package sample.database;

import javafx.collections.ObservableList;
import sample.DataRestorer;
import sample.RunnableTask;
import sample.database.containers.*;
import sample.database.records.*;
import sample.exceptions.AuthException;
import sample.exceptions.ConnectTimeoutException;

import java.io.File;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.*;

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
    private Timer timer = null;
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

            startCheckUpdates();
        }
        catch (SQLTimeoutException e) {
            throw new ConnectTimeoutException();
        }
        catch (SQLException e) {
            throw new AuthException();
        }
    }

    public void startCheckUpdates() {
        if (timer != null) return;

        timer = new Timer(true);
        timer.schedule(new RunnableTask(this::checkUpdates), 0, 1000);
        if (role == Role.Superuser)
            timer.schedule(new RunnableTask(this::checkUsersUpdates), 0, 5000);
    }

    public void stopCheckUpdates() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    public Connection getConnection() throws SQLException {
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

    private List<Genre> getSerialGenres(int idSerial, Connection connection) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("CALL get_genres_id_for(?);");
        statement.setInt(1, idSerial);
        List<Genre> result = new ArrayList<>();
        statement.execute();
        ResultSet resultIds = statement.getResultSet();
        while (resultIds.next()) {
            int idGenre = resultIds.getInt("id_genre");
            genres.getById(idGenre).ifPresent(result::add);
        }
        return result;
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
        PreparedStatement statement = connection.prepareStatement("CALL get_changes_after(?);");
        statement.setInt(1, lastMainChangeId);
        statement.execute();
        ResultSet result = statement.getResultSet();
        while (result.next()) {
            lastMainChangeId = result.getInt("id");
            String tableName = result.getString("table_name");
            int id = result.getInt("id_row");
            applyMainChange(connection, tableName, id);
        }
    }

    private void checkStgUpdates(Connection connection) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("CALL get_stg_changes_after(?);");
        statement.setInt(1, lastStgChangeId);
        statement.execute();
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
        PreparedStatement statement = connection.prepareStatement("CALL get_genre_by_id(?);");
        statement.setInt(1, id);
        statement.execute();
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
        PreparedStatement statement = connection.prepareStatement("CALL get_serial_by_id(?);");
        statement.setInt(1, id);
        statement.execute();
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
        PreparedStatement statement = connection.prepareStatement("CALL get_season_by_id(?);");
        statement.setInt(1, id);
        statement.execute();
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
        PreparedStatement statement = connection.prepareStatement("CALL get_series_by_id(?);");
        statement.setInt(1, id);
        statement.execute();
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
    public ObservableList<Genre> getGenres() {
        return genres.getGenresObservable();
    }

    public ObservableList<Serial> getSerials() {
        return serials.getSerialsObservable();
    }

    public Collection<Season> getSeasons() {
        return seasons.getSeasons();
    }

    public ObservableList<Season> getSeasonsFor(int serialId) {
        return seasons.getBySerialId(serialId);
    }

    public Collection<Series> getSeries() {
        return series.getSeries();
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

            PreparedStatement updateStatement =
                    connection.prepareStatement("CALL update_serial(?, ?, ?, ?);");
            PreparedStatement clearGenresStatement =
                    connection.prepareStatement("CALL clear_genres(?);");
            PreparedStatement addGenreStatement =
                    connection.prepareStatement("CALL add_genre_to_serial(?, ?);");
            try {
                updateStatement.setInt(1, serial.getId());
                updateStatement.setString(2, name);
                updateStatement.setString(3, officialSite);
                updateStatement.setDouble(4, mark);
                updateStatement.execute();

                clearGenresStatement.setInt(1, serial.getId());
                clearGenresStatement.execute();
                for (Genre genre : genres) {
                    addGenreStatement.setInt(1, serial.getId());
                    addGenreStatement.setInt(2, genre.getId());
                    addGenreStatement.addBatch();
                }
                addGenreStatement.executeBatch();

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
            PreparedStatement statement =
                    connection.prepareStatement("CALL update_season(?, ?, ?, ?);");
            statement.setInt(1, season.getId());
            statement.setInt(2, number);
            statement.setInt(3, seriesCount);
            statement.setString(4, torrent);
            statement.execute();

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
            PreparedStatement statement =
                    connection.prepareStatement("CALL update_series(?, ?, ?, ?, ?);");
            statement.setInt(1, series.getId());
            statement.setInt(2, number);
            statement.setString(3, name);
            statement.setString(4, dateFormat.format(releaseDate));// TODO check
            statement.setString(5, torrent);
            statement.execute();

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
            PreparedStatement statement =
                    connection.prepareStatement("CALL update_genre(?, ?);");
            statement.setInt(1, genre.getId());
            statement.setString(2, name);
            statement.execute();

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

            PreparedStatement createStatement =
                    connection.prepareStatement("CALL create_serial(?, ?, ?);");
            PreparedStatement addGenreStatement =
                    connection.prepareStatement("CALL add_genre_to_serial(?, ?);");
            try {
                createStatement.setString(1, name);
                createStatement.setString(2, officialSite);
                createStatement.setDouble(3, mark);
                createStatement.execute();

                int idSerial = 0;
                ResultSet result = createStatement.getResultSet();
                while (result.next())
                    idSerial = result.getInt(1);

                for (Genre genre : genres) {
                    addGenreStatement.setInt(1, idSerial);
                    addGenreStatement.setInt(2, genre.getId());
                    addGenreStatement.addBatch();
                }
                addGenreStatement.executeBatch();

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
            PreparedStatement statement =
                    connection.prepareStatement("CALL create_season(?, ?, ?, ?);");
            statement.setInt(1, owner.getId());
            statement.setInt(2, number);
            statement.setInt(3, seriesCount);
            statement.setString(4, torrent);
            statement.execute();

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
            PreparedStatement statement =
                    connection.prepareStatement("CALL create_series(?, ?, ?, ?, ?);");
            statement.setInt(1, owner.getId());
            statement.setInt(2, number);
            statement.setString(3, name);
            statement.setString(4, dateFormat.format(releaseDate));
            statement.setString(5, torrent);
            statement.execute();

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
            PreparedStatement statement =
                    connection.prepareStatement("CALL create_genre(?);");
            statement.setString(1, name);
            statement.execute();

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
            PreparedStatement statement =
                    connection.prepareStatement("CALL delete_serial(?);");
            statement.setInt(1, serial.getId());
            statement.execute();

            serials.remove(serial.getId());
            return true;
        }
        catch (SQLException e) {
            return false;
        }
    }

    public boolean delete(Season season) {
        try (Connection connection = getConnection()) {
            PreparedStatement statement =
                    connection.prepareStatement("CALL delete_season(?);");
            statement.setInt(1, season.getId());
            statement.execute();

            seasons.remove(season.getId());
            return true;
        }
        catch (SQLException e) {
            return false;
        }
    }

    public boolean delete(Series series) {
        try (Connection connection = getConnection()) {
            PreparedStatement statement =
                    connection.prepareStatement("CALL delete_series(?);");
            statement.setInt(1, series.getId());
            statement.execute();

            this.series.remove(series.getId());
            return true;
        }
        catch (SQLException e) {
            return false;
        }
    }

    public boolean delete(Genre genre) {
        try (Connection connection = getConnection()) {
            PreparedStatement statement =
                    connection.prepareStatement("CALL delete_genre(?);");
            statement.setInt(1, genre.getId());
            statement.execute();

            genres.remove(genre.getId());
            return true;
        }
        catch (SQLException e) {
            return false;
        }
    }

    public boolean deleteAll() {
        try (Connection connection = getConnection()) {
            Statement statement = connection.createStatement();
            statement.addBatch("DELETE FROM serial_to_genre;");
            statement.addBatch("DELETE FROM series;");
            statement.addBatch("DELETE FROM season;");
            statement.addBatch("DELETE FROM serial;");
            statement.addBatch("DELETE FROM genre;");
            statement.executeBatch();
            statement.close();
            return true;
        }
        catch (SQLException e) {
            return false;
        }
    }

    // restore
    public boolean restoreGenres(File file) {
        try (Connection connection = getConnection()) {
            return DataRestorer.restoreGenres(connection, file);
        }
        catch (SQLException e) {
            return false;
        }
    }

    public boolean restoreSerials(File file) {
        try (Connection connection = getConnection()) {
            return DataRestorer.restoreSerials(connection, file);
        }
        catch (SQLException e) {
            return false;
        }
    }

    public boolean restoreSeasons(File file) {
        try (Connection connection = getConnection()) {
            return DataRestorer.restoreSeasons(connection, file);
        }
        catch (SQLException e) {
            return false;
        }
    }

    public boolean restoreSeries(File file) {
        try (Connection connection = getConnection()) {
            return DataRestorer.restoreSeries(connection, file);
        }
        catch (SQLException e) {
            return false;
        }
    }

    public boolean restoreSTG(File file) {
        try (Connection connection = getConnection()) {
            return DataRestorer.restoreSTG(connection, file);
        }
        catch (SQLException e) {
            return false;
        }
    }

    // users
    public boolean updateUserPassword(User user, String password) {
        try (Connection connection = getConnection()) {
            PreparedStatement statement =
                    connection.prepareStatement("CALL change_user_password(?, ?);");
            statement.setString(1, user.nameObservable().getValue());
            statement.setString(2, password);
            statement.execute();

            return true;
        }
        catch (SQLException e) {
            return false;
        }
    }

    public boolean updateUserRole(User user, Role role) {
        try (Connection connection = getConnection()) {
            PreparedStatement statement;
            switch (role) {
                case Guest:
                    statement = connection.prepareStatement("CALL make_user_guest(?);");
                    break;
                case Editor:
                    statement = connection.prepareStatement("CALL make_user_editor(?);");
                    break;
                default:
                    return false;
            }
            statement.setString(1, user.nameObservable().getValue());
            statement.execute();

            user.setRole(role);
            return true;
        }
        catch (SQLException e) {
            return false;
        }
    }

    public boolean createUser(String name, String password, Role role) {
        try (Connection connection = getConnection()) {
            PreparedStatement statement;
            switch (role) {
                case Guest:
                    statement = connection.prepareStatement("CALL create_user_guest(?, ?);");
                    break;
                case Editor:
                    statement = connection.prepareStatement("CALL create_user_editor(?, ?);");
                    break;
                default:
                    return false;
            }
            statement.setString(1, name);
            statement.setString(2, password);
            statement.execute();

            users.addOrUpdate(name, role);
            return true;
        }
        catch (SQLException e) {
            return false;
        }
    }

    public boolean delete(User user) {
        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement("CALL delete_user(?);");
            statement.setString(1, user.nameObservable().getValue());
            statement.execute();

            users.remove(user);
            return true;
        }
        catch (SQLException e) {
            return false;
        }
    }

}
