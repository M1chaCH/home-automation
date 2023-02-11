package ch.micha.automation.room.light;

/**
 * @param id autoincrement number
 * @param name unique human-readable identifier
 * @param power device power
 * @param red red color (0-255)
 * @param green green color (0-255)
 * @param blue blue color (0-255)
 * @param brightness (1-100)
 * @param changeDurationMillis if 0 DEFAULT will be used (nullable)
 */
public record LightConfigDTO(
        int id,
        String name,
        boolean power,
        int red,
        int green,
        int blue,
        int brightness,
        int changeDurationMillis)
{ }
