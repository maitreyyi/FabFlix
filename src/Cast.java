import java.util.ArrayList;
import java.util.List;

public class Cast {

    private final String id;

    public List<String> stars;


    public Cast(String id) {
        this.id = id;
        this.stars = new ArrayList<>();
    }

    public String getId() {
        return this.id;
    }

    public void addStar(String star) {
        this.stars.add(star);
    }

    public List<String> getStars() {
        return this.stars;
    }

    public String printCast() {
        String str = "Id: " + getId();

        str += ", stars: [";

        for (String s : getStars())
        {
            str += s +", ";
        }
        str += "]";

        return str;
    }

}
