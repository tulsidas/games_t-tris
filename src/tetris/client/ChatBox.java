package tetris.client;

import pulpcore.Input;
import pulpcore.image.Colors;
import pulpcore.image.CoreFont;
import pulpcore.image.CoreImage;
import pulpcore.sprite.Button;
import pulpcore.sprite.FilledSprite;
import pulpcore.sprite.Group;
import pulpcore.sprite.StretchableSprite;
import pulpcore.sprite.TextField;
import client.InGameChatArea;

public class ChatBox extends Group {
   private ChatListener listener;

   private TextField chatTF;

   private Button sendChat;

   private InGameChatArea chatArea;

   public ChatBox(ChatListener listener, int x, int y) {
      super(x, y);

      this.listener = listener;

      int b = 5;
      int w = 225;
      int h = 350;

      add(new StretchableSprite("imgs/border.9.png", 0, 0, w, h));
      add(new FilledSprite(b, b, w - b * 2, h - b * 2, Colors.WHITE));

      CoreFont font = CoreFont.load("imgs/FS.font.png");
      CoreFont fontw = font.tint(Colors.WHITE);

      add(new StretchableSprite("imgs/border.9.png", b, h - 38, w - b * 2, 30));
      chatTF = new TextField(fontw, font, "", b * 2, h - 30, w - b * 2 - 20, -1);
      chatTF.selectionColor.set(Colors.WHITE);
      chatTF.setMaxNumChars(80);
      add(chatTF);

      chatArea = new InGameChatArea(font, b, b, w - b * 2 - 3, h - b * 2 - 33);
      add(chatArea);

      sendChat = new Button(CoreImage.load("imgs/btn-send.png").split(3),
            w - 18, h - 35);
      sendChat.setKeyBinding(Input.KEY_ENTER);
      add(sendChat);
   }

   @Override
   public void update(int elapsedTime) {
      super.update(elapsedTime);

      if (sendChat.isClicked() && chatTF.getText().trim().length() > 0) {
         listener.sendChat(chatTF.getText().trim());
         chatTF.setText("");
      }
   }

   public boolean hasFocus() {
      return chatTF.hasFocus() || chatArea.hasFocus();
   }

   public void addLine(String msg) {
      chatArea.addLine(msg);
   }
}
