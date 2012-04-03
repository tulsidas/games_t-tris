package tetris.common.messages.client;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;

import tetris.common.ifaz.TetrisServerHandler;

import common.ifaz.BasicClientGameMessage;
import common.ifaz.BasicServerHandler;
import common.messages.client.CreateRoomMessage;

/**
 * Pedido de un usuario de crear una sala
 */
public class CreateTetrisRoomMessage extends CreateRoomMessage implements
      BasicClientGameMessage {

   private int startingLevel;

   public CreateTetrisRoomMessage() {
   }

   public CreateTetrisRoomMessage(int puntos, int startingLevel) {
      super(puntos);
      this.startingLevel = startingLevel;
   }

   @Override
   public void execute(IoSession session, BasicServerHandler server) {
      ((TetrisServerHandler) server).createRoom(session, puntos, startingLevel);
   }

   @Override
   public String toString() {
      return "Create Room (" + puntos + " pts), nivel " + startingLevel;
   }

   @Override
   public int getContentLength() {
      return super.getContentLength() + 1;
   }

   @Override
   public void decode(ByteBuffer buff) {
      super.decode(buff);
      startingLevel = buff.get();
   }

   @Override
   protected void encodeContent(ByteBuffer buff) {
      super.encodeContent(buff);
      buff.put((byte) startingLevel);
   }

   @Override
   public byte getMessageId() {
      return (byte) 0x91;
   }
}
