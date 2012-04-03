package tetris.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pulpcore.Input;
import pulpcore.Stage;
import pulpcore.animation.event.TimelineEvent;
import pulpcore.image.Colors;
import pulpcore.image.CoreFont;
import pulpcore.image.filter.DropShadow;
import pulpcore.scene.Scene;
import pulpcore.sound.Sound;
import pulpcore.sprite.Button;
import pulpcore.sprite.FilledSprite;
import pulpcore.sprite.Group;
import pulpcore.sprite.Label;
import tetris.common.ifaz.GameHandler;
import tetris.common.messages.BoardStateMessage;
import tetris.common.messages.GameOverMessage;
import tetris.common.messages.server.StartGameMessage;
import tetris.common.model.TetrisRoom;
import client.DisconnectedScene;
import client.PingScene;
import client.PulpcoreUtils;
import client.DisconnectedScene.Reason;

import common.game.AbandonRoomMessage;
import common.messages.chat.RoomChatMessage;
import common.messages.server.RoomJoinedMessage;
import common.model.AbstractRoom;
import common.model.User;

public class TetrisScene extends PingScene implements GameHandler, ChatListener {

    private GameConnector connection;

    private boolean mustDisconnect;

    private Button abandonGame, startGame;

    private ChatBox chatBox;

    private CoreFont font, din30;

    private String userName;

    private boolean inGame, creator;

    private PlayerBoard board;

    private PreviewBoard previewBoard;

    private Map<User, OponentSprite> boardOtros;

    private Figure figure, nextFigure;

    private int level, score;

    private float pushLines;

    /**
     * Time passed since last tick
     */
    private int currentTime;

    /**
     * Length of current tick, it gets shorter on harder levels
     */
    private int currentTick;

    /** Timers to delay the button presses */
    private int delayLeft, delayRight, delayDown;

    private static final int KEY_DELAY = 100;

    /**
     * The level label.
     */
    private Label scoreLabel, levelLabel, finalLabel;

    private TetrisRoom room;

    private User currentUser;

    private int color;
    
    private Sound horn;

    /** Available colors */
    private static final List<Integer> COLORS = new ArrayList<Integer>(Arrays
            .asList(Colors.RED, Colors.GREEN, Colors.ORANGE, Colors.BLUE,
                    Colors.YELLOW, Colors.PURPLE, Colors.WHITE, Colors.MAGENTA,
                    Colors.CYAN));

    private List<Integer> availableColors;

    public TetrisScene(GameConnector connection, User currentUser,
            TetrisRoom room) {
        super(connection);

        this.connection = connection;
        connection.setGameHandler(this);

        this.mustDisconnect = true;

        this.room = room;
        this.userName = currentUser.getName();
        this.creator = room.getPlayers().size() == 1;
        this.currentUser = currentUser;

        this.delayLeft = 0;
        this.delayRight = 0;
        this.delayDown = 0;

        availableColors = new ArrayList<Integer>(COLORS);
        Collections.shuffle(availableColors);

        this.color = availableColors.remove(0);

        this.boardOtros = new HashMap<User, OponentSprite>();

        this.level = room.getStartingLevel();
        this.currentTick = (int) (1000 * Math.pow(0.8, level));
    }

    @Override
    public void load() {
        // fonts
        font = CoreFont.load("imgs/DIN15.font.png").tint(Colors.WHITE);
        din30 = CoreFont.load("imgs/DIN30.font.png").tint(Colors.WHITE);

        add(new FilledSprite(Colors.rgb(0x0d8ee8)));

        // the other ones
        for (User u : room.getPlayers()) {
            if (!u.getName().equals(userName)) {
                roomJoined(room, u);
            }
        }

        // the board, with the current # of players
        board = new PlayerBoard(30, 30);
        add(board);

        previewBoard = new PreviewBoard(192, 30);
        add(previewBoard);

        add(new Label(font.tint(color), currentUser.getName(), 180, 10));

        // chatbox above the board so we can still chat even if it's huge
        chatBox = new ChatBox(this, 480, 80);
        Group h = new Group();
        h.add(chatBox);
        addLayer(h);

        levelLabel = new Label(font, "Nivel " + level, 120, 10);
        add(levelLabel);

        scoreLabel = new Label(font, "Puntos: 0", 10, 10);
        add(scoreLabel);

        // layer más arriba del resto
        Group g = new Group();
        finalLabel = new Label(din30, "", 0, 180);
        finalLabel.setFilter(new DropShadow(3, 3, Colors.BLACK));
        finalLabel.visible.set(false);
        g.add(finalLabel);
        addLayer(g);

        startGame = Button.createLabeledButton("¡Empezar!", 290, 5);
        if (creator) {
            // add start game
            add(startGame);
        }

        // abandon button
        abandonGame = Button.createLabeledButton("Salir", 590, 5);
        add(abandonGame);
        
        horn = Sound.load("sfx/horn.ogg");

        // let the server know that we are in the game room
        connection.send(new RoomJoinedMessage());
    }

