package tetris.common.model;

import java.util.List;

import org.apache.mina.common.ByteBuffer;

import common.model.AbstractRoom;
import common.model.User;
import common.util.StringUtil;

public class TetrisRoom extends AbstractRoom {
    private int startingLevel;

    public TetrisRoom() {
    }

    public TetrisRoom(int id, int puntosApostados, int startingLevel,
            List<User> players) {
        super(id, puntosApostados, players);
        this.startingLevel = startingLevel;
    }

    @Override
    public boolean isFull() {
        return getPlayers().size() == 9; // 9 es el maximo
    }

    public int getStartingLevel() {
        return startingLevel;
    }

    @Override
    public String getDisplayText() {
        StringBuilder ret = new StringBuilder("[");
        List<User> players = getPlayers();
        if (players.size() > 0) {
            ret.append(StringUtil.truncate(players.get(0).getName(), 20));
        }
        if (players.size() > 1) {
            ret.append(" + " + (players.size() - 1));
        }
        ret.append("] x" + getPuntosApostados() + ", niv " + startingLevel);

        return ret.toString();
    }

    @Override
    public ByteBuffer encode() {
        ByteBuffer parent = super.encode();

        ByteBuffer ret = ByteBuffer.allocate(parent.remaining() + 4);
        ret.put(parent);
        ret.putInt(startingLevel);

        return ret.flip();
    }

    @Override
    public void decode(ByteBuffer buff) {
        super.decode(buff);

        startingLevel = buff.getInt();
    }

    @Override
    public int compareTo(AbstractRoom o) {
        if (o.isStarted() == isStarted()) {
            return getPlayers().size() - o.getPlayers().size();
        }
        else if (o.isStarted() && !isStarted()) {
            return 1;
        }
        else {
            return -1;
        }
    }
}