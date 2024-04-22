package ru.yandex.practicum.filmorate.storage;

public class DbConstants {
    public static final String SQL_INSERT_FILM = "INSERT INTO \"films\" "
            + "(\"title\", \"description\", \"release_date\", \"duration\", \"rating_id\") "
            + "VALUES(?, ?, ?, ?, ?)";
    public static final String SQL_INSERT_FILM_GENRES = "INSERT INTO \"film_genres\" (\"film_id\", \"genre_id\") VALUES (?, ?)";
    public static final String SQL_UPDATE_FILM = "update \"films\" set "
            + "\"title\" = ?, "
            + "\"description\" = ?, "
            + "\"release_date\" = ?, "
            + "\"duration\" = ? ,"
            + "\"rating_id\" = ? "
            + "where \"id\" = ?";
    public static final String SQL_SELECT_ALL_FILMS = "select "
            + "\"id\", "
            + "\"title\", "
            + "\"description\", "
            + "\"release_date\", "
            + "\"duration\", "
            + "\"rating_id\" "
            + "from \"films\"";
    public static final String SQL_SELECT_FILM_BY_ID = "select "
            + "\"id\", "
            + "\"title\", "
            + "\"description\", "
            + "\"release_date\", "
            + "\"duration\", "
            + "\"rating_id\" "
            + "from \"films\" where \"id\" = ?";
    public static final String SQL_DELETE_FILM_BY_ID = "delete from \"films\" where \"id\" = ?";
    public static final String SQL_SELECT_GENRE_BY_ID = "SELECT \"id\", \"name\" FROM \"genres\" WHERE \"id\" = ?";
    public static final String SQL_SELECT_RATING_BY_ID = "SELECT \"id\", \"rating_title\" " +
            "FROM \"ratings\" WHERE \"id\" = ?";
    public static final String SQL_SELECT_ALL_RATINGS = "select \"id\", \"rating_title\" from \"ratings\"";
    public static final String SQL_SELECT_ALL_GENRES = "select \"id\", \"name\" from \"genres\"";
    public static final String SQL_INSERT_LIKE = "INSERT INTO \"likes\" (\"film_id\", \"user_id\", \"created_at\") "
            + "VALUES (?, ?, CURRENT_TIMESTAMP())\n";
    public static final String SQL_DELETE_LIKE = "DELETE FROM \"likes\" WHERE \"film_id\" = ? AND \"user_id\" = ?";
    public static final String SQL_SELECT_LIKES_BY_FILM_ID = "select \"user_id\" "
            + "from \"likes\" "
            + "where \"film_id\" = ?";
    public static final String SQL_SELECT_FILM_GENRES_BY_FILM_ID = "SELECT g.\"id\", g.\"name\" \n"
            + "FROM \"film_genres\" f \n" + "LEFT JOIN \"genres\" g  \n"
            + "ON f.\"genre_id\" = g.\"id\"\n"
            + "WHERE f.\"film_id\" = ?\n"
            + "ORDER BY f.\"id\" ASC ";
    public static final String SQL_DELETE_FILM_GENRES_BY_FILM_ID = "DELETE FROM \"film_genres\" WHERE \"film_id\" = ?";

    public static final String SQL_INSERT_USER = "INSERT INTO \"users\" (\"email\", \"login\", \"name\", \"birthday\") " +
            "VALUES(?, ?, ?, ?);";
    public static final String SQL_UPDATE_USER = "update \"users\" set \"email\" = ?," +
            "\"login\" = ?," +
            "\"name\" = ?," +
            "\"birthday\" = ?" +
            "where \"id\" = ?";
    public static final String SQL_SELECT_ALL_USERS = "select " +
            "\"id\"," +
            "\"email\"," +
            "\"login\"," +
            "\"name\"," +
            "\"birthday\"" +
            " from \"users\"";
    public static final String SQL_SELECT_USER_BY_ID = "select " +
            "\"id\"," +
            "\"email\"," +
            "\"login\"," +
            "\"name\"," +
            "\"birthday\"" +
            " from \"users\" where \"id\" = ?";
    public static final String SQL_INSERT_FRIEND = "INSERT INTO \"friends\" "
            + "(\"user_id\", \"friend_id\", \"request_status\", \"created_at\")"
            + " VALUES (?, ?, false, CURRENT_TIMESTAMP());";
    public static final String SQL_DELETE_FRIEND = "DELETE FROM \"friends\" WHERE \"user_id\" = ? AND \"friend_id\" = ?";
    public static final String SQL_APPROVE_FRIEND = "UPDATE \"friends\" SET \"request_status\" = TRUE " +
            "WHERE \"user_id\" = ? AND \"friend_id\" = ?";
    public static final String SQL_SELECT_USER_FRIENDS_IDS = "SELECT DISTINCT " +
            "CASE WHEN \"user_id\" = ? THEN \"friend_id\" ELSE \"user_id\" END AS \"friends_ids\"\n" +
            "FROM \"friends\" \n" +
            "WHERE \"user_id\" = ? OR (\"friend_id\" = ? AND \"request_status\" = true)";

    public static final String SQL_SELECT_USER_FRIENDS = "SELECT \"id\", \"email\", \"login\", \"name\", \"birthday\" " +
            "FROM \"users\" WHERE \"id\" IN (\n" +
            "SELECT DISTINCT CASE WHEN \"user_id\" = ? THEN \"friend_id\" ELSE \"user_id\" END AS \"friends_ids\"\n" +
            "FROM \"friends\" WHERE \"user_id\" = ? OR (\"friend_id\" = ? AND \"request_status\" = true))";
}
