export interface Model {
  id: string,
  name: string,
  accessLevel: string,
  applicationId: string,
  owner: {
    id: string,
    firstName: string,
    lastName: string
  }
}
