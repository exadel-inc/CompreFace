export interface Model {
  id: string,
  name: string,
  accessLevel: string,
  applicationId: {
    id: string,
    shareMode: string
  }[],
  owner: {
    id: string,
    firstName: string,
    lastName: string
  }
}
