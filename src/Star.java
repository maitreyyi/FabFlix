public class Star {

    private final String name;

    private final int dob;


    public Star(String name, int dob) {
        this.name = name;
        this.dob = dob;
    }


    public String getName() {
        return this.name;
    }

    public int getDob() {
        return dob;
    }

    public boolean isEqual(String name, int dob) {
        return this.name.equals(name) && this.dob == dob;
    }

    public String printStar() {
        return "Name: " + getName() + ", DOB: " + getDob();
    }

}
