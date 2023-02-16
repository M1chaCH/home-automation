package ch.micha.automation.room.spotify.dtos;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class SpotifyAuthorisationDTO{
    /** An Access Token that can be provided in subsequent calls, for example to Spotify Web API services. */
    private String accessToken;
    /** How the Access Token may be used: always “Bearer”. */
    private String tokenType;
    /** A space-separated list of scopes which have been granted for this access_token */
    private String scope;
    /** the unix time code in seconds for when the accessToken was requested */
    private int generatedAt;
    /** The time period (in seconds) for which the Access Token is valid. */
    private int expiresIn;
    /**
     * A token that can be sent to the Spotify Accounts service in place of an authorization code. (When the access
     * code expires, send a POST request to the Accounts service /api/token endpoint, but use this code in place of an
     * authorization code. A new Access Token will be returned. A new refresh token might be returned too.)
     */
    private String refreshToken;
}
