package ch.micha.automation.room.events;

public enum HandlerPriority {
    FIRST(-1),
    LAST(1),
    NOT_APPLICABLE(0);

    private final int priority;

    HandlerPriority(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }
}
