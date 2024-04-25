import java.util.Map;
import java.util.TreeMap;

/**
 * This User class only has the username field in this example.
 * You can add more attributes such as the user's shopping cart items.
 */
public class User {

    private final String username;
    Map<String, Integer> cart = new TreeMap<>();

    public User(String username) {
        this.username = username;
    }

}
