package tetris.client;

import org.apache.mina.common.IoSession;

import tetris.common.ifaz.GameHandler;
import tetris.common.ifaz.GameMessage;
import tetris.common.messages.TetrisProtocolDecoder;
import client.AbstractGameConnector;

import common.model.User;

public class GameConnector extends AbstractGameConnector implements GameHandler {

    public GameConnector(String host, int port, int salon, String user,
            String pass, long version) {
        super(host, port, salon, user, pass, version,
                new TetrisProtocolDecoder());
    }

    @Override
    public void messageReceived(IoSession sess, Object message) {
        super.messageReceived(sess, message);

        if (message instanceof GameMessage && gameHandler != null) {
            ((GameMessage) message).execute(this);
        }
    }

    // /////////////
    // GameHandler
    // /////////////
    @Override
    public void gameStart() {
        if (gameHandler != null) {
            ((GameHandler) gameHandler).gameStart();
        }
    }

    @Override
    public void boardState(byte[] state, byte lines, User user) {
        if (gameHandler != null) {
            ((GameHandler) gameHandler).boardState(state, lines, user);
        }
    }

    @Override
    public void gameOver(User user) {
        if (gameHandler != null) {
            ((GameHandler) gameHandler).gameOver(user);
        }
    }
}