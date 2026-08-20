package pureplus.neovimgui.swinggui;

import java.awt.event.InputMethodListener;
import java.awt.event.InputMethodEvent;
import java.awt.im.InputMethodRequests;
import java.awt.font.TextHitInfo;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.text.AttributedCharacterIterator;

import pureplus.neovimgui.neovimif.NeovimApi;
import pureplus.neovimgui.neovimif.NeovimDrawModel;

import java.awt.FontMetrics;

public class NeovimInputMethodRequests implements InputMethodRequests, InputMethodListener
{
    private NeovimView view;
    private NeovimDrawModel model;
    private NeovimApi api;
    private String  currentText;

    public NeovimInputMethodRequests(NeovimView view, NeovimDrawModel model, NeovimApi api) {
        this.view = view;
        this.model = model;
        this.api = api;
    }

    @Override
    public void inputMethodTextChanged(InputMethodEvent event) {
        // Handle text input method changes if needed
        //System.out.println("Input method text changed: " + event);
        
        AttributedCharacterIterator text = event.getText();
        if (text != null) {
            try {
                StringBuilder sb = new StringBuilder();
                int committedCount = event.getCommittedCharacterCount();

                int count = 0;
                for (char c = text.first(); c != AttributedCharacterIterator.DONE; c = text.next()) {
                    // Process the committed character 'c' as needed
                    if (count < committedCount) {
                        // This character is committed
                        api.input(String.valueOf(c));
                    }
                    sb.append(c);
                    count++;
                
                    //System.out.println("Committed character: " + c);
                }
                currentText = sb.toString();
                if (committedCount == sb.length()) currentText = null;
            } catch (java.io.IOException ex) {
                ex.printStackTrace();
                currentText = null;
            }
        } else {
            currentText = null;
        }
        view.repaint();
        //System.out.println("Current text: " + currentText);
    }

    @Override
    public void caretPositionChanged(InputMethodEvent event) {
        // Handle caret position changes if needed
        //System.out.println("Caret position changed: " + event);
    }

    @Override
    public Rectangle getTextLocation(TextHitInfo offset) {
        // Return the location of the text for input method purposes
        if (model.getModeName().equals("insert") || model.getModeName().equals("replace")) {
            Rectangle cursorBounds = view.getCursorBoundsFromDisplay();
            if (cursorBounds == null) {
                cursorBounds = new Rectangle(0, 0, 0, 0);
            }
            return cursorBounds;
        } else {
            return new Rectangle(0, 0, 0, 0);
        }

    }

    @Override
    public TextHitInfo getLocationOffset(int x, int y) {
        System.out.println("getLocationOffset called with coordinates: (" + x + ", " + y + ")");
        // Return the text hit info for the given location
        return null;
    }

    @Override
    public int getInsertPositionOffset() {
        // Return the offset of the insertion position
        return 0;
    }

    @Override
    public AttributedCharacterIterator getCommittedText(int beginIndex, int endIndex, AttributedCharacterIterator.Attribute[] attributes) {
        // Return the committed text for the given range
        return new java.text.AttributedString("").getIterator();
    }

    @Override
    public int getCommittedTextLength() {
        // Return the length of the committed text
        return 0;
    }

    @Override
    public AttributedCharacterIterator cancelLatestCommittedText(AttributedCharacterIterator.Attribute[] attributes) {
        // Cancel the latest committed text if needed
        return null;
    }

    @Override
    public AttributedCharacterIterator getSelectedText(AttributedCharacterIterator.Attribute[] attributes) {
        // Return the selected text if needed
        return null;
    }

    public void paint(Graphics g) {
        // Optionally, you can implement custom painting for the input method if needed
        //System.out.println("Painting input method text: " + currentText);
        if (currentText != null) {
            Rectangle cursorBounds = view.getCursorBounds();
            FontMetrics fm = g.getFontMetrics();
            //g.setColor(Color.BLACK);
            g.drawString(currentText, cursorBounds.x, cursorBounds.y + fm.getAscent());
            //System.out.println("Painting current text: " + currentText + " at " + cursorBounds);
        }
    }
}
