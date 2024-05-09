import java.util.ArrayList;
import java.util.List;

public class Movie {

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
        for (String g : genre.split(" "))
            this.genres.add(g);
    }

    public List<String> getGenres() {
        return this.genres;
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
