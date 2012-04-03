package tetris.common.ifaz;

import org.apache.mina.common.IoSession;

import common.ifaz.BasicServerHandler;

public interface TetrisServerHandler extends BasicServerHandler {
   void createRoom(IoSession session, int puntos, int startingLevel);
}
