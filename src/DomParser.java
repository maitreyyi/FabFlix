import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;


public class DomParser {

    // Set up lists
    TreeMap<String, Movie> movies = new TreeMap<>(); // <id, Movie>
    List<Star> stars = new ArrayList<>();
    TreeMap<String, Cast> casts = new TreeMap<>(); // <id, Cast>
    List<String> genres = new ArrayList<>();

    // Set up inconsistency counters
    int movieDup = 0;
    int movieInconsistent = 0;
    int movieYear = 0;
    int movieNotFound = 0;
    int starDup = 0;
    int starNotFound = 0;

    TreeMap<String, String> movieHash = new TreeMap<String, String>();
    TreeMap<String, String> starHash = new TreeMap<String, String>();
    TreeMap<String, String> starIdHash = new TreeMap<String, String>();
    TreeMap<String, Integer> genreHash = new TreeMap<String, Integer>();


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
        printData();
        printInconsistencies();
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

            parseActor(element);
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
            parseMovie(element, dirName);
        }
    }

    /**
     * It takes an employee Element, reads the values in, creates
     * an Employee object for return
     */
    private void parseMovie(Element element, String director) {
        // for each <movies> element get text or int values of
        // title, id, year
        String id = getTextValue(element, "fid");
        String title = getTextValue(element, "t");

        // If title is invalid, print out inconsistency
        if (title == null) {
            System.out.println("MovieID \"" + id + "\" has invalid title");
            movieInconsistent++;
            return;
        }

        // If no id, print out inconsistency
        if (id == null || id.equals(" ")) {
            System.out.println("Movie \"" + title + "\" has no id");
            movieInconsistent++;
            return;
        }

        int year = getIntValue(element, "year");

        // If year is invalid, print out inconsistency
        if (year == -1) {
            System.out.println("MovieID \"" + id + "\" has invalid year. Setting default: 9999");
            year = 9999;
            movieYear++;
        }

        if (movies.containsKey(id)) {
            System.out.println("MovieId \"" + id + "\" is already used");
            movieInconsistent++;
            return;
        }

        if (movieHash.containsKey(title+director+year)) {
            System.out.println("Movie \"" + title + "\" is duplicate");
            movieDup++;
            return;
        }

        if (movieIsDupe(director, title, year)) {
            System.out.println("Movie \"" + title + "\" is duplicate");
            movieDup++;
            return;
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
            if (!genres.contains(g) && !genreHash.containsKey(g)) {
                genres.add(g);
            }
        }

        movies.put(id, movie);
    }

    private void parseActor(Element actor) {
        // Get actor name
        String name = getTextValue(actor, "stagename");
        if (name == null)
        {
            System.out.println("Actor has invalid name");
            return;
        }

        // Get date of birth
        int dob = getIntValue(actor, "dob");

        // create a new Star with values read from xml nodes
        if (starIsDupe(name, dob)) {
            System.out.println(name + " is a duplicate");
            starDup++;
            return;
        }
        String key = name;
        key += (dob == -1) ? (null) : (dob);
        if (starHash.containsKey(key)) {
            System.out.println(name + " is a duplicate");
            starDup++;
            return;
        }
        Star star = new Star(name, dob);
        stars.add(star);
    }

    private void parseCastInFilm(Element dirfilms) {
        // Make each movie element
        NodeList nodeList = dirfilms.getElementsByTagName("filmc");
        for (int i = 0; i < nodeList.getLength(); i++) {

            // get the movie element
            Element element = (Element) nodeList.item(i);

            // get the Cast object
            parseCast(element);
        }

    }

    private void parseCast(Element element) {
        // Separate actors
        NodeList nodeList = element.getElementsByTagName("m");

        // Get title, if title not exist, then return
        Element firstElem = (Element) nodeList.item(0);
        String title = getTextValue(firstElem, "t");
        if (title == null) {
            return;
        }
        String id = getTextValue((Element)nodeList.item(0), "f");

        if (casts.containsKey(id)) {
            return;
        }

        // Create new cast item
        Cast cast = new Cast(id);

        // Add star
        for (int i = 0; i < nodeList.getLength(); i++) {
            String movieId = getTextValue((Element)nodeList.item(i), "f");
            if (!movies.containsKey(movieId)) {
                System.out.println("MovieID \"" + movieId + "\" does not exist");
                movieNotFound++;
                return;
            }
            String star = getTextValue((Element)nodeList.item(i), "a");
            if (star != null && !findStar(star) && !starIdHash.containsKey(star)) {
                System.out.println("Star " + star + " does not exist");
                starNotFound++;
                continue;
            }
            if (star != null) {
                cast.addStar(star);
            }
        }

        // add it to list if movie is properly made
        casts.put(id, cast);
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

    private boolean movieIsDupe(String director, String title, int year) {
        for (Movie m : movies.values()) {
            if (m.isEqual(title, director, year))
                return true;
        }
        return false;

    }

    private boolean starIsDupe(String name, int year) {
        for (Star s : stars) {
            if (s.isEqual(name, year))
                return true;
        }
        return false;
    }

    private boolean findStar(String name) {
        for (Star s : stars) {
            if (s.getName().equals(name))
                return true;
        }
        return false;
    }

    /**
     * Iterate through the list and print the
     * content to console
     */
    private void printData() {
        System.out.println("Total parsed " + movies.size() + " movies");
        System.out.println("Total parsed " + stars.size() + " stars");
        System.out.println("Total parsed " + casts.size() + " casts");
        System.out.println("Total parsed " + genres.size() + " genres");
    }

    private void printInconsistencies() {

        System.out.println(movieDup + " duplicate movies");
        System.out.println(movieInconsistent + " inconsistent movies");
        System.out.println(movieYear + " invalid movie years");
        System.out.println(movieNotFound + " movies not found");
        System.out.println(starDup + " duplicate stars");
        System.out.println(starNotFound + " stars not found in casts");
    }

    private void setMovieMap(String jdbcURL) {
        try {
            // Create connection
            Connection conn = DriverManager.getConnection(jdbcURL,"mytestuser", "My6$Password");
            String query = "SELECT * FROM movies";
            PreparedStatement statement = conn.prepareStatement(query);
            ResultSet rs = statement.executeQuery();
            int i = 0;

            while (rs.next()) {
                movieHash.put(rs.getString("title")+rs.getString("director")+rs.getInt("year"), rs.getString("id"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setStarMap(String jdbcURL) {
        try {
            // Create connection
            Connection conn = DriverManager.getConnection(jdbcURL,"mytestuser", "My6$Password");
            String query = "SELECT * FROM stars";
            PreparedStatement statement = conn.prepareStatement(query);
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                starHash.put(rs.getString("name")+rs.getString("birthyear"), rs.getString("id"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setStarIdMap(String jdbcURL) {
        try {
            // Create connection
            Connection conn = DriverManager.getConnection(jdbcURL,"mytestuser", "My6$Password");
            String query = "SELECT name, id FROM stars";
            PreparedStatement statement = conn.prepareStatement(query);
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                starIdHash.put(rs.getString("name"), rs.getString("id"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setGenreMap(String jdbcURL) {
        try {
            // Create connection
            Connection conn = DriverManager.getConnection(jdbcURL,"mytestuser", "My6$Password");
            String query = "SELECT * FROM genres";
            PreparedStatement statement = conn.prepareStatement(query);
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                genreHash.put(rs.getString("name"), rs.getInt("id"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
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
            String movie_query = "INSERT IGNORE INTO movies (id, title, year, director) " +
                    "VALUES (?, ?, ?, ?) ";

            String rating_query = "INSERT INTO ratings (movieId, rating, numVotes) " +
                    "VALUES (?, ?, ?) ";

            String new_genre_query = "INSERT INTO genres (name) " +
                    "SELECT ? ";

            String get_genre_query = "SELECT id FROM genres WHERE name = ?";

            String genre_in_movie_query = "INSERT IGNORE INTO genres_in_movies (genreId, movieId) " +
                    "SELECT ?, ? ";

            String get_max_string_id = "SELECT MAX(id) as id FROM stars";

            String star_query = "INSERT INTO stars (id, name, birthyear) " +
                    "VALUES (?, ?, ?) ";

            String star_no_year_query = "INSERT INTO stars (id, name) " +
                    "VALUES (?, ?) ";

            String get_star_query = "SELECT id FROM stars WHERE name = ? LIMIT 1";

            String stars_in_movie_query = "INSERT IGNORE INTO stars_in_movies (starId, movieId) " +
                    "SELECT ?, ? ";

            // Create genre statement
            PreparedStatement genreStatement = conn.prepareStatement(new_genre_query);
            conn.setAutoCommit(false);

            System.out.println("Parsing genres");

            // Add genres to batch
            for (String g : genres) {
                genreStatement.setString(1, g);
                genreStatement.addBatch();
            }
            // Execute genre insert
            genreStatement.executeBatch();
            conn.commit();
            genreStatement.close();

            System.out.println("Parsing movies");

            // Create genre_in_movie statements
            int insertCount = 0;
            PreparedStatement insertBatch = conn.prepareStatement(genre_in_movie_query);
            genreStatement = conn.prepareStatement(get_genre_query);

            for (Movie m : movies.values()) {
                // Insert movie
                PreparedStatement statement = conn.prepareStatement(movie_query);
                PreparedStatement ratingS = conn.prepareStatement(rating_query);

                if (m.getId() != null) {
                    statement.setString(1, m.getId());
                    statement.setString(2, m.getTitle());
                    statement.setInt(3, m.getYear());
                    statement.setString(4, m.getDirector());


                    // Perform the query
                    if (statement.executeUpdate() > 0) {
                        //if movie was inserted, insert rating
                        ratingS.setString(1,m.getId());
                        ratingS.setFloat(2,0);
                        ratingS.setInt(3,0);
                        ratingS.addBatch();
                        insertCount++;

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
                                insertBatch.addBatch();
                                insertCount++;

                                // If at 2000, run transaction
                                if (insertCount >= 2000) {
                                    insertBatch.executeBatch();
                                    ratingS.executeBatch();
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
                    ratingS.executeBatch();
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
                    starsYearBatch.addBatch();
                } else {
                    starsNoYearBatch.setString(1, starid);
                    starsNoYearBatch.setString(2, s.getName());
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

            setStarIdMap(jdbcURL);

            System.out.println("Doing cast");
            // Create stars_in_movies statemetns
            PreparedStatement getStarStatement = conn.prepareStatement(get_star_query);
            PreparedStatement starInMovieStatement = conn.prepareStatement(stars_in_movie_query);

            for (Cast c : casts.values()) {
                for (String s : c.getStars()) {
                    // Get starId
                    starid = starIdHash.get(s);

                    // Add star_in_movie to batch
                    starInMovieStatement.setString(1, starid);
                    starInMovieStatement.setString(2, c.getId());
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
            // If remaining, run transaction
            if (insertCount > 0) {
                starInMovieStatement.executeBatch();
                conn.commit();
            }
            statement.close();
            getStarStatement.close();
            starInMovieStatement.close();
            rs.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

//    private void

    public static void main(String[] args)  throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        // Set db connection
        Class.forName("com.mysql.jdbc.Driver").newInstance();
        String jdbcURL="jdbc:mysql://localhost:3306/moviedb";

        // create an instance
        DomParser domParserExample = new DomParser();

        // Set up hash
        domParserExample.setMovieMap(jdbcURL);
        domParserExample.setStarMap(jdbcURL);
        domParserExample.setStarIdMap(jdbcURL);
        domParserExample.setGenreMap(jdbcURL);

        // call run example
        domParserExample.runParser();

        // Insert values
        domParserExample.insertValues(jdbcURL);
    }
}