    public void unload() {
        if (mustDisconnect) {
            connection.disconnect();
        }
    }

    @Override
    public void update(int elapsedTime) {
        super.update(elapsedTime);

        if (startGame.isClicked()) {
            startGame.enabled.set(false);
            connection.send(new StartGameMessage());
        }

        if (inGame) {
            this.currentTime += elapsedTime;

            if (currentTime >= currentTick) {
                currentTime = 0;
                if (figure.isAllWayDown()) {
                    handleFigureBottom();
                }
                else {
                    figure.moveDown();
                }
            }

            // update key delays
            if (delayLeft > 0) {
                delayLeft -= elapsedTime;
            }
            if (delayRight > 0) {
                delayRight -= elapsedTime;
            }
            if (delayDown > 0) {
                delayDown -= elapsedTime;
            }

            // Handle remaining key events
            if (Input.isDown(Input.KEY_LEFT) && delayLeft <= 0) {
                figure.moveLeft();
                delayLeft = KEY_DELAY;
            }
            else if (Input.isDown(Input.KEY_RIGHT) && delayRight <= 0) {
                figure.moveRight();
                delayRight = KEY_DELAY;
            }
            else if (Input.isDown(Input.KEY_DOWN) && delayDown <= 0) {
                if (figure.isAllWayDown()) {
                    handleFigureBottom();
                }
                else {
                    figure.moveDown();
                }
                delayDown = KEY_DELAY;
            }
            else if (Input.isReleased(Input.KEY_SPACE)) {
                figure.moveAllWayDown();
                handleFigureBottom();
            }
            else if (Input.isReleased(Input.KEY_UP)
                    || Input.isReleased(Input.KEY_Z)) {
                figure.rotateClockwise();
            }
            else if (Input.isReleased(Input.KEY_X)) {
                figure.rotateCounterClockwise();
            }
        }

        if (abandonGame.enabled.get() && abandonGame.isClicked()) {
            abandonGame();
        }
    }

    public void sendChat(String msg) {
        chatBox.addLine(userName + ": " + msg);
        connection.send(new RoomChatMessage(msg));
    }

    @Override
    public void incomingChat(final User from, final String msg) {
        invokeLater(new Runnable() {
            public void run() {
                // tic.play();

                chatBox.addLine(from.getName() + ": " + msg);
            }
        });
    }

    private final void setScene(final Scene s) {
        mustDisconnect = false;
        Stage.setScene(s);
    }

    private void abandonGame() {
        // envio abandono
        connection.send(new AbandonRoomMessage());

        invokeLater(new Runnable() {
            public void run() {
                // me rajo al lobby
                setScene(new LobbyScene(currentUser, connection));
            }
        });
    }

    // /////////////////////
    // Board & Game methods
    // /////////////////////
    private void handleStart() {
        // Reset score and figures
        inGame = true;

        board.gameStart();
        previewBoard.gameStart();

        finalLabel.visible.set(false);

        figure = randomFigure();
        figure.attach(board, false);

        nextFigure = randomFigure();
        nextFigure.attach(previewBoard, true);
        
        horn.play();
    }

    /**
     * Handles a level modification event. This will modify the level label and
     * adjust the thread speed.
     */
    private void handleLevelModification() {
        levelLabel.setText("Nivel " + level);
        currentTick *= 0.8;
    }

    private void handleScoreModification() {
        scoreLabel.setText("Puntos: " + score);
    }

    // /////////////
    // GameHandler
    // /////////////
    public void gameStart() {
        invokeLater(new Runnable() {
            public void run() {
                // move and dissapear chat box
                chatBox.enabled.set(false);
                chatBox.x.animateTo(Stage.getWidth() + 50, 1000);

                remove(startGame);

                handleStart();
            }
        });
    }

