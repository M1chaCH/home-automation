package ch.micha.automation.room.spotify;

import ch.micha.automation.room.errorhandling.exceptions.SpotifyAlreadyAuthorizedException;
import ch.micha.automation.room.errorhandling.exceptions.SpotifyException;
import ch.micha.automation.room.errorhandling.exceptions.SpotifyNotAuthorizedException;
import ch.micha.automation.room.errorhandling.exceptions.UnexpectedSpotifyException;
import ch.micha.automation.room.spotify.dtos.SpotifyAuthorisationDTO;
import ch.micha.automation.room.spotify.dtos.SpotifyClientDTO;
import ch.micha.automation.room.spotify.dtos.SpotifyCodeDTO;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.json.JSONObject;

import java.io.IOException;
import java.time.Instant;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This service calls the spotify player API to play or search specific music.<br>
 * Some helpful links:
 * https://developer.spotify.com/documentation/general/guides/authorization/code-flow/
 * https://developer.spotify.com/documentation/web-api/reference/#/
 */
@ApplicationScoped
public class SpotifyService {
    private final Logger logger = Logger.getLogger(getClass().getSimpleName());
    public static final String SPOTIFY_API = "https://api.spotify.com";
    public static final String SPOTIFY_AUTH_API = "https://accounts.spotify.com/api/token";

    private final SpotifyProvider provider;

    @Inject
    public SpotifyService(SpotifyProvider provider) {
        this.provider = provider;
    }

    public SpotifyAuthorisationDTO getAccess() {
        return provider.findAuth().orElseThrow(SpotifyNotAuthorizedException::new);
    }

    public SpotifyClientDTO getClient() {
        return provider.getClient();
    }

    public SpotifyAuthorisationDTO addAuthorisation(SpotifyCodeDTO dto) {
        SpotifyAuthorisationDTO auth = requestAccessToken(dto.getCode(), dto.getRedirectUrl());
        if(!provider.insertAuth(auth))
            throw new SpotifyAlreadyAuthorizedException();

        return auth;
    }

    public void togglePlayback() {
        SpotifyClientDTO client = getClient();
        try {
            HttpResponse<JsonNode> response = Unirest
                    .put(SPOTIFY_API + "/v1/me/player/play")
                    .basicAuth(client.getClientId(), client.getClientSecret())
                    .header("Content-Type", "application/json")
                    .asJson();

            if (response.getStatus() != 200) {
                logger.log(Level.INFO, "got error: {0}", new String(response.getRawBody().readAllBytes()));
                throw new SpotifyException("could not toggle playback");
            }
        } catch (UnirestException | IOException e) {
            throw new UnexpectedSpotifyException(e);
        }
    }

    // TODO implement automatic token refresh
    private SpotifyAuthorisationDTO requestAccessToken(String code, String redirectUri) {
        logger.log(Level.INFO, "requesting spotify access token");
        SpotifyClientDTO client = getClient();

        try {
            HttpResponse<JsonNode> response = Unirest
                    .post(SPOTIFY_AUTH_API)
                    .basicAuth(client.getClientId(), client.getClientSecret())
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .field("code", code)
                    .field("redirect_uri", redirectUri)
                    .field("grant_type", "authorization_code")
                    .asJson();

            if(response.getStatus() != 200) {
                String errorBody = new String(response.getRawBody().readAllBytes());

                if(errorBody.contains("Invalid authorization code"))
                    throw new SpotifyException("got invalid authorisation code");

                throw new UnexpectedSpotifyException(new RuntimeException(
                        String.format("failed to request access token -> status: %s - body: %s",
                                response.getStatus(), errorBody)));
            }

            logger.log(Level.INFO, "got response from spotify: {0}", response.getStatusText());
            SpotifyAuthorisationDTO dto = new SpotifyAuthorisationDTO();
            JSONObject body = response.getBody().getObject();
            dto.setAccessToken(body.getString("access_token"));
            dto.setTokenType(body.getString("token_type"));
            dto.setScope(body.getString("scope"));
            dto.setExpiresIn(body.getInt("expires_in"));
            dto.setRefreshToken(body.getString("refresh_token"));
            dto.setGeneratedAt(Instant.now().getEpochSecond());

            return dto;
        } catch (UnirestException | IOException e) {
            throw new UnexpectedSpotifyException(e);
        }
    }
}
