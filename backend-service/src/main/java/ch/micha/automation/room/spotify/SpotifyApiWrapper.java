package ch.micha.automation.room.spotify;

import ch.micha.automation.room.cache.EntityCache;
import ch.micha.automation.room.cache.EntityCacheFactory;
import ch.micha.automation.room.errorhandling.exceptions.ResourceNotFoundException;
import ch.micha.automation.room.errorhandling.exceptions.SpotifyException;
import ch.micha.automation.room.errorhandling.exceptions.SpotifyNotAuthorizedException;
import ch.micha.automation.room.errorhandling.exceptions.UnexpectedSpotifyException;
import ch.micha.automation.room.spotify.dtos.*;
import ch.micha.automation.room.spotify.interceptor.SpeakerRequired;
import ch.micha.automation.room.spotify.interceptor.SpotifyAuthorized;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.HttpRequestWithBody;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("java:S1192") // don't care about multiple same strings
@ApplicationScoped
public class SpotifyApiWrapper {
    private final Logger logger = Logger.getLogger(getClass().getSimpleName());
    public static final String SPOTIFY_API = "https://api.spotify.com";
    public static final String SPOTIFY_AUTH_API = "https://accounts.spotify.com/api/token";
    public static final String SPOTIFY_PLAYER_PREFIX = "/v1/me/player";
    public static final String SPOTIFY_AUTH_HEADER = "Authorization";
    public static final String CONTENT_TYPE_HEADER = "Content-Type";
    public static final String CONTENT_TYPE_FORM = "application/x-www-form-urlencoded";
    public static final String CONTENT_TYPE_JSON = "application/json";

    private final EntityCache<List<SpotifyResourceDTO>> resourceCache;
    private final String userId;
    private final String deviceName;
    private String deviceId;

    private SpotifyAuthorisationDTO cachedAuth;
    private boolean initialized = false;

    @Inject
    public SpotifyApiWrapper(EntityCacheFactory cacheFactory,
                             @ConfigProperty(name = "room.automation.spotify.device") String deviceName,
                             @ConfigProperty(name = "room.automation.spotify.user") String user) {
        this.deviceName = deviceName;
        this.userId = user;

        resourceCache = cacheFactory.build();
    }

    /**
     * caches the given auth and also loads the id of the configured default device from spotify
     * @param auth a working set of the auth tokens
     */
    public void init(SpotifyAuthorisationDTO auth) {
        logger.log(Level.INFO, "initializing spotify api");

        this.cachedAuth = auth;
        initialized = true;
        this.deviceId = loadDefaultDevice();

        logger.log(Level.INFO, "successfully initialized spotify api");
    }

    /**
     * request a new token for the entire application
     * @param code the code provided by spotify, basically says, user has granted access
     * @param redirectUri the URL to be redirected to (just a security thing)
     * @param client the "spotify dev account credentials"
     * @return a new set of authentication tokens
     */
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

    /**
     * wrapper for {@link #refreshTokenIfExpired(SpotifyAuthorisationDTO, SpotifyClientDTO)}, will use cached auth
     * @param client the configured client secrets
     * @return the refreshed auth token set or null if not expired
     */
    public SpotifyAuthorisationDTO refreshTokenIfExpired(SpotifyClientDTO client) {
        return refreshTokenIfExpired(cachedAuth, client);
    }

