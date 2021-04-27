/**
 * 
 */
package vlcj.test.tutorial;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent;

public class Tutorial {

    private static Tutorial thisApp;

    private final JFrame frame;
    private final EmbeddedMediaPlayerComponent mediaPlayerComponent;
    
    private final String filepath = "C:\\Gumtree\\resource";
    
    public Tutorial() {
    	frame = new JFrame("My First Media Player");
        frame.setBounds(100, 100, 600, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                mediaPlayerComponent.release();
                System.exit(0);
            }
        });
        
        mediaPlayerComponent = new EmbeddedMediaPlayerComponent();
        frame.setContentPane(mediaPlayerComponent);

        frame.setVisible(true);
        
    }

    public void play() {
    	mediaPlayerComponent.mediaPlayer().media().play(filepath);
    }
    
    public static void main(String[] args) {
        thisApp = new Tutorial();
        thisApp.play();

    }


}