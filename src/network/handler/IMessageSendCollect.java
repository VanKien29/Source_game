package network.handler;

import network.io.Message;
import network.session.ISession;
import java.io.DataInputStream;
import java.io.DataOutputStream;

public interface IMessageSendCollect {
  Message readMessage(ISession paramISession, DataInputStream paramDataInputStream) throws Exception;
  
  byte readKey(ISession paramISession, byte paramByte);
  
  void doSendMessage(ISession paramISession, DataOutputStream paramDataOutputStream, Message paramMessage) throws Exception;
  
  byte writeKey(ISession paramISession, byte paramByte);
}
