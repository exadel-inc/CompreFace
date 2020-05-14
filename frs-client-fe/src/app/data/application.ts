export interface Application {
  id: string;
  name: string;
  owner: {
    id: string,
    firstName: string,
    lastName: string
  };
  role: string;
  organizationId: string;
  apiKey?: string;
}

export interface ApplicationUpdateDto {
  name: string;
  id: string;
  organizationId: string;
}
