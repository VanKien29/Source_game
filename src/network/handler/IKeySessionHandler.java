package network.handler;

import network.io.Message;
import network.session.ISession;

public interface IKeySessionHandler {
  void sendKey(ISession paramISession);
  
  void setKey(ISession paramISession, Message paramMessage) throws Exception;
}
