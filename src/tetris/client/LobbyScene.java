package tetris.client;

import pulpcore.image.CoreFont;
import pulpcore.image.CoreImage;
import pulpcore.scene.Scene;
import pulpcore.sprite.ImageSprite;
import pulpcore.sprite.Sprite;
import tetris.common.messages.client.CreateTetrisRoomMessage;
import tetris.common.model.TetrisRoom;
import client.AbstractGameConnector;
import client.AbstractLobbyScene;
import client.Spinner;

import common.messages.Message;
import common.model.AbstractRoom;
import common.model.User;

/**
 * @author Tulsi
 */
public class LobbyScene extends AbstractLobbyScene {

   private Spinner startingLevel;

   public LobbyScene(User user, AbstractGameConnector connection) {
      super(user, connection);
   }

   @Override
   public void load() {
      super.load();

      // spinner
      CoreImage[] up = CoreImage.load("imgs/btn-puntos.png").split(3);
      CoreFont din18numeric = CoreFont.load("imgs/DIN18_numeric.font.png");

      startingLevel = new Spinner(90, 400, 30, 25, din18numeric, up);
      startingLevel.setValue(1);
      startingLevel.setMinValue(1);
      startingLevel.setMaxValue(9);
      startingLevel.setMaxNumChars(1);

      add(new ImageSprite(CoreImage.load("imgs/nivel.png"), 90, 385));
      add(startingLevel);
   }

   @Override
   protected boolean puedeCrearSala() {
      return startingLevel.getValue() > 0 && startingLevel.getValue() <= 9;
   }

   @Override
   protected Scene getGameScene(AbstractGameConnector connection, User usr,
         AbstractRoom room) {
      return new TetrisScene((GameConnector) connection, usr, (TetrisRoom) room);
   }

   @Override
   protected Sprite getGameImage() {
      return new ImageSprite("imgs/logo-tetris.png", 495, 10);
   }

   @Override
   protected Message createRoomMessage(int value) {
      return new CreateTetrisRoomMessage(value, startingLevel.getValue());
   }
}
