package ch.micha.automation.room.spotify;

import ch.micha.automation.room.errorhandling.exceptions.SpotifyException;
import ch.micha.automation.room.errorhandling.exceptions.UnexpectedSpotifyException;
import ch.micha.automation.room.spotify.dtos.SpotifyAuthorisationDTO;
import ch.micha.automation.room.spotify.dtos.SpotifyClientDTO;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.HttpRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("java:S1192") // don't care about multiple same strings
@ApplicationScoped
public class SpotifyApiService {
    private final Logger logger = Logger.getLogger(getClass().getSimpleName());
    public static final String SPOTIFY_API = "https://api.spotify.com";
    public static final String SPOTIFY_AUTH_API = "https://accounts.spotify.com/api/token";
    public static final String SPOTIFY_AUTH_HEADER = "Authorization";
    public static final String CONTENT_TYPE_HEADER = "Content-Type";
    public static final String CONTENT_TYPE_FORM = "application/x-www-form-urlencoded";
    public static final String CONTENT_TYPE_JSON = "application/json";

    private final String deviceName;
    private String deviceId;

    private SpotifyAuthorisationDTO auth;
    private boolean initialized = false;

    @Inject
    public SpotifyApiService(@ConfigProperty(name = "room.automation.spotify.device") String deviceName) {
        this.deviceName = deviceName;
    }

    public void init(SpotifyAuthorisationDTO auth) {
        logger.log(Level.INFO, "initializing spotify api");

        this.auth = auth;
        initialized = true;
        this.deviceId = loadDefaultDevice();

        logger.log(Level.INFO, "successfully initialized spotify api");
    }

    public SpotifyAuthorisationDTO requestAccessToken(String code, String redirectUri, SpotifyClientDTO client) {
        logger.log(Level.INFO, "requesting spotify access token");

        try {
            HttpResponse<JsonNode> response = Unirest
                    .post(SPOTIFY_AUTH_API)
                    .basicAuth(client.getClientId(), client.getClientSecret())
                    .header(CONTENT_TYPE_HEADER, CONTENT_TYPE_FORM)
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

    public SpotifyAuthorisationDTO refreshTokenIfExpired(SpotifyClientDTO client) {
        if(auth.getGeneratedAt() + auth.getExpiresIn() < Instant.now().getEpochSecond()) {
            logger.log(Level.INFO, "refreshing spotify access token");
            try {
                HttpResponse<JsonNode> response = Unirest
                        .post(SPOTIFY_AUTH_API)
                        .basicAuth(client.getClientId(), client.getClientSecret())
                        .header(CONTENT_TYPE_HEADER, CONTENT_TYPE_FORM)
                        .field("grant_type", "refresh_token")
                        .field("refresh_token", auth.getRefreshToken())
                        .asJson();

                throwUnexpectedIfNeeded(response, "failed to refresh token");

                JSONObject body = response.getBody().getObject();
                SpotifyAuthorisationDTO createdDto = new SpotifyAuthorisationDTO();
                createdDto.setAccessToken(body.getString("access_token"));
                createdDto.setTokenType(body.getString("token_type"));
                createdDto.setScope(body.getString("scope"));
                createdDto.setExpiresIn(body.getInt("expires_in"));
                createdDto.setGeneratedAt(Instant.now().getEpochSecond());

                if(!body.isNull("refresh_token")) {
                    createdDto.setRefreshToken(body.getString("refresh_token"));
                } else {
                    createdDto.setRefreshToken(auth.getRefreshToken());
                }

                return createdDto;
            } catch (UnirestException e) {
                throw new UnexpectedSpotifyException(e);
            }
        }
        return null;
    }

    public boolean isPlaying() {
        try {
            logger.log(Level.INFO, "loading playback state of current player");

            HttpResponse<JsonNode> response = callPlayerApi("", "get", null, null);
            throwUnexpectedIfNeeded(response, "failed to load playback state");

            JSONObject body = response.getBody().getObject();
            return body.getBoolean("is_playing");
        } catch (UnirestException e) {
            throw new UnexpectedSpotifyException(e);
        } catch (NullPointerException e) {
            logger.info("no player is active");
            return false;
        }
    }

    public void pausePlayback() {
        try {
            logger.log(Level.INFO, "pausing playback of default device");

            HttpResponse<JsonNode> response = callPlayerApi("/pause", "put", null, null);
            throwUnexpectedIfNeeded(response, "failed to pause playback");

            logger.log(Level.INFO, "successfully paused playback");
        } catch (UnirestException e) {
            throw new UnexpectedSpotifyException(e);
        }
    }

    public void resumePlayback() {
        try {
            logger.log(Level.INFO, "resuming playback");

            HttpResponse<JsonNode> response = callPlayerApi("/play", "put", Map.of("device_id", deviceId), null);
            throwUnexpectedIfNeeded(response, "failed to resume playback");

            logger.log(Level.INFO, "successfully resumed playback");
        } catch (UnirestException e) {
            throw new UnexpectedSpotifyException(e);
        }
    }

    private String loadDefaultDevice() {
        try {
            logger.log(Level.INFO, "loading configured device: {0}", deviceName);

            HttpResponse<JsonNode> response = callPlayerApi("/devices", "get", null, null);
            throwUnexpectedIfNeeded(response, "failed to load device id");

            JSONObject body = response.getBody().getObject();
            JSONArray devices = body.getJSONArray("devices");
            for (int i = 0; i < devices.length(); i++) {
                JSONObject device = devices.getJSONObject(i);
                if(deviceName.equals(device.getString("name"))) {
                    String foundDeviceId = device.getString("id");
                    logger.log(Level.INFO, "found device id: {0}", foundDeviceId);
                    return foundDeviceId;
                }
            }

            logger.log(Level.WARNING, "could not find available device with name: {0}", deviceName);
            return null;
        } catch (UnirestException e) {
            throw new UnexpectedSpotifyException(e);
        }
    }

    private HttpResponse<JsonNode> callPlayerApi(String endpoint, String method, Map<String, Object> queries, Map<String, String> customHeaders) throws UnirestException {
        if(!initialized)
            throw new IllegalStateException("api was never initialized");

        endpoint = SPOTIFY_API + "/v1/me/player" + endpoint;
        if(customHeaders == null)
            customHeaders = new HashMap<>();
        if(queries == null)
            queries = new HashMap<>();

        HttpRequest request = switch (method) {
            case "post" -> Unirest.post(endpoint);
            case "put" -> Unirest.put(endpoint);
            default -> Unirest.get(endpoint);
        };

        return request
                .header(SPOTIFY_AUTH_HEADER, auth.getTokenType() +  " " + auth.getAccessToken())
                .header(CONTENT_TYPE_HEADER, CONTENT_TYPE_JSON)
                .headers(customHeaders)
                .queryString(queries)
                .asJson();
    }

    private void throwUnexpectedIfNeeded(HttpResponse<JsonNode> response, String message){
        if(response.getStatus() > 206) {
            try {
                logger.log(Level.INFO, "call to spotify failed. with body: {0} - {1}", new Object[]{response.getStatus(), new String(response.getRawBody().readAllBytes())});
            } catch (IOException e) {
                logger.log(Level.INFO, "call to spotify failed. with status: {0} - {1}", new Object[]{ response.getStatus(), response.getStatusText() });
            }
            throw new UnexpectedSpotifyException(message);
        }
    }
}
