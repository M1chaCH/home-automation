package ch.micha.automation.room.alarm;

public record AlarmEntity(
    int id,
    String cronSchedule,
    boolean active,
    String spotifyResource,
    int maxVolume
) { }
