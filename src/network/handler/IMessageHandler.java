package network.handler;

import network.io.Message;
import network.session.ISession;

public interface IMessageHandler {
  void onMessage(ISession paramISession, Message paramMessage) throws Exception;
}
