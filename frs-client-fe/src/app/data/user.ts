export interface User {
  accountNonExpired?: boolean;
  accountNonLocked?: boolean;
  credentialsNonExpired?: boolean;
  email?: string;
  enabled?: boolean;
  firstName?: string;
  id?: number;
  lastName?: string;
  password?: string;
  // userAppRoles?: Array<UserAppRoleDto>;
  // userOrganizationRoles?: Array<UserOrganizationRoleDto>;
  username?: string;
}
