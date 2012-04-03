package tetris.common.ifaz;

import common.ifaz.BasicGameHandler;
import common.model.User;

public interface GameHandler extends BasicGameHandler {

   void gameStart();

   void boardState(byte[] state, byte removedLines, User user);

   void gameOver(User user);
}
