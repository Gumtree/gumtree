package au.gov.ansto.bragg.koala.ui.mjpeg;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

public interface IMjpegPanel {
//    void setBufferedImage(BufferedImage image);
    void repaint();
    void setFailedString(String s);
    void setImageBytes(byte[] imageBytes);
    void dispose();
    void start();
    void stop();
}