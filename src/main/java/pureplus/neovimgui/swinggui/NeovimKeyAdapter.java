package pureplus.neovimgui.swinggui;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import pureplus.neovimgui.neovimif.NeovimApi;

public class NeovimKeyAdapter implements KeyListener
{
    NeovimApi   api;

    public void keyTyped(KeyEvent evt) {
        String  keycode = null;
        char    keychar = evt.getKeyChar();

        //System.out.println("keyTyped");
        switch (keychar) {
        case '<':
            keycode = "<lt>"; break;
        case '>':
            keycode = "<gt>"; break;
        case '\n':
            keycode = "<Enter>"; break;
        default:
            keycode = String.valueOf(keychar);
            break;
        }

        try {
            api.input(keycode);
        } catch(java.io.IOException ex) {
            ex.printStackTrace();
        }
    }

    public void keyReleased(KeyEvent evt) {
        String  keycode = null;

        //System.out.println("keyReleased: "+evt.getKeyCode());
        switch (evt.getKeyCode()) {
        case KeyEvent.VK_UP:
            keycode = "<Up>"; break;
        case KeyEvent.VK_DOWN:
            keycode = "<Down>"; break;
        case KeyEvent.VK_LEFT:
            keycode = "<Down>"; break;
        case KeyEvent.VK_RIGHT:
            keycode = "<Down>"; break;
        default:
	        //not use
            keycode = null;
            break;
        }

        if (keycode!=null) {
            try {
                api.input(keycode);
            } catch(java.io.IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void keyPressed(KeyEvent evt) {}

    public NeovimKeyAdapter(NeovimApi api) {
        this.api = api;
    }
}
