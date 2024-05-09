import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DomParser {

    List<Movie> movies = new ArrayList<>();
    List<Star> stars = new ArrayList<>();
    Document dom;

    public void runParser() {

        // parse the xml file and get the dom object
        parseMovieFile();

        // get each movie element and create a Movie object
        parseDocument();

        // iterate through the list and print the data
        printMovieData();

    }

    private void parseMovieFile() {
        // get the factory
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

        try {

            // using factory get an instance of document builder
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

            // parse using builder to get DOM representation of the XML file
            dom = documentBuilder.parse("mains243.xml");

        } catch (ParserConfigurationException | SAXException | IOException error) {
            error.printStackTrace();
        }
    }

    private void parseDocument() {
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

    private void parseDirectorFilms(Element dirfilms) {
        // Get director id
        String dirName = getTextValue(dirfilms, "dirname");

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
            System.out.println("MovieID \"" + id + "\" has invalid year");
            return null;
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

        // create a new Movie with the value read from the xml nodes
        return new Movie(title, id, year, director);
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

    public static void main(String[] args) {
        // create an instance
        DomParser domParserExample = new DomParser();

        // call run example
        domParserExample.runParser();
    }
}
