import {TestBed} from '@angular/core/testing';
import {AuthGuard, LoginGuard} from './auth.guard';
import {Router} from '@angular/router';
import {ROUTERS_URL} from '../../data/routers-url.variable';
import {Store, MemoizedSelector} from '@ngrx/store';
import {provideMockStore, MockStore} from '@ngrx/store/testing';
import {AppState} from 'src/app/store';
import {selectUserInfoState} from '../../store/userInfo/selectors';
import {UserInfoState} from 'src/app/store/userInfo/reducers';

describe('Auth Guard', () => {
  let guard: AuthGuard;
  let mockStore: MockStore<AppState>;
  let mockUsernameSelector: MemoizedSelector<AppState, UserInfoState>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        AuthGuard,
        provideMockStore(),
        {
          provide: Router,
          useValue: { navigateByUrl: () => { } }
        }
      ],
    });

    mockStore = TestBed.get(Store);
    mockUsernameSelector = mockStore.overrideSelector(selectUserInfoState, {
      avatar: '',
      email: '',
      firstName: '',
      guid: '',
      lastName: '',
      password: '',
      isAuthenticated: true,
    });
    guard = TestBed.get<AuthGuard>(AuthGuard);
    guard.router.navigateByUrl = jasmine.createSpy();
  });

  it('should return false if the user state is not logged in', () => {
    guard.canActivate().subscribe(value => {
      expect(value).toBeTruthy();
      expect(guard.router.navigateByUrl).toHaveBeenCalledTimes(0);
    });
  });

  it('should return true if the user state is logged in', () => {
    mockUsernameSelector.setResult({
      avatar: '',
      email: '',
      firstName: '',
      guid: '',
      lastName: '',
      password: '',
      isAuthenticated: false,
    });
    guard.canActivate().subscribe(value => {
      expect(value).toBeFalsy();
      expect(guard.router.navigateByUrl).toHaveBeenCalledTimes(1);
      expect(guard.router.navigateByUrl).toHaveBeenCalledWith(ROUTERS_URL.LOGIN);
    });
  });
});

describe('Login Guard', () => {
  let guard: LoginGuard;
  let mockStore: MockStore<AppState>;
  let mockUsernameSelector: MemoizedSelector<AppState, UserInfoState>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        LoginGuard,
        provideMockStore(),
        {
          provide: Router,
          useValue: { navigateByUrl: () => { } }
        }
      ],
    });

    mockStore = TestBed.get(Store);
    mockUsernameSelector = mockStore.overrideSelector(selectUserInfoState, {
      avatar: '',
      email: '',
      firstName: '',
      guid: '',
      lastName: '',
      password: '',
      isAuthenticated: false,
    });
    guard = TestBed.get<LoginGuard>(LoginGuard);
    guard.router.navigateByUrl = jasmine.createSpy();
  });

  it('should return true if the user state is not logged in', () => {
    guard.canActivate().subscribe(value => {
      expect(value).toBeTruthy();
      expect(guard.router.navigateByUrl).toHaveBeenCalledTimes(0);
    });
  });

  it('should return false if the user state is logged in', () => {
    mockUsernameSelector.setResult({
      avatar: '',
      email: '',
      firstName: '',
      guid: '',
      lastName: '',
      password: '',
      isAuthenticated: true,
    });
    guard.canActivate().subscribe(value => {
      expect(value).toBeFalsy();
      expect(guard.router.navigateByUrl).toHaveBeenCalledTimes(1);
      expect(guard.router.navigateByUrl).toHaveBeenCalledWith(ROUTERS_URL.ORGANIZATION);
    });
  });
});
