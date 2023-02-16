
export type SpotifyAuthorisationDTO = {
  accessToken: string,
  tokenType: string,
  scope: string,
  generatedAt: number,
  expiresIn: number,
  refreshToken: string
}