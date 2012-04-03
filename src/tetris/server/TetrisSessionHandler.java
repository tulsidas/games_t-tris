package tetris.server;

import server.ServerSessionHandler;
import tetris.common.messages.TetrisProtocolDecoder;

import com.google.common.collect.Lists;

public class TetrisSessionHandler extends ServerSessionHandler {

   public TetrisSessionHandler() {
      super(new TetrisProtocolDecoder());

      salones = Lists.newArrayList();
      salones.add(new TetrisSaloon(0, this));
      salones.add(new TetrisSaloon(1, this));
      salones.add(new TetrisSaloon(2, this));
   }

   @Override
   protected int getCodigoJuego() {
      // tetris = 6 para la base
      return 6;
   }
}