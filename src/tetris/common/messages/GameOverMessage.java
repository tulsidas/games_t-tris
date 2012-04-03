package tetris.common.messages;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;

import tetris.common.ifaz.ClientGameMessage;
import tetris.common.ifaz.GameHandler;
import tetris.common.ifaz.GameMessage;
import tetris.common.ifaz.SaloonHandler;

import common.ifaz.BasicServerHandler;
import common.messages.TaringaProtocolEncoder;
import common.messages.VariableLengthMessageAdapter;
import common.model.User;

public class GameOverMessage extends VariableLengthMessageAdapter implements
      ClientGameMessage, GameMessage {

   private User user;

   public GameOverMessage() {
   }

   public GameOverMessage(User user) {
      this.user = user;
   }

   @Override
   public void execute(IoSession session, SaloonHandler salon) {
      salon.gameOver(session);
   }

   @Override
   public void execute(IoSession session, BasicServerHandler serverHandler) {
      execute(session, (SaloonHandler) serverHandler);
   }

   @Override
   public void execute(GameHandler game) {
      game.gameOver(user);
   }

   @Override
   public byte getMessageId() {
      return (byte) 0x94;
   }

   @Override
   public void decode(ByteBuffer buff) {
      if (buff.get() == TaringaProtocolEncoder.NON_NULL) {
         user = User.readFrom(buff);
      }
   }

   @Override
   public ByteBuffer encodedContent() {
      ByteBuffer buf = ByteBuffer.allocate(32);
      buf.setAutoExpand(true);

      if (user == null) {
         buf.put(TaringaProtocolEncoder.NULL);
      }
      else {
         buf.put(TaringaProtocolEncoder.NON_NULL);
         User.writeTo(user, buf);
      }

      return buf.flip();
   }
}