    @Override
    public void boardState(byte[] state, byte removedLines, User user) {
        boardOtros.get(user).setState(state);
        if (removedLines > 1) {
            pushLines(removedLines);
        }
    }

    private void pushLines(int lines) {
        float players = (float) (boardOtros.size() + 1);
        pushLines += lines / players;

        if (pushLines >= 1) {
            int l = (int) Math.floor(pushLines);
            board.pushLines(l);
            pushLines -= l;
        }
    }

    private void handleFigureBottom() {
        figure.fix();

        // 1 points for placing a figure
        score += 1;

        byte removedLines = (byte) board.removeFullLines();

        // 10, 20, 40, 80
        score += 10 * (int) (Math.pow(2, removedLines - 1));

        handleScoreModification();

        if (score / 150 > level) {
            level++;
            handleLevelModification();
        }

        // mando mi estado
        connection.send(new BoardStateMessage(board.getState(), removedLines));

        figure = nextFigure;
        boolean attached = figure.attach(board, false);

        if (!attached) {
            inGame = false;
            board.setGameOver(true);

            // recupero el chat box
            chatBox.enabled.set(true);
            chatBox.x.animateTo(480, 1000);

            connection.send(new GameOverMessage());
        }
        else {
            nextFigure = randomFigure();
            previewBoard.clear();
            nextFigure.attach(previewBoard, true);
        }
    }

    private Figure randomFigure() {
        return new Figure((int) (1 + (Math.random() * 7)));
    }

    @Override
    public void disconnected() {
        invokeLater(new Runnable() {
            public void run() {
                Stage.setScene(new DisconnectedScene(Reason.FAILED));
            }
        });
    }

    @Override
    public void finJuego(boolean victoria) {
        if (victoria) {
            for (OponentSprite os : boardOtros.values()) {
                os.fill();
            }
            finalLabel.setText("¡Ganaste! ¡Capo!");
        }
        else {
            board.setGameOver(true);
            finalLabel.setText("¡Perdiste, mequetrefe!");

            // recupero el chat box
            chatBox.enabled.set(true);
            chatBox.x.animateTo(430, 1000);
        }

        finalLabel.visible.set(true);
        PulpcoreUtils.centerSprite(finalLabel, 235, 319);

        inGame = false;

        // al lobby
        mustDisconnect = false;
        addEvent(new TimelineEvent(5000) {
            @Override
            public void run() {
                abandonGame();
            }
        });
    }

    @Override
    public void newGame() {
    }

    @Override
    /**
     * user perdio
     */
    public void gameOver(User user) {
        boardOtros.get(user).fill();
    }

    @Override
    public void oponenteAbandono(final boolean enJuego, final User user) {
        invokeLater(new Runnable() {
            public void run() {
                if (enJuego) {
                    // griso
                    boardOtros.get(user).gray();
                }
                else {
                    OponentSprite os = boardOtros.remove(user);

                    // repongo el color
                    availableColors.add(os.getColor());

                    remove(os);

                    // reorganizo los otros tableros
                    int i = 0;
                    for (OponentSprite otro : boardOtros.values()) {
                        otro.x.animateTo(X_OPONENTE[i], 500);
                        otro.y.animateTo(Y_OPONENTE[i], 500);
                        i++;
                    }

                    // soy el game owner?
                    List<User> roomPlayers = room.getPlayers();
                    if (roomPlayers.size() == 2
                            && roomPlayers.get(0).equals(user)
                            && roomPlayers.get(1).equals(currentUser)) {
                        creator = true;
                        // add start game
                        add(startGame);
                    }
                }
            }
        });
    }

    private final int[] X_OPONENTE = { 210, 210, 280, 280, 350, 350, 420, 420 };

    private final int[] Y_OPONENTE = { 130, 280, 130, 280, 130, 280, 130, 280 };

    @Override
    public void roomJoined(AbstractRoom room, final User user) {
        invokeLater(new Runnable() {
            public void run() {
                int color = availableColors.remove(0);

                int numOponentes = boardOtros.size();

                OponentSprite os = new OponentSprite(X_OPONENTE[numOponentes],
                        Y_OPONENTE[numOponentes], user, color);
                boardOtros.put(user, os);
                add(os);
            }
        });
    }

    @Override
    public void startGame(boolean start) {
        handleStart();
    }

    @Override
    public void updatePoints(int puntos) {
    }
}

// TODO si me uno a una sala CREO que me está uniendo cuando hago click y no
// cuando entré posta a la sala entonces hay discrepancias