import { TestBed } from '@angular/core/testing';
import {AuthGuard, LoginGuard} from './auth.guard';
import {AuthService} from "./auth.service";
import {Router} from "@angular/router";
import {ROUTERS_URL} from "../../data/routers-url";

describe('Auth Guard', () => {
  let guard: AuthGuard;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        AuthGuard,
        {
          provide: AuthService,
          useValue: { getToken: () => 'Some token'}
        },
        {
          provide: Router,
          useValue: { navigateByUrl: () => {}}
        }
      ],
    });
    guard = TestBed.get<AuthGuard>(AuthGuard);
    guard.router.navigateByUrl = jasmine.createSpy();
  });

  it('should return false if the user state is not logged in', () => {
    expect(guard.canActivate()).toBe(true);
    expect(guard.router.navigateByUrl).toHaveBeenCalledTimes(0);
  });

  it('should return true if the user state is logged in', () => {
    guard.auth.getToken = () => '';
    expect(guard.canActivate()).toBe(false);
    expect(guard.router.navigateByUrl).toHaveBeenCalledTimes(1);
    expect(guard.router.navigateByUrl).toHaveBeenCalledWith(ROUTERS_URL.LOGIN);
  });
});

describe('Login Guard', () => {
  let guard: AuthGuard;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        LoginGuard,
        {
          provide: AuthService,
          useValue: { getToken: () => ''}
        },
        {
          provide: Router,
          useValue: { navigateByUrl: () => {}}
        }
      ],
    });
    guard = TestBed.get<LoginGuard>(LoginGuard);
    guard.router.navigateByUrl = jasmine.createSpy();
  });

  it('should return true if the user state is not logged in', () => {
    expect(guard.canActivate()).toBe(true);
    expect(guard.router.navigateByUrl).toHaveBeenCalledTimes(0);
  });

  it('should return false if the user state is logged in', () => {
    guard.auth.getToken = () => 'Some token';
    expect(guard.canActivate()).toBe(false);
    expect(guard.router.navigateByUrl).toHaveBeenCalledTimes(1);
    expect(guard.router.navigateByUrl).toHaveBeenCalledWith(ROUTERS_URL.ORGANIZATION);
  });
});
