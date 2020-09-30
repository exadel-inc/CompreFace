import {AppUser} from './app-user';

export interface UserDeletion {
  deleterUserId: string;
  userToDelete: AppUser;
}
