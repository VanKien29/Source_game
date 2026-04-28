/*    */ package network.example;
/*    */ 
/*    */ import network.handler.IMessageHandler;
/*    */ import network.io.Message;
/*    */ import network.session.ISession;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ public class MessageHandler
/*    */   implements IMessageHandler
/*    */ {
/*    */   public void onMessage(ISession session, Message msg) throws Exception {
/* 16 */     System.out.println(msg.reader().readUTF());
/* 17 */     msg.cleanup();
/*    */   }
/*    */ }
