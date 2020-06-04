export interface Model {
  id: string;
  name: string;
  accessLevel: string;
  relations: {
    id: string;
    shareMode: string;
  }[];
  owner: {
    id: string;
    firstName: string;
    lastName: string;
  };
  role: string;
  apiKey?: string;
}

export interface ModelUpdate {
  name: string;
  applicationId: string;
  organizationId: string;
  modelId: string;
}
