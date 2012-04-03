package tetris.common.messages;

import tetris.common.game.FinJuegoMessage;
import tetris.common.messages.client.CreateTetrisRoomMessage;
import tetris.common.messages.server.StartGameMessage;

import common.messages.TaringaProtocolDecoder;

public class TetrisProtocolDecoder extends TaringaProtocolDecoder {

   public TetrisProtocolDecoder() {
      classes.put(new FinJuegoMessage().getMessageId(), FinJuegoMessage.class);
      classes.put(new CreateTetrisRoomMessage().getMessageId(),
            CreateTetrisRoomMessage.class);
      classes
            .put(new StartGameMessage().getMessageId(), StartGameMessage.class);
      classes.put(new BoardStateMessage().getMessageId(),
            BoardStateMessage.class);
      classes.put(new GameOverMessage().getMessageId(), GameOverMessage.class);
   }
}
