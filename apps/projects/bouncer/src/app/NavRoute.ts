export enum NavRoute {
  LOGIN = '/login',
  LOGOUT = '/logout',
  SCAN = '/scan',
  SYNC = '/sync',
  LOGS = '/logs',
  SETTINGS = '/settings'
}

export function getAllNavRoutes(): string[] {
  return Object.keys(NavRoute).map((key) => NavRoute[key])
}
