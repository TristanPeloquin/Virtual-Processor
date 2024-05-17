//Represents a bit, which has two states - true or false (1 or 0)
public class Bit {
    private boolean value;

    public Bit(boolean value) {
        this.value = value;
    }

    // Sets bit to the given value
    void set(boolean value) {
        this.value = value;
    }

    // Sets bit to true
    void set() {
        value = true;
    }

    // Toggles a bit between true and false
    void toggle() {
        value = !value;
    }

    // Sets bit to false
    void clear() {
        value = false;
    }

    // Returns the value of the bit
    boolean getValue() {
        return value;
    }

    // Performs logical AND between this bit and another bit
    Bit and(Bit other) {
        if (value) {
            if (other.getValue()) {
                return new Bit(true);
            }
        }
        return new Bit(false);
    }

    // Performs logical OR on this bit and another bit
    Bit or(Bit other) {
        if (value) {
            return new Bit(true);
        }
        if (other.getValue()) {
            return new Bit(true);
        }
        return new Bit(false);
    }

    // Peforms logical XOR on this bit and another bit
    Bit xor(Bit other) {
        if (value) {
            if (other.getValue()) {
                return new Bit(false);
            }
            return new Bit(true);
        }
        if (other.getValue()) {
            return new Bit(true);
        }
        return new Bit(false);
    }

    // Performs logical NOT on this bit and another bit
    Bit not() {
        return new Bit(!value);
    }

    public String toString() {
        if (value) {
            return "1";
        }
        return "0";
    }
}