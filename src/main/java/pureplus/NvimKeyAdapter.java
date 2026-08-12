package pureplus;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class NvimKeyAdapter implements KeyListener
{
    NvimApi   api;

    public void keyTyped(KeyEvent evt) {
        String  keycode = null;
        char    keychar = evt.getKeyChar();

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

        if ((evt.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0) {
            switch (evt.getKeyCode()) {
            case KeyEvent.VK_A:
                keycode = "C-A"; break;
            }
        } else {
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

    public NvimKeyAdapter(NvimApi api) {
        this.api = api;
    }
}
