package tetris.client;

import common.model.User;

import pulpcore.image.Colors;
import pulpcore.image.CoreFont;
import pulpcore.sprite.Group;
import pulpcore.sprite.Label;

public class OponentSprite extends Group {

   private static CoreFont font = CoreFont.load("imgs/DIN15.font.png").tint(
         Colors.WHITE);

   private OponentBoard board;

   private Label label;

   private int color;

   private User user;

   public OponentSprite(int x, int y, User user, int color) {
      super(x, y);

      this.user = user;

      board = new OponentBoard(0, 15);
      board.gameStart();

      add(board);

      label = new Label(font.tint(color), getTruncatedName(), 0, 0);
      add(label);
   }

   public void setState(byte[] state) {
      board.setState(state);
   }

   public void fill() {
      fill(color);
   }

   public void fill(int c) {
      board.fill(c);
   }

   public void gray() {
      board.fill(Colors.DARKGRAY);
      label.getFont().tint(Colors.DARKGRAY);
   }

   public int getColor() {
      return color;
   }

   private String getTruncatedName() {
      // truncate if name too large
      String name = user.getName();

      int length = font.getStringWidth(name);
      while (length > 95) {
         name = name.substring(0, name.length() - 1);
         length = font.getStringWidth(name);
      }

      return name;
   }

   private class OponentBoard extends BoardSprite {

      private OponentBoard(int x, int y) {
         super(x, y, 8, 20, 5);
      }
   }
}
