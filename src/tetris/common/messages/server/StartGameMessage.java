package tetris.common.messages.server;

import org.apache.mina.common.IoSession;

import tetris.common.ifaz.GameHandler;
import tetris.common.ifaz.GameMessage;
import tetris.common.ifaz.SaloonHandler;
import tetris.common.messages.TetrisClientGameMessage;

public class StartGameMessage extends TetrisClientGameMessage implements
      GameMessage {

   @Override
   public int getContentLength() {
      return 0;
   }

   @Override
   public byte getMessageId() {
      return (byte) 0x92;
   }

   @Override
   public void execute(GameHandler game) {
      game.gameStart();
   }

   @Override
   public void execute(IoSession session, SaloonHandler salon) {
      salon.startGame(session);
   }
   
   @Override
   public String toString() {
      return "Start Game";
   }
}
