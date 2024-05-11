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

        if (movie.noGenres()) {
            movie.addGenre("NoGenre");
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
        NodeList nodeList = element.getElementsByTagName("m");

        Element firstElem = (Element) nodeList.item(0);
        String title = getTextValue(firstElem, "t");
        if (title == null) {
            return null;
        }

        Cast cast = new Cast(title);

        for (int i = 0; i < nodeList.getLength(); i++) {
            String star = getTextValue(firstElem, "a");
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
        int number = Integer.parseInt(id.split("m")[1]);
        number += 1;
        String zeroed_str = String.format("%07d", number);
        return "nm" + zeroed_str;
    }

    private void insertValues(String jdbcURL) {

        try {
            Connection conn = DriverManager.getConnection(jdbcURL,"mytestuser", "My6$Password");

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

            for (Movie m : movies) {
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
                        for (String g : m.getGenres()) {
                            PreparedStatement genre_statement = conn.prepareStatement(new_genre_query);
                            genre_statement.setString(1, g);
                            genre_statement.setString(2, g);
                            genre_statement.executeUpdate();

                            genre_statement = conn.prepareStatement(get_genre_query);
                            genre_statement.setString(1, g);
                            ResultSet rs = genre_statement.executeQuery();
                            rs.next();
                            int genreId = rs.getInt("id");

                            genre_statement = conn.prepareStatement(genre_in_movie_query);
                            genre_statement.setInt(1, genreId);
                            genre_statement.setString(2, m.getId());
                            genre_statement.setInt(3, genreId);
                            genre_statement.setString(4, m.getId());

                            rs.close();
                            genre_statement.close();
                        }
                    }
                }
                statement.close();

            }

            for (Star s : stars) {
                PreparedStatement statement = conn.prepareStatement(get_max_string_id);
                ResultSet rs = statement.executeQuery();
                rs.next();
                String starid = id_increment(rs.getString("id"));

                if (s.getDob() != -1) {
                    statement = conn.prepareStatement(star_query);
                    statement.setInt(3, s.getDob());
                    statement.setString(4, s.getName());
                    statement.setInt(5, s.getDob());
                } else {
                    statement = conn.prepareStatement(star_no_year_query);
                    statement.setString(3, s.getName());
                }
                statement.setString(1, starid);
                statement.setString(2, s.getName());
                statement.executeUpdate();

                statement.close();
                rs.close();
            }

            for (Cast c : casts) {
                PreparedStatement statement = conn.prepareStatement(get_movie_query);
                statement.setString(1, c.getTitle());

                ResultSet rs = statement.executeQuery();
                if (rs.next()) {
                    String movieid = rs.getString("id");

                    for (String s : c.getStars()) {
                        statement = conn.prepareStatement(get_star_query);
                        statement.setString(1, s);

                        rs = statement.executeQuery();
                        if (rs.next()) {
                            String starid = id_increment(rs.getString("id"));

                            statement = conn.prepareStatement(stars_in_movie_query);
                            statement.setString(1, starid);
                            statement.setString(2, movieid);
                            statement.setString(3, starid);
                            statement.setString(4, movieid);
                            statement.executeUpdate();
                        }
                    }
                }
                statement.close();
                rs.close();
            }

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
