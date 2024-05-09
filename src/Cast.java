import java.util.ArrayList;
import java.util.List;

public class Cast {

    private final String title;

    public List<String> stars;


    public Cast(String title) {
        this.title = title;
        this.stars = new ArrayList<>();
    }

    public String getTitle() {
        return this.title;
    }

    public void addStar(String star) {
        this.stars.add(star);
    }

    public List<String> getStars() {
        return this.stars;
    }

    public String printCast() {
        String str = "Title: " + getTitle();

        str += ", stars: [";

        for (String s : getStars())
        {
            str += s +", ";
        }
        str += "]";

        return str;
    }

}
