package network.server;

public interface InHOANDZServer extends Runnable {
  InHOANDZServer init();

  InHOANDZServer start(int paramInt) throws Exception;

  InHOANDZServer setAcceptHandler(ISessionAcceptHandler paramISessionAcceptHandler);

  InHOANDZServer close();

  InHOANDZServer dispose();

  InHOANDZServer randomKey(boolean paramBoolean);

  InHOANDZServer setDoSomeThingWhenClose(IServerClose paramIServerClose);

  InHOANDZServer setTypeSessioClone(Class paramClass) throws Exception;

  ISessionAcceptHandler getAcceptHandler() throws Exception;

  boolean isRandomKey();

  void stopConnect();
}