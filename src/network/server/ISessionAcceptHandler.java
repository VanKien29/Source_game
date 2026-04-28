package network.server;

import network.session.ISession;

public interface ISessionAcceptHandler {
  void sessionInit(ISession paramISession);
  
  void sessionDisconnect(ISession paramISession);
}