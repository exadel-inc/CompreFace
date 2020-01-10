export interface IFacade {
  initSubscriptions: () => void;
  unsubscribe: () => void;
}
