package sample;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataRestorer {

    public static boolean restoreGenres(Connection connection, File file) {
        try (BufferedReader reader = Files.newBufferedReader(file.toPath())) {
            Pattern pattern = Pattern.compile("([\\d]+), \"(.*)\"");

            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO genre VALUES(?, ?);");
            while (reader.ready()) {
                String line = reader.readLine();
                Matcher matcher = pattern.matcher(line);
                if (matcher.matches()) {

                    statement.setInt(1, Integer.parseInt(matcher.group(1)));
                    statement.setString(2,
                            matcher.group(2).replace("\\\"", "\""));
                    statement.addBatch();
                }
                else {
                    System.out.println("Wrong pattern genres in line \"" + line + "\"");
                }
            }
            statement.executeBatch();
            statement.close();
            return true;
        }
        catch (IOException|SQLException e) {
            return false;
        }
    }

    public static boolean restoreSerials(Connection connection, File file) {
        try (BufferedReader reader = Files.newBufferedReader(file.toPath())) {
            Pattern pattern = Pattern.compile("([\\d]+), \"(.*)\", \"(.*)\", ([\\d.\\-]+)");

            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO serial VALUES(?, ?, ?, ?);");
            while (reader.ready()) {
                String line = reader.readLine();
                Matcher matcher = pattern.matcher(line);
                if (!matcher.matches()) {
                    System.out.println("Wrong pattern serials in line \"" + line + "\"");
                    continue;
                }
                double mark;
                try {
                    mark = Double.parseDouble(matcher.group(4));
                }
                catch (NumberFormatException e) {
                    System.out.println("Wrong mark in line \"" + line + "\"");
                    continue;
                }


                statement.setInt(1, Integer.parseInt(matcher.group(1)));
                statement.setString(2,
                        matcher.group(2).replace("\\\"", "\""));
                statement.setString(3,
                        matcher.group(3).replace("\\\"", "\""));
                statement.setDouble(4, mark);
                statement.addBatch();
            }
            statement.executeBatch();
            statement.close();
            return true;
        }
        catch (IOException|SQLException e) {
            return false;
        }
    }

    public static boolean restoreSeasons(Connection connection, File file) {
        try (BufferedReader reader = Files.newBufferedReader(file.toPath())) {
            Pattern pattern = Pattern.compile("([\\d]+), ([\\d]+), ([\\d]+), ([\\d]+), \"(.*)\"");

            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO season VALUES(?, ?, ?, ?, ?);");
            while (reader.ready()) {
                String line = reader.readLine();
                Matcher matcher = pattern.matcher(line);
                if (!matcher.matches()) {
                    System.out.println("Wrong pattern seasons in line \"" + line + "\"");
                    continue;
                }

                statement.setInt(1, Integer.parseInt(matcher.group(1)));
                statement.setInt(2, Integer.parseInt(matcher.group(2)));
                statement.setInt(3, Integer.parseInt(matcher.group(3)));
                statement.setInt(4, Integer.parseInt(matcher.group(4)));
                statement.setString(5,
                        matcher.group(5).replace("\\\"", "\""));
                statement.addBatch();
            }
            statement.executeBatch();
            statement.close();
            return true;
        }
        catch (IOException|SQLException e) {
            return false;
        }
    }

    public static boolean restoreSeries(Connection connection, File file) {
        try (BufferedReader reader = Files.newBufferedReader(file.toPath())) {
            Pattern pattern = Pattern.compile("([\\d]+), ([\\d]+), ([\\d]+), \"(.*)\", \"(.*)\", \"(.*)\"");

            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO series VALUES(?, ?, ?, ?, ?, ?);");
            while (reader.ready()) {
                String line = reader.readLine();
                Matcher matcher = pattern.matcher(line);
                if (!matcher.matches()) {
                    System.out.println("Wrong pattern series in line \"" + line + "\"");
                    continue;
                }

                statement.setInt(1, Integer.parseInt(matcher.group(1)));
                statement.setInt(2, Integer.parseInt(matcher.group(2)));
                statement.setInt(3, Integer.parseInt(matcher.group(3)));
                statement.setString(4,
                        matcher.group(4).replace("\\\"", "\""));
                statement.setString(5, matcher.group(5));
                statement.setString(6,
                        matcher.group(6).replace("\\\"", "\""));
                statement.addBatch();
            }
            statement.executeBatch();
            statement.close();
            return true;
        }
        catch (IOException|SQLException e) {
            return false;
        }
    }

    public static boolean restoreSTG(Connection connection, File file) {
        try (BufferedReader reader = Files.newBufferedReader(file.toPath())) {
            Pattern pattern = Pattern.compile("([\\d]+), ([\\d]+)");

            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO serial_to_genre VALUES(?, ?);");
            while (reader.ready()) {
                String line = reader.readLine();
                Matcher matcher = pattern.matcher(line);
                if (!matcher.matches()) {
                    System.out.println("Wrong pattern Stg in line \"" + line + "\"");
                    continue;
                }

                statement.setInt(1, Integer.parseInt(matcher.group(1)));
                statement.setInt(2, Integer.parseInt(matcher.group(2)));
                statement.addBatch();
            }
            statement.executeBatch();
            statement.close();
            return true;
        }
        catch (IOException|SQLException e) {
            return false;
        }
    }


}
