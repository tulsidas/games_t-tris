package tetris.common.messages;

import org.apache.mina.common.IoSession;

import tetris.common.ifaz.ClientGameMessage;
import tetris.common.ifaz.SaloonHandler;

import common.ifaz.BasicServerHandler;
import common.messages.FixedLengthMessageAdapter;

public abstract class TetrisClientGameMessage extends FixedLengthMessageAdapter
        implements ClientGameMessage {
    public abstract void execute(IoSession session, SaloonHandler salon);

    public void execute(IoSession session, BasicServerHandler serverHandler) {
        execute(session, (SaloonHandler) serverHandler);
    }
}