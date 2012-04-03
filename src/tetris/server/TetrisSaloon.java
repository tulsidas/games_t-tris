package tetris.server;

import org.apache.mina.common.IoSession;

import server.AbstractSaloon;
import tetris.common.ifaz.SaloonHandler;
import tetris.common.ifaz.TetrisServerHandler;

import common.ifaz.POSTHandler;
import common.messages.server.GameStartedMessage;

public class TetrisSaloon extends AbstractSaloon implements
        TetrisServerHandler, SaloonHandler {

    public TetrisSaloon(int id, POSTHandler poster) {
        super(id, poster);
    }

    @Override
    public void createRoom(IoSession session, int puntos) {
        // XXX no se usa?
        throw new RuntimeException();
    }

    @Override
    public void createRoom(IoSession session, int puntos, int startingLevel) {
        TetrisServerRoom tsr = new TetrisServerRoom(this, session, puntos,
                startingLevel);

        createRoom(session, puntos, tsr);
    }

    // //////

    @Override
    protected TetrisServerRoom getRoom(IoSession session) {
        return (TetrisServerRoom) super.getRoom(session);
    }

    @Override
    public void boardState(IoSession session, byte[] state, byte removedLines) {
        TetrisServerRoom room = getRoom(session);
        if (room != null) {
            room.boardState(session, state, removedLines);
        }
    }

    @Override
    public void startGame(IoSession session) {
        TetrisServerRoom room = getRoom(session);
        room.startGame();

        // aviso al lobby que empezo el juego en esta sala
        broadcastLobby(new GameStartedMessage(room.getId()));
    }

    @Override
    public void gameOver(IoSession session) {
        getRoom(session).gameOver(session);
    }
}