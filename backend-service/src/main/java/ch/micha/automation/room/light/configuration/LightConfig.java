package ch.micha.automation.room.light.configuration;

/**
 * @param id autoincrement number
 * @param name unique human-readable identifier
 * @param red red color (0-255)
 * @param green green color (0-255)
 * @param blue blue color (0-255)
 * @param brightness (1-100)
 */
public record LightConfig(
        int id,
        String name,
        int red,
        int green,
        int blue,
        int brightness)
{ }
