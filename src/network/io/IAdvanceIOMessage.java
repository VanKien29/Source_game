package network.io;

import java.awt.image.BufferedImage;
import java.io.IOException;

public interface IAdvanceIOMessage extends IMessage {
  BufferedImage readImage() throws IOException;
  
  void writeImage(BufferedImage paramBufferedImage, String paramString) throws IOException;
}
