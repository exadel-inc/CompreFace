import {AppUser} from './appUser';

export interface UserDeletion {
  deleterUserId: string;
  userToDelete: AppUser;
}
