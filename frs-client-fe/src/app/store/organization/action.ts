// import {Action} from '@ngrx/store';
// import {Organization} from "../../data/organization";
// import {EntityMap, Update} from "@ngrx/entity";
//
// export enum OrganizationActionTypes {
//   LOAD_ALL = '[Organization] Load All Organizations',
//   LOADED_ALL_SUCCESS = '[Organization] Loaded All Organization Success',
//   LOAD_ORGANIZATION = '[Organization] Get Organization',
//   LOADED_ONE_SUCCESS = '[Organization] Loaded One Organization Success',
//   GET_ONE = '[Organization] Get One Organization',
//   CREATE = '[Organization] Create Organization',
//   ADD_ORGANIZATION = '[Organization] Add Organization',
//   UPDATE_ORGANIZATION = '[Organization] Add Organization',
//   MAP_ORGANIZATIONS = '[Organization] Add Organization',
//   DELETE_ORGANIZATION = '[Organization] Add Organization',
// }
//
// export class LoadAll implements Action {
//   readonly type = OrganizationActionTypes.LOAD_ALL;
//
//   constructor() {
//   }
// }
//
// export class GetOne implements Action {
//   readonly type = OrganizationActionTypes.LOAD_ORGANIZATION;
//
//   constructor(public payload: Organization) {
//   }
// }
//
// export class Create implements Action {
//   readonly type = OrganizationActionTypes.CREATE;
//
//   constructor(public payload: Organization) {
//   }
// }
//
// export class LoadedAllSuccess implements Action {
//   readonly type = OrganizationActionTypes.LOADED_ALL_SUCCESS;
//
//   constructor(public payload: { organizations: Organization[] }) {
//   }
// }
//
// export class AddOrganization implements Action {
//   readonly type = OrganizationActionTypes.ADD_ORGANIZATION;
//
//   constructor(public payload: { Organization: Organization }) {
//   }
// }
//
// export class UpdateOrganization implements Action {
//   readonly type = OrganizationActionTypes.UPDATE_ORGANIZATION;
//
//   constructor(public payload: { Organization: Update<Organization> }) {
//   }
// }
//
// export class MapOrganizations implements Action {
//   readonly type = OrganizationActionTypes.MAP_ORGANIZATIONS;
//
//   constructor(public payload: { entityMap: EntityMap<Organization> }) {
//   }
// }
//
// export class DeleteOrganization implements Action {
//   readonly type = OrganizationActionTypes.DELETE_ORGANIZATION;
//
//   constructor(public payload: { id: string }) {
//   }
// }
//
//
// export type OrganizationActions =
//   | LoadAll
//   | GetOne
//   | Create
//   | LoadedAllSuccess
//   | AddOrganization
//   | MapOrganizations
//   | UpdateOrganization
//   | DeleteOrganization
//
