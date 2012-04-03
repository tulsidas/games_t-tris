package tetris.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.mina.common.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import server.AbstractServerRoom;
import server.db.RedisManager;
import tetris.common.game.FinJuegoMessage;
import tetris.common.messages.BoardStateMessage;
import tetris.common.messages.GameOverMessage;
import tetris.common.messages.server.StartGameMessage;
import tetris.common.model.TetrisRoom;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import common.game.OponentAbandonedMessage;
import common.model.AbstractRoom;
import common.model.User;

public class TetrisServerRoom extends AbstractServerRoom {

    private int startingLevel;

    private List<PlayerData> players;

    private List<PlayerData> jugando;

    private int bolsa; // lo que se lleva el ganador

    private Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * @param player
     *            el jugador creador
     * @param puntos
     *            los puntos apostados
     */
    public TetrisServerRoom(TetrisSaloon salon, IoSession player, int puntos,
            int startingLevel) {
        super(salon, puntos);

        this.startingLevel = startingLevel;

        players = Collections.synchronizedList(new ArrayList<PlayerData>(2));
        players.add(new PlayerData(player));
    }

    @Override
    public AbstractRoom createRoom() {
        return new TetrisRoom(getId(), puntosApostados, startingLevel,
                getUsers());
    }

    @Override
    public void abandon(IoSession session) {
        synchronized (players) {
            PlayerData pd = getPlayerData(session);
            players.remove(pd);
            pd.cancelTimer();

            multicast(new OponentAbandonedMessage(isEnJuego(), saloon
                    .getUser(session)), session);

            if (isEnJuego()) {
                jugando.remove(pd);

                // si quedo uno solo gameover
                if (jugando.size() == 1) {
                    setEnJuego(false);

                    PlayerData ganador = jugando.get(0);
                    ganador.cancelTimer();

                    // transfiero puntines
                    log.info(bolsa + " fichas a "
                            + saloon.getUser(ganador.session) + "(abandono)");
                    RedisManager.darPuntos(saloon.getUser(ganador.session),
                            bolsa);

                    ganador.session.write(new FinJuegoMessage(true));
                }
            }
        }
    }

    @Override
    public Collection<IoSession> getUserSessions() {
        return Lists.newArrayList(Iterables.transform(players,
                new Function<PlayerData, IoSession>() {
                    @Override
                    public IoSession apply(PlayerData pd) {
                        return pd.session;
                    }
                }));
    }

    @Override
    public boolean isComplete() {
        return players.size() == 9;
    }

    @Override
    public boolean isGameOn() {
        return isStarted();
    }

    @Override
    public boolean join(IoSession session) {
        synchronized (players) {
            if (isComplete() || isStarted()) {
                return false;
            }
            else {
                players.add(new PlayerData(session));
                return true;
            }
        }
    }

    @Override
    public void proximoJuego(IoSession session, boolean acepta) {
        // no se usa
    }

    @Override
    public void startGame() {
        jugando = Collections.synchronizedList(new ArrayList<PlayerData>(
                players));

        setEnJuego(true);
        setStarted(true);

        bolsa = puntosApostados * players.size();

        // empieza el juego
        multicast(new StartGameMessage());

        // mientras, sacamos puntos
        if (players.size() > 1 && puntosApostados > 0) {
            log.info("saco " + puntosApostados + " a " + players);
            RedisManager.sacarPuntos(Iterables.transform(players,
                    new Function<PlayerData, User>() {
                        @Override
                        public User apply(PlayerData pd) {
                            return saloon.getUser(pd.session);
                        }
                    }), puntosApostados);
        }

        // cheat check
        for (PlayerData pd : jugando) {
            pd.level = startingLevel;
            pd.cheatCheck();
        }
    }

    public void boardState(IoSession session, byte[] state, byte removedLines) {
        multicast(new BoardStateMessage(state, removedLines, saloon
                .getUser(session)), session);

        // // //
        // QA //
        // // //
        try {
            PlayerData pd = getPlayerData(session);
            pd.updateScore(removedLines);
        }
        catch (NoSuchElementException nsee) {
            // System.out.println("boardState de " );
        }
    }

    private PlayerData getPlayerData(final IoSession session) {
        return Iterables.find(players, new Predicate<PlayerData>() {
            @Override
            public boolean apply(PlayerData it) {
                return it.session == session;
            }
        });
    }

    public synchronized void gameOver(IoSession session) {
        synchronized (jugando) {
            PlayerData pd = getPlayerData(session);
            jugando.remove(pd);
            pd.cancelTimer();

            if (jugando.size() > 1) {
                multicast(new GameOverMessage(saloon.getUser(session)), session);
            }
            else if (jugando.size() == 1) {
                setEnJuego(false);

                PlayerData ganador = jugando.get(0);
                ganador.cancelTimer();

                // si queda uno solo transferir puntos y avisarle que gano
                log.info(bolsa + " fichas a " + saloon.getUser(ganador.session)
                        + "(gameOver)");
                RedisManager.darPuntos(saloon.getUser(ganador.session), bolsa);

                ganador.session.write(new FinJuegoMessage(true));
                multicast(new FinJuegoMessage(false), ganador.session);
            }
            else {
                // jugando.size() == 0
                // jugaba solo
                session.write(new FinJuegoMessage(false));
            }
        }
    }

    @Override
    public int getMinimumPlayers() {
        return 1;
    }

    @Override
    public int getMinimumPlayingPlayers() {
        return 1;
    }

    @Override
    public void joined(IoSession session) {
    }

    private class PlayerData {
        IoSession session;

        private int score, level;

        private Timer timer;

        private class KickTask extends TimerTask {
            @Override
            public void run() {
                if (jugando.contains(session)) {
                    saloon.abandonGame(session);
                }
            }
        };

        public PlayerData(IoSession session) {
            this.session = session;
            this.score = 0;
        }

        public void cancelTimer() {
            if (timer != null) {
                timer.cancel();
            }
        }

        public void updateScore(int lines) {
            score += 1 + (10 * (int) (Math.pow(2, lines - 1)));

            if (score / 150 > level) {
                level++;
            }

            cheatCheck();
        }

        public void cheatCheck() {
            if (timer != null) {
                // System.out.println("se reportó a tiempo...");
                timer.cancel();
            }

            timer = new Timer();
            // 5 seg de changui
            int time = 5000 + 20 * (int) (1000 * Math.pow(0.8, level));
            // System.out.println("level " + level + ", score " + score
            // + ", timeout " + time);
            timer.schedule(new KickTask(), time);
        }

        @Override
        public int hashCode() {
            return session.getRemoteAddress().hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            PlayerData other = (PlayerData) obj;
            if (session == null) {
                if (other.session != null) {
                    return false;
                }
            }
            else if (!session.equals(other.session)) {
                return false;
            }
            return true;
        }
    }
}