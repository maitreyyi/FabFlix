import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class DomParser {

    List<Movie> movies = new ArrayList<>();
    List<Star> stars = new ArrayList<>();
    List<Cast> casts = new ArrayList<>();
    List<String> genres = new ArrayList<>();
    Document dom;

    public void runParser() {

        // parse the xml file and get the dom object
        parseFile("mains243.xml");

        // get each movie element and create a Movie object
        parseMovieDocument();

        parseFile("actors63.xml");
        parseActorDocument();

        parseFile("casts124.xml");
        parseCastDocument();

        // iterate through the list and print the data
        printMovieData();
        printStarsData();
        printCastData();
        System.out.println(genres.size());
    }

    private void parseFile(String uri) {
        // get the factory
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

        try {

            // using factory get an instance of document builder
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

            // parse using builder to get DOM representation of the XML file
            dom = documentBuilder.parse(uri);

        } catch (ParserConfigurationException | SAXException | IOException error) {
            error.printStackTrace();
        }
    }

    private void parseMovieDocument() {
        // get the document root Element
        Element documentElement = dom.getDocumentElement();

        // get a nodelist of movie Elements, parse each into Movie object
        NodeList nodeList = documentElement.getElementsByTagName("directorfilms");
        for (int i = 0; i < nodeList.getLength(); i++) {

            // get the movie element
            Element element = (Element) nodeList.item(i);

            parseDirectorFilms(element);
        }
    }

    private void parseActorDocument() {
        // get the document root Element
        Element documentElement = dom.getDocumentElement();

        // get a nodelist of movie Elements, parse each into Movie object
        NodeList nodeList = documentElement.getElementsByTagName("actor");
        for (int i = 0; i < nodeList.getLength(); i++) {

            // get the movie element
            Element element = (Element) nodeList.item(i);

            Star star = parseActor(element);
            if (star != null) {
                stars.add(star);
            }
        }
    }

    private void parseCastDocument() {
        // get the document root Element
        Element documentElement = dom.getDocumentElement();

        // get a nodelist of movie Elements, parse each into Movie object
        NodeList nodeList = documentElement.getElementsByTagName("dirfilms");
        for (int i = 0; i < nodeList.getLength(); i++) {

            // get the movie element
            Element element = (Element) nodeList.item(i);

            parseCastInFilm(element);
        }
    }

    private void parseDirectorFilms(Element dirfilms) {
        // Get director id
        String dirName = getTextValue(dirfilms, "dirname");
        if (dirName == null) {
            dirName = "Unknown";
        }

        // Make each movie element
        NodeList nodeList = dirfilms.getElementsByTagName("film");
        for (int i = 0; i < nodeList.getLength(); i++) {

            // get the movie element
            Element element = (Element) nodeList.item(i);

            // get the Movie object
            Movie movie = parseMovie(element, dirName);

            // add it to list if movie is properly made
            if (movie != null) {
                movies.add(movie);
            }
        }
    }

    /**
     * It takes an employee Element, reads the values in, creates
     * an Employee object for return
     */
    private Movie parseMovie(Element element, String director) {
        // for each <movies> element get text or int values of
        // title, id, year
        String id = getTextValue(element, "fid");
        String title = getTextValue(element, "t");

        // If title is invalid, print out inconsistency
        if (title == null) {
            System.out.println("MovieID \"" + id + "\" has invalid title");
            return null;
        }

        // If no id, print out inconsistency
        if (id == null || id.equals(" ")) {
            System.out.println("Movie \"" + title + "\" has no id");
            return null;
        }

        int year = getIntValue(element, "year");

        // If year is invalid, print out inconsistency
        if (year == -1) {
            System.out.println("MovieID \"" + id + "\" has invalid year. Setting default: 9999");
            year = 9999;
        }

        Movie movie = new Movie(title, id, year, director);

        NodeList nodeList = element.getElementsByTagName("cats");
        for (int i = 0; i < nodeList.getLength(); i++) {

            // get the cat element
            String genre = getTextValue(element, "cat");
            if (genre != null) {
                movie.addGenre(genre);
            }
        }

        // If there are no genres, add NoGenre type
        if (movie.noGenres()) {
            movie.addGenre("NoGenre");
        }

        // Add any new genres to genre list
        for (String g : movie.getGenres()) {
            if (!genres.contains(g)) {
                genres.add(g);
            }
        }

        // create a new Movie with the value read from the xml nodes
        return movie;
    }

    private Star parseActor(Element actor) {
        // Get actor name
        String name = getTextValue(actor, "stagename");
        if (name == null)
        {
            System.out.println("Actor has invalid name");
            return null;
        }

        // Get date of birth
        int dob = getIntValue(actor, "dob");

        // create a new Star with values read from xml nodes
        return new Star(name, dob);
    }

    private void parseCastInFilm(Element dirfilms) {
        // Make each movie element
        NodeList nodeList = dirfilms.getElementsByTagName("filmc");
        for (int i = 0; i < nodeList.getLength(); i++) {

            // get the movie element
            Element element = (Element) nodeList.item(i);

            // get the Cast object
            Cast cast = parseCast(element);

            // add it to list if movie is properly made
            if (cast != null) {
                casts.add(cast);
            }
        }

    }

    private Cast parseCast(Element element) {
        // Separate actors
        NodeList nodeList = element.getElementsByTagName("m");

        // Get title, if title not exist, then return
        Element firstElem = (Element) nodeList.item(0);
        String title = getTextValue(firstElem, "t");
        if (title == null) {
            return null;
        }

        // Create new cast item
        Cast cast = new Cast(title);

        // Add star
        for (int i = 0; i < nodeList.getLength(); i++) {
            String star = getTextValue((Element)nodeList.item(i), "a");
            if (star != null) {
                cast.addStar(star);
            }
        }

        return cast;
    }

    /**
     * It takes an XML element and the tag name, look for the tag and get
     * the text content
     * i.e. for <Employee><Name>John</Name></Employee> xml snippet if
     * the Element points to employee node and tagName is name it will return John
     */
    private String getTextValue(Element element, String tagName) {
        String textVal = null;
        NodeList nodeList = element.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0) {
            // here we expect only one <Name> would present in the <Employee>
            try {
                textVal = nodeList.item(0).getFirstChild().getNodeValue();
            } catch (Exception e){
                return null;
            }

        }
        return textVal;
    }

    /**
     * Calls getTextValue and returns a int value
     */
    private int getIntValue(Element ele, String tagName) {
        // in production application you would catch the exception
        try {
            return Integer.parseInt(getTextValue(ele, tagName));
        } catch (Exception e){
            return -1;
        }
    }

    /**
     * Iterate through the list and print the
     * content to console
     */
    private void printMovieData() {

        System.out.println("Total parsed " + movies.size() + " movies");
    }
    private void printStarsData() {

        System.out.println("Total parsed " + stars.size() + " stars");
    }
    private void printCastData() {

        System.out.println("Total parsed " + casts.size() + " casts");
    }

    private String id_increment(String id) {
        // Increment the id
        int number = Integer.parseInt(id.split("m")[1]);
        number += 1;
        String zeroed_str = String.format("%07d", number);
        return "nm" + zeroed_str;
    }

    private void insertValues(String jdbcURL) {

        try {
            // Create connection
            Connection conn = DriverManager.getConnection(jdbcURL,"mytestuser", "My6$Password");

            // Set up queries
            String movie_query = "INSERT INTO movies (id, title, year, director) " +
                    "SELECT ?, ?, ?, ? " +
                    "FROM DUAL " +
                    "WHERE NOT EXISTS (SELECT 1 FROM movies WHERE title = ? OR id = ?)";

            String new_genre_query = "INSERT INTO genres (name) " +
                    "SELECT ? " +
                    "FROM DUAL " +
                    "WHERE NOT EXISTS (SELECT 1 FROM genres WHERE name = ?)";

            String get_genre_query = "SELECT id FROM genres WHERE name = ?";

            String genre_in_movie_query = "INSERT INTO genres_in_movies (genreId, movieId) " +
                    "SELECT ?, ? " +
                    "FROM DUAL " +
                    "WHERE NOT EXISTS (SELECT 1 FROM genres_in_movies WHERE genreId = ? AND movieId = ?)";

            String get_max_string_id = "SELECT MAX(id) as id FROM stars";

            String star_query = "INSERT INTO stars (id, name, birthyear) " +
                    "SELECT ?, ?, ? " +
                    "FROM DUAL " +
                    "WHERE NOT EXISTS (SELECT 1 FROM stars WHERE name = ? AND birthyear = ?)";

            String star_no_year_query = "INSERT INTO stars (id, name) " +
                    "SELECT ?, ? " +
                    "FROM DUAL " +
                    "WHERE NOT EXISTS (SELECT 1 FROM stars WHERE name = ? AND birthyear IS NULL)";

            String get_star_query = "SELECT id FROM stars WHERE name = ?";

            String stars_in_movie_query = "INSERT INTO stars_in_movies (starId, movieId) " +
                    "SELECT ?, ? " +
                    "FROM DUAL " +
                    "WHERE NOT EXISTS (SELECT 1 FROM stars_in_movies WHERE starId = ? AND movieId = ?)";

            String get_movie_query = "SELECT id FROM movies WHERE title = ?";

            // Create genre statement
            PreparedStatement genreStatement = conn.prepareStatement(new_genre_query);
            conn.setAutoCommit(false);

            // Add genres to batch
            for (String g : genres) {
                genreStatement.setString(1, g);
                genreStatement.setString(2, g);
                genreStatement.addBatch();
            }
            // Execute genre insert
            genreStatement.executeBatch();
            conn.commit();
            genreStatement.close();
            System.out.println("genres done");


            // Create genre_in_movie statements
            int insertCount = 0;
            PreparedStatement insertBatch = conn.prepareStatement(genre_in_movie_query);
            genreStatement = conn.prepareStatement(get_genre_query);

            for (Movie m : movies) {
                // Insert movie
                PreparedStatement statement = conn.prepareStatement(movie_query);
                if (m.getId() != null) {
                    statement.setString(1, m.getId());
                    statement.setString(2, m.getTitle());
                    statement.setInt(3, m.getYear());
                    statement.setString(4, m.getDirector());
                    statement.setString(5, m.getTitle());
                    statement.setString(6, m.getId());

                    // Perform the query
                    if (statement.executeUpdate() > 0) {
                        // If the movie was inserted, insert its genres
                        for (String g : m.getGenres()) {
                            genreStatement.setString(1, g);
                            ResultSet rs = genreStatement.executeQuery();
                            // Get genreId
                            if (rs.next()) {
                                int genreId = rs.getInt("id");

                                // Insert batch
                                insertBatch.setInt(1, genreId);
                                insertBatch.setString(2, m.getId());
                                insertBatch.setInt(3, genreId);
                                insertBatch.setString(4, m.getId());
                                insertBatch.addBatch();
                                insertCount++;

                                // If at 2000, run transaction
                                if (insertCount >= 2000) {
                                    insertBatch.executeBatch();
                                    conn.commit();
                                    insertCount = 0;
                                }

                            }
                            rs.close();
                        }
                    }
                }
                // If remaining, run transaction
                if (insertCount > 0) {
                    insertBatch.executeBatch();
                    conn.commit();
                    insertCount = 0;
                }

                statement.close();
            }
            genreStatement.close();
            insertBatch.close();

            System.out.println("Doing stars");
            // Create stars statements
            PreparedStatement starsYearBatch = conn.prepareStatement(star_query);
            PreparedStatement starsNoYearBatch = conn.prepareStatement(star_no_year_query);
            conn.setAutoCommit(false);
            insertCount = 0;

            // Get max starId
            String starid = "nm0000000";
            PreparedStatement statement = conn.prepareStatement(get_max_string_id);
            ResultSet rs = statement.executeQuery();
            conn.commit();

            rs.next();
            if (rs.getString("id") != null) {
                starid = rs.getString("id");
            }
            statement.close();
            rs.close();

            for (Star s : stars) {
                starid = id_increment(starid);

                // Check birthyear, use corresponding insert
                if (s.getDob() != -1) {
                    starsYearBatch.setString(1, starid);
                    starsYearBatch.setString(2, s.getName());
                    starsYearBatch.setInt(3, s.getDob());
                    starsYearBatch.setString(4, s.getName());
                    starsYearBatch.setInt(5, s.getDob());
                    starsYearBatch.addBatch();
                } else {
                    starsNoYearBatch.setString(1, starid);
                    starsNoYearBatch.setString(2, s.getName());
                    starsNoYearBatch.setString(3, s.getName());
                    starsNoYearBatch.addBatch();
                }
                insertCount++;

                // If at 2000, run transaction
                if (insertCount >= 2000) {
                    starsYearBatch.executeBatch();
                    starsNoYearBatch.executeBatch();
                    conn.commit();
                    insertCount = 0;
                }
            }
            // If remaining, run transaction
            if (insertCount > 0) {
                starsYearBatch.executeBatch();
                starsNoYearBatch.executeBatch();
                conn.commit();
                insertCount = 0;
            }
            starsYearBatch.close();
            starsNoYearBatch.close();

            System.out.println("Doing cast");
            // Create stars_in_movies statemetns
            PreparedStatement getMovieStatement = conn.prepareStatement(get_movie_query);
            PreparedStatement getStarStatement = conn.prepareStatement(get_star_query);
            PreparedStatement starInMovieStatement = conn.prepareStatement(stars_in_movie_query);

            for (Cast c : casts) {
                // Get movie title
                getMovieStatement.setString(1, c.getTitle());

                rs = getMovieStatement.executeQuery();
                if (rs.next()) {
                    // Get movieId
                    String movieid = rs.getString("id");

                    for (String s : c.getStars()) {
                        // Get star name
                        getStarStatement.setString(1, s);

                        rs = getStarStatement.executeQuery();
                        if (rs.next()) {
                            // Get starId
                            starid = rs.getString("id");

                            // Add star_in_movie to batch
                            starInMovieStatement.setString(1, starid);
                            starInMovieStatement.setString(2, movieid);
                            starInMovieStatement.setString(3, starid);
                            starInMovieStatement.setString(4, movieid);
                            starInMovieStatement.addBatch();
                            insertCount++;

                            // If at 2000, run transaction
                            if (insertCount >= 2000) {
                                starInMovieStatement.executeBatch();
                                conn.commit();
                                insertCount = 0;
                            }
                        }
                    }
                }
            }
            // If remaining, run transaction
            if (insertCount > 0) {
                starInMovieStatement.executeBatch();
                conn.commit();
            }
            statement.close();
            getMovieStatement.close();
            getStarStatement.close();
            starInMovieStatement.close();
            rs.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args)  throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        // create an instance
        DomParser domParserExample = new DomParser();

        // call run example
        domParserExample.runParser();

        Class.forName("com.mysql.jdbc.Driver").newInstance();
        String jdbcURL="jdbc:mysql://localhost:3306/testmoviedb";

        // Insert values
        domParserExample.insertValues(jdbcURL);
    }
}
