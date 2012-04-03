package tetris.common.messages;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;

import tetris.common.ifaz.ClientGameMessage;
import tetris.common.ifaz.GameHandler;
import tetris.common.ifaz.GameMessage;
import tetris.common.ifaz.SaloonHandler;

import common.ifaz.BasicServerHandler;
import common.messages.VariableLengthMessageAdapter;
import common.model.User;

public class BoardStateMessage extends VariableLengthMessageAdapter implements
        ClientGameMessage, GameMessage {

    private byte[] state;

    private byte removedLines;

    private User user;

    public BoardStateMessage() {
    }

    public BoardStateMessage(byte[] state, byte removedLines) {
        this(state, removedLines, null);
    }

    public BoardStateMessage(byte[] state, byte removedLines, User user) {
        this.state = state;
        this.removedLines = removedLines;
        this.user = user;
    }

    @Override
    public void execute(IoSession session, SaloonHandler salon) {
        salon.boardState(session, state, removedLines);
    }

    @Override
    public void execute(IoSession session, BasicServerHandler serverHandler) {
        execute(session, (SaloonHandler) serverHandler);
    }

    @Override
    public void execute(GameHandler game) {
        game.boardState(state, removedLines, user);
    }

    @Override
    public byte getMessageId() {
        return (byte) 0x93;
    }

    @Override
    public String toString() {
        return "Board State";
    }

    @Override
    public void decode(ByteBuffer buff) {
        state = new byte[20];
        buff.get(state, 0, 20);

        removedLines = buff.get();

        user = User.readFrom(buff);
    }

    @Override
    public ByteBuffer encodedContent() {
        ByteBuffer buf = ByteBuffer.allocate(32);
        buf.setAutoExpand(true);

        buf.put(state);
        buf.put(removedLines);
        User.writeTo(user, buf);

        return buf.flip();
    }
}
