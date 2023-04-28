package ch.micha.automation.room.spotify.dtos;

/**
 * an enum to wrap the state of the spotify player
 * PLAYING: a song is playing on a device
 * PAUSE: a device and song is ready, but nothing is playing
 * STOPPED: no device connected and nothing is happening (when the app shows nothing at the bottom)
 */
public enum SpotifyPlayerState {
    PLAYING,
    PAUSED,
    STOPPED
}
