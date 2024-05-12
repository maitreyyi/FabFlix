import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;



public class Movie {

    private static final Map<String, String> catCodes = new TreeMap<>();
    static {
        catCodes.put("Ctxx", "Uncategorized");
        catCodes.put("Actn", "Action");
        catCodes.put("Camp", "Camp");
        catCodes.put("Comd", "Comedy");
        catCodes.put("Disa", "Disaster");
        catCodes.put("Epic", "Epic");
        catCodes.put("Horr", "Horror");
        catCodes.put("Noir", "Black");
        catCodes.put("ScFi", "Sci-Fi");
        catCodes.put("West", "Western");
        catCodes.put("Advt", "Adventure");
        catCodes.put("Cart", "Animation");
        catCodes.put("Docu", "Documentary");
        catCodes.put("Musc", "Musical");
        catCodes.put("Faml", "Family");
        catCodes.put("Porn", "Adult");
        catCodes.put("Surl", "Sureal");
        catCodes.put("AvGa", "Avant Garde");
        catCodes.put("CnR", "Cops and Robbers");
        catCodes.put("Dram", "Drama");
        catCodes.put("Hist", "History");
        catCodes.put("Myst", "Mystery");
        catCodes.put("Romt", "Romance");
        catCodes.put("Susp", "Thriller");
        catCodes.put("NoGenre", "NoGenre");
    }

    private final String title;

    private final String id;

    private final int year;

    private final String director;

    public List<String> genres;


    public Movie(String title, String id, int year, String director) {
        this.title = title;
        this.id = id;
        this.year = year;
        this.director = director;
        this.genres = new ArrayList<>();
    }


    public String getId() {
        return this.id;
    }

    public int getYear() {
        return this.year;
    }

    public String getTitle() {
        return this.title;
    }

    public String getDirector() {
        return this.director;
    }

    public void addGenre(String genre) {
        for (String g : genre.split(" ")) {
            String g_name = catCodes.get(g);
            if (g_name != null)
                this.genres.add(g_name);
        }
    }

    public List<String> getGenres() {
        return this.genres;
    }

    public boolean noGenres() {
        return this.genres.isEmpty();
    }

    public boolean isEqual(String title, String director, int year) {
        return this.title.equals(title) && this.director.equals(director) && this.year == year;
    }

    public String printMovie() {
        String str = "Id: " + getId() + ", title: " + getTitle() +
                     ", year: " + getYear() + ", director: " + getDirector();

        str += ", genres: [";

        for (String g : getGenres())
        {
            str += g +", ";
        }
        str += "]";

        return str;
    }

}
