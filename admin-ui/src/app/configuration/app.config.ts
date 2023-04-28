export const appRoutes = {
    ROOT: "admin",
    HOME: "home",
    SCENES: "scenes",
    LIGHT_CONFIGS: "light-configs",
    SPOTIFY: "spotify",
    SPOTIFY_CALLBACK: "spotify/callback",
    DEVICES: "devices",
}

export const apiEndpoints = {
    AUTOMATION: "automation",
    SCENE: "automation/scene",
    SCENE_CRUD: "automation/scene/crud",
    CONFIG_CRUD: "automation/config/crud",
    SPOTIFY: "automation/spotify",
    SPOTIFY_CLIENT: "automation/spotify/client",
    SPOTIFY_PLAYBACK: "automation/spotify/playback",
    SPOTIFY_RESOURCES: "automation/spotify/resources",
    SPOTIFY_PLAYER: "automation/spotify/player",
    DEVICES: "automation/device",
}
export type HttpMethods = 'GET' | 'POST' | 'PUT' | 'DELETE';