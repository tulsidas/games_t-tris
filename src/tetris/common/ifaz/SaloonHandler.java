package tetris.common.ifaz;

import org.apache.mina.common.IoSession;

/**
 * Interfaz de los mensajes que recibe el Saloon de los clientes
 */
public interface SaloonHandler {

   void boardState(IoSession session, byte[] state, byte removedLines);

   void gameOver(IoSession session);

   void startGame(IoSession session);
}