export const appRoutes = {
    ROOT: "admin",
    HOME: "home",
    SCENES: "scenes",
    SPOTIFY: "spotify",
    SPOTIFY_CALLBACK: "spotify/callback",
}

export const apiEndpoints = {
    SCENE: "automation/scene",
    SCENE_REST: "automation/scene/rest",
    SPOTIFY: "automation/spotify",
    SPOTIFY_CLIENT: "automation/spotify/client",
    SPOTIFY_PLAYBACK: "automation/spotify/playback",
    SPOTIFY_RESOURCES: "automation/spotify/resources",
}
export type HttpMethods = 'GET' | 'POST' | 'PUT' | 'DELETE';