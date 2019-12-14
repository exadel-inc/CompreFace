// import { Injectable } from '@angular/core';
// import {Actions, Effect, ofType} from '@ngrx/effects';
// import { Observable, of as observableOf } from 'rxjs';
// import {OrganizationActionTypes, LoadedAllSuccess} from "./action";
// import {catchError, map, switchMap, tap} from "rxjs/operators";
// import {OrganizationService} from "../../core/organization/organization.service";
//
// @Injectable()
// export class OrganizationEffects {
//   constructor(private actions: Actions, private organizationService: OrganizationService) {}
//
//   // Listen for the 'LOGIN' action
//   @Effect()
//   GetAll: Observable<any> = this.actions.pipe(
//     ofType(OrganizationActionTypes.LOAD_ALL),
//     map(() => null),
//     switchMap(() => {
//       return this.organizationService.GetAll().pipe(
//         map(res => {
//           return new LoadedAllSuccess({ organizations: res });
//         }),
//       )
//
//     }));
//
//   // Listen for the 'LogInSuccess' action
//   // @Effect({ dispatch: false })
//   // LogInSuccess: Observable<any> = this.actions.pipe(
//   //   ofType(OrganizationActionTypes.LOGIN_SUCCESS),
//   //   tap(data => {
//   //     localStorage.setItem('token', data.payload.token);
//   //     this.router.navigateByUrl(ROUTERS_URL.ORGANIZATION);
//   //   })
//   // );
//   //
//   // // Listen for the 'LogInFailure' action
//   // @Effect({ dispatch: false })
//   // LogInFailure: Observable<any> = this.actions.pipe(
//   //   ofType(OrganizationActionTypes.LOGIN_FAILURE)
//   // );
//   //
//   // @Effect()
//   // SignUp: Observable<any> = this.actions.pipe(
//   //   ofType(OrganizationActionTypes.SIGNUP),
//   //   map((action: SignUp) => action.payload),
//   //   switchMap(payload => {
//   //     return this.authService.signUp(payload.username, payload.password, payload.email).pipe(
//   //       map(() => {
//   //         return new SignUpSuccess({});
//   //       }),
//   //       catchError(error =>
//   //         observableOf(new SignUpFailure({ error }))
//   //       )
//   //     )
//   //
//   //   }));
//   //
//   // @Effect({ dispatch: false })
//   // SignUpSuccess: Observable<any> = this.actions.pipe(
//   //   ofType(OrganizationActionTypes.SIGNUP_SUCCESS),
//   //   tap(() => {
//   //     this.router.navigateByUrl(ROUTERS_URL.LOGIN);
//   //   })
//   // );
//   //
//   // @Effect({ dispatch: false })
//   // SignUpFailure: Observable<any> = this.actions.pipe(
//   //   ofType(OrganizationActionTypes.SIGNUP_FAILURE)
//   // );
//   //
//   // @Effect({ dispatch: false })
//   // public LogOut: Observable<any> = this.actions.pipe(
//   //   ofType(OrganizationActionTypes.LOGOUT),
//   //   tap(() => {
//   //     localStorage.removeItem('token');
//   //     this.router.navigateByUrl(ROUTERS_URL.LOGIN);
//   //   })
//   // );
// }
