import java.util.Map;
import java.util.TreeMap;

/**
 * This User class only has the username field in this example.
 * You can add more attributes such as the user's shopping cart items.
 */
public class User {

    private final String username;
    public boolean employee;
    public boolean customer;
    public Map<String, Integer> cart = new TreeMap<>();
    public Map<String, String> parameters = new TreeMap<>();

    public User(String username) {
        this.username = username;
        this.customer = false;
        this.employee = false;
    }

    public void addParam(String key, String value) {
        this.parameters.put(key, value);
    }

    public Map<String, String> getParam() {
        return this.parameters;
    }

    public Map<String, Integer> getCart() {return this.cart;}

    public void addToCart(String movie_id) {
        //add item to cart if it doesn't exist, if it exists increment by one
        Integer quantity = this.cart.get(movie_id);
        if(quantity== null){
            this.cart.put(movie_id,1);
        } else {
            this.cart.put(movie_id, quantity+1);
        }
    }

    public void removeFromCart(String movie_id) {
        //add item to cart if it doesn't exist, if it exists increment by one
        Integer quantity = this.cart.get(movie_id);
        if (quantity != null) {
            if (quantity == 1) {
                this.cart.remove(movie_id);
            } else {
                this.cart.put(movie_id, quantity - 1);
            }
        }
    }
    public void deleteFromCart(String movie_id) {
        //add item to cart if it doesn't exist, if it exists increment by one
            this.cart.remove(movie_id);
    }
    public Map<String, Integer> clearCart() {
        this.cart.clear();
        return this.cart;
    }

    public void log_employee() {
        this.employee = true;
    }

    public boolean is_employee() {
        return this.employee;
    }

    public void log_customer() {
        this.customer = true;
    }
    public boolean is_customer() {
        return this.customer;
    }
}
