package ch.micha.automation.room.sql;

public class UnknownNameGenerator {
    private final String type;

    private int count = 0;

    public UnknownNameGenerator(String type) {
        this.type = type;
    }

    public UnknownNameGenerator() {
        this.type = "";
    }

    public String nextString() {
        count++;
        return String.format("unknown_%s_%s", type, count);
    }

    public int nextNumber() {
        count++;
        return count;
    }
}
