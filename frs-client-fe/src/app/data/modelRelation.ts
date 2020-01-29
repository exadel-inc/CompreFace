import { Application } from './application';

export interface ModelRelation {
  id: string;
  applications: Application[];
}