    /**
     * checks if the token is expired, if so refreshes it and returns it
     * @param currentAuth the auth token set to check if it expired
     * @param client the configured client secrets
     * @return the refreshed auth token set or null if not expired
     * @throws SpotifyNotAuthorizedException if given auth was null
     */
    public synchronized SpotifyAuthorisationDTO refreshTokenIfExpired(SpotifyAuthorisationDTO currentAuth, SpotifyClientDTO client) {
        if(currentAuth == null)
            throw new SpotifyNotAuthorizedException();

        if(currentAuth.getGeneratedAt() + currentAuth.getExpiresIn() < Instant.now().getEpochSecond()) {
            logger.log(Level.INFO, "refreshing spotify access token");
            try {
                HttpResponse<JsonNode> response = Unirest
                        .post(SPOTIFY_AUTH_API)
                        .basicAuth(client.getClientId(), client.getClientSecret())
                        .header(CONTENT_TYPE_HEADER, CONTENT_TYPE_FORM)
                        .field("grant_type", "refresh_token")
                        .field("refresh_token", currentAuth.getRefreshToken())
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
                    createdDto.setRefreshToken(currentAuth.getRefreshToken());
                }

                this.cachedAuth = createdDto;
                return createdDto;
            } catch (UnirestException e) {
                throw new UnexpectedSpotifyException(e);
            }
        }
        return null;
    }

    /**
     * @return the current player, null if player stopped
     */
    @SpotifyAuthorized
    public SpotifyPlayerDTO getPlayer() {
        try {
            logger.log(Level.INFO, "loading current spotify player");

            HttpResponse<JsonNode> response = callApi(SPOTIFY_PLAYER_PREFIX, "get", null, null);
            if(response.getStatus() == 204)
                return null;

            throwUnexpectedIfNeeded(response, "failed to load playback state");

            JSONObject body = response.getBody().getObject();
            JSONObject item = body.getJSONObject("item");
            String songName = item.getString("name");
            String trackUrl = item.getJSONObject("external_urls").getString("spotify");
            String trackUri = item.getString("uri");

            JSONObject album = body.getJSONObject("item").getJSONObject("album");
            String artistName = album.getJSONArray("artists").getJSONObject(0).getString("name");
            String albumName = album.getString("name");

            JSONArray images = album.getJSONArray("images");
            String albumCoverUrl = images.getJSONObject(0).getString("url");

            JSONObject device = body.getJSONObject("device");
            String currentDeviceName = device.getString("name");
            int volume = device.optInt("volume_percent");

            String contextUri = body.getJSONObject("context").optString("uri");

            boolean playing = body.getBoolean("is_playing");

            logger.log(Level.INFO, "successfully fetched spotify player");
            return new SpotifyPlayerDTO(
                    songName,
                    artistName,
                    albumName,
                    albumCoverUrl,
                    currentDeviceName,
                    trackUrl,
                    new SpotifyContextDTO(
                        contextUri,
                        trackUri,
                        volume
                    ),
                    playing
            );
        } catch (UnirestException e) {
            throw new UnexpectedSpotifyException(e);
        } catch (JSONException e) {
            logger.log(Level.WARNING, "failed to parse response for spotify player: {0}", e.getMessage());
            throw new UnexpectedSpotifyException("could not parse json");
        }
    }

    /**
     * tells spotify to move to the next song
     */
    @SpeakerRequired
    @SpotifyAuthorized
    public void nextSong() {
        try {
            logger.log(Level.INFO, "skipping song");

            HttpResponse<JsonNode> response = callApi(SPOTIFY_PLAYER_PREFIX + "/next", "post", null, Map.of("device_id", deviceId));
            throwUnexpectedIfNeeded(response, "failed to skip song");

            logger.log(Level.INFO, "successfully skipped song");
        } catch (UnirestException e) {
            throw new UnexpectedSpotifyException(e);
        }
    }

    /**
     * tells spotify to move to the previous song
     */
    @SpeakerRequired
    @SpotifyAuthorized
    public void previousSong() {
        try {
            logger.log(Level.INFO, "moving to previous song");

            HttpResponse<JsonNode> response = callApi(SPOTIFY_PLAYER_PREFIX + "/previous", "post", null, Map.of("device_id", deviceId));
            throwUnexpectedIfNeeded(response, "failed to go to previous song");

            logger.log(Level.INFO, "successfully moved to next song");
        } catch (UnirestException e) {
            throw new UnexpectedSpotifyException(e);
        }
    }

    /**
     * @return the state of the current player
     */
    @SpotifyAuthorized
    public SpotifyPlayerState getPlayerState() {
        try {
            logger.log(Level.INFO, "loading playback state of current player");

            HttpResponse<JsonNode> response = callApi(SPOTIFY_PLAYER_PREFIX, "get", null, null);
            if(response.getStatus() == 204)
                return SpotifyPlayerState.STOPPED;

            throwUnexpectedIfNeeded(response, "failed to load playback state");

            JSONObject body = response.getBody().getObject();
            return body.getBoolean("is_playing") ? SpotifyPlayerState.PLAYING : SpotifyPlayerState.PAUSED;
        } catch (UnirestException e) {
            throw new UnexpectedSpotifyException(e);
        } catch (NullPointerException e) {
            logger.info("no player is active");
            return SpotifyPlayerState.STOPPED;
        }
    }

    /**
     * tells spotify to pause the playback
     */
    @SpotifyAuthorized
    public void pausePlayback() {
        try {
            logger.log(Level.INFO, "pausing playback of default device");

            HttpResponse<JsonNode> response = callApi(SPOTIFY_PLAYER_PREFIX + "/pause", "put", null, null);

            if(response.getStatus() == 404) {
                logger.log(Level.WARNING, "tried to pause already paused player");
            } else {
                throwUnexpectedIfNeeded(response, "failed to pause playback");
                logger.log(Level.INFO, "successfully paused playback");
            }
        } catch (UnirestException e) {
            throw new UnexpectedSpotifyException(e);
        }
    }

    /**
     * resumes the playback, if player is stopped nothing happens
     */
    @SpeakerRequired
    @SpotifyAuthorized
    public void resumePlayback() {
        try {
            logger.log(Level.INFO, "resuming playback");

            HttpResponse<JsonNode> response = callApi(SPOTIFY_PLAYER_PREFIX + "/play", "put", null, Map.of("device_id", deviceId));
            throwUnexpectedIfNeeded(response, "failed to resume playback");

            logger.log(Level.INFO, "successfully resumed playback");
        } catch (UnirestException e) {
            throw new UnexpectedSpotifyException(e);
        }
    }

    /**
     * starts or resumes the given context uri at a given optional offsetUri
     * @param contextUri the context of the track to play in (playlist / album uri)
     * @param offsetUri the uri for the offset within the context
     */
    @SpeakerRequired
    @SpotifyAuthorized
    public void playContext(String contextUri, String offsetUri) {
        try {
            logger.log(Level.INFO, "playing {0} at {1}", new Object[]{contextUri, offsetUri});

            JSONObject body = new JSONObject();
            body.put("context_uri", contextUri);
            if(offsetUri != null && !offsetUri.isBlank()) {
                JSONObject offset = new JSONObject();
                offset.put("uri", offsetUri);
                body.put("offset", offset);
            }

            HttpResponse<JsonNode> response = callApi(SPOTIFY_PLAYER_PREFIX + "/play", "put", body, Map.of("device_id", deviceId));
            throwUnexpectedIfNeeded(response, "failed to play contextUri %s, at %s".formatted(contextUri, offsetUri));

            logger.log(Level.INFO, "successfully started at context at offset");
        } catch (UnirestException e) {
            throw new UnexpectedSpotifyException(e);
        }
    }

    /**
     * sets the volume of the current spotify player
     * @param volume the volume in percent to apply
     */
    @SpeakerRequired
    @SpotifyAuthorized
    public void setPlaybackVolume(int volume) {
        try {
            volume = Math.max(0, Math.min(100, volume));
            logger.log(Level.INFO, "setting playback volume to {0}", volume);

            HttpResponse<JsonNode> response = callApi(SPOTIFY_PLAYER_PREFIX + "/volume", "put", null, Map.of("device_id", deviceId, "volume_percent", volume));
            throwUnexpectedIfNeeded(response, "failed to set playback volume");

            logger.log(Level.INFO, "successfully set playback volume");
        } catch (UnirestException e) {
            throw new UnexpectedSpotifyException(e);
        }
    }

    /**
     * sets to shuffle of the player
     * @param shuffle the state of the shuffle to apply
     */
    @SpeakerRequired
    @SpotifyAuthorized
    public void setPlaybackShuffle(boolean shuffle) {
        try {
            logger.log(Level.INFO, "setting playback shuffle to {0}", shuffle);

            HttpResponse<JsonNode> response = callApi(SPOTIFY_PLAYER_PREFIX + "/shuffle", "put", null, Map.of("device_id", deviceId, "state", shuffle));
            throwUnexpectedIfNeeded(response, "failed to set playback shuffle");

            logger.log(Level.INFO, "successfully set playback shuffle");
        } catch (UnirestException e) {
            throw new UnexpectedSpotifyException(e);
        }
    }

    /**
     * loads the first 50 saved playlists from spotify
     * these playlists are cached. this means they are only loaded from spotify after the cache expires.
     * the cache is automatically updated
     * @return the first 50 saved playlists
     */
    @SpotifyAuthorized
    public List<SpotifyResourceDTO> getSavedSpotifyResources() {
        try {
            List<SpotifyResourceDTO> playlists = new ArrayList<>();
            if(!resourceCache.isValid()) {
                logger.log(Level.INFO, "loading first 50 saved playlists from spotify");
                HttpResponse<JsonNode> response = callApi(String.format("/v1/users/%s/playlists", userId), "get", null, Map.of("limit", 50));
                throwUnexpectedIfNeeded(response, "could not load saved playlists");

                JSONObject body = response.getBody().getObject();
                JSONArray jsonPlaylists = body.getJSONArray("items");

                for (int i = 0; i < jsonPlaylists.length(); i++)
                    playlists.add(parseResource(jsonPlaylists.getJSONObject(i)));

                resourceCache.store(playlists);
            } else {
                logger.log(Level.INFO, "loading playlists from cache");
                playlists = resourceCache.load();
            }

            logger.log(Level.INFO, "successfully loaded {0} playlists", playlists.size());
            return playlists;
        } catch (UnirestException e) {
            throw new UnexpectedSpotifyException(e);
        }
    }

    /**
     * loads a specific playlist
     * the playlists are cached. this means they are only loaded from spotify after the cache expires.
     * the cache is automatically updated
     * @return the searched playlist
     */
    @SpotifyAuthorized
    public Optional<SpotifyResourceDTO> getSavedSpotifyResource(String resourceUri) {
        logger.log(Level.INFO, "searching for playlist: {0}", resourceUri);

        if(resourceUri == null || resourceUri.isBlank() || resourceUri.isEmpty())
            return Optional.empty();

        if(resourceCache.isValid()) {
            Optional<SpotifyResourceDTO> cachedPlaylist = resourceCache.load().stream()
                    .filter(p -> p.getSpotifyURI().equals(resourceUri))
                    .findFirst();
            if(cachedPlaylist.isPresent()) {
                logger.log(Level.INFO, "found playlist in cache");
                return cachedPlaylist;
            }
        }

        try {
            String playlistId = resourceUri.substring("spotify:playlist:".length());
            logger.log(Level.INFO, "playlist not found in cache, searching spotify for playlist with id {0}", playlistId);
            HttpResponse<JsonNode> response = callApi(String.format("/v1/playlists/%s", playlistId), "get", null,
                    Map.of("fields", "name,description,uri,external_urls(spotify),images"));

            if(response.getStatus() == 404)
                return Optional.empty();
            throwUnexpectedIfNeeded(response, "could not load playlist");

            logger.log(Level.INFO, "successfully loaded playlist from spotify");
            return Optional.of(parseResource(response.getBody().getObject()));
        } catch (UnirestException e) {
            throw new UnexpectedSpotifyException(e);
        }
    }

    /**
     * @return the spotify device id to the configured default device
     */
    private String loadDefaultDevice() {
        try {
            logger.log(Level.INFO, "loading configured device: {0}", deviceName);

            HttpResponse<JsonNode> response = callApi(SPOTIFY_PLAYER_PREFIX + "/devices", "get", null, null);
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

    private HttpResponse<JsonNode> callApi(String endpoint, String method, JSONObject body, Map<String, Object> queries) throws UnirestException {
        if(!initialized)
            throw new IllegalStateException("api was never initialized");

        endpoint = SPOTIFY_API + endpoint;
        if(queries == null)
            queries = new HashMap<>();

        if(method.equals("get")) {
            return Unirest.get(endpoint)
                    .header(SPOTIFY_AUTH_HEADER, cachedAuth.getTokenType() +  " " + cachedAuth.getAccessToken())
                    .header(CONTENT_TYPE_HEADER, CONTENT_TYPE_JSON)
                    .queryString(queries)
                    .asJson();
        }

        HttpRequestWithBody request;
        if(method.equals("post"))
            request = Unirest.post(endpoint);
        else
            request = Unirest.put(endpoint);

        if(body == null)
            body = new JSONObject();

        return request
                .header(SPOTIFY_AUTH_HEADER, cachedAuth.getTokenType() +  " " + cachedAuth.getAccessToken())
                .header(CONTENT_TYPE_HEADER, CONTENT_TYPE_JSON)
                .queryString(queries)
                .body(body)
                .asJson();
    }

    private SpotifyResourceDTO parseResource(JSONObject spotifyPlaylist) {
        SpotifyResourceDTO playlist = new SpotifyResourceDTO();
        playlist.setName(spotifyPlaylist.getString("name"));
        playlist.setDescription(spotifyPlaylist.getString("description"));
        playlist.setSpotifyURI(spotifyPlaylist.getString("uri"));
        playlist.setHref(spotifyPlaylist
                .getJSONObject("external_urls")
                .getString("spotify")
        );
        playlist.setImageUrl(spotifyPlaylist
                .getJSONArray("images")
                .getJSONObject(0)
                .getString("url")
        );

        return playlist;
    }

    /**
     * if the response status is an error (> 206) then logs the error, including the error body and throws an unexpected
     * spotify exception
     * EXCEPT the error is 404 and the body contains "Device not found" in this case, a resource not found Exception is thrown.
     * @param response the response from the spotify api call
     * @param message the message to give to the thrown error
     */
    private void throwUnexpectedIfNeeded(HttpResponse<JsonNode> response, String message){
        if(response.getStatus() > 206) {
            try {
                String responseBody = new String(response.getRawBody().readAllBytes());
                logger.log(Level.INFO, "call to spotify failed. with body: {0} - {1}", new Object[]{response.getStatus(), responseBody});

                if(response.getStatus() == 404 && responseBody.contains("Device not found"))
                    throw new ResourceNotFoundException("spotify speaker", deviceId);
            } catch (IOException e) {
                logger.log(Level.INFO, "call to spotify failed. with status: {0} - {1}", new Object[]{ response.getStatus(), response.getStatusText() });
            }
            throw new UnexpectedSpotifyException(String.format("%s - %s: %s", response.getStatus(), response.getStatusText(), message));
        }
    }

    public boolean isInitialized() {
        return initialized;
    }
}
