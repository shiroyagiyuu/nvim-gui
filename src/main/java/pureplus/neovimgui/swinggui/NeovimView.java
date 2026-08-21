package pureplus.neovimgui.swinggui;

import javax.swing.*;

import pureplus.neovimgui.neovimif.NeovimApi;
import pureplus.neovimgui.neovimif.NeovimDrawEventListener;
import pureplus.neovimgui.neovimif.NeovimDrawModel;
import pureplus.neovimgui.neovimif.NeovimModeInfo;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.im.InputMethodRequests;

public class NeovimView extends JPanel implements NeovimDrawEventListener
{
    NeovimDrawModel  model;
    NeovimApi        api;
    Dimension      cellSize;
    Font           baseFont, boldFont, italicFont, boldItalicFont;
    int            ascent;
    NeovimInputMethodRequests  inputMethodListener;
    JFrame         frm;

	public NeovimView(NeovimDrawModel model, NeovimApi api) {
        this.model = model;
        this.api = api;
        setFont(new Font("Monospaced", Font.PLAIN, 12));
        cellSize = null;

        setFocusable(true);
        setFocusTraversalKeysEnabled(false);

        inputMethodListener = new NeovimInputMethodRequests(this, model, api);
        addInputMethodListener(inputMethodListener);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent evt) {
                if (cellSize == null) { return; }

                Dimension comp_sz = getSize();
                if (comp_sz.width <= 0 || comp_sz.height <= 0) { return; }

                Dimension grid_size = model.getSize();
                int new_gridw = comp_sz.width / cellSize.width;
                int new_gridh = comp_sz.height / cellSize.height;
                if ((new_gridw > 0 && new_gridh > 0) &&
                    (new_gridw != grid_size.width || new_gridh != grid_size.height)) {
                     try {
                         api.uiTryResize(new_gridw, new_gridh);
                     } catch (java.io.IOException ex) {
                         ex.printStackTrace();
                     }
                }
            }
        });
	}

    public void setFrame(JFrame frm) {
        this.frm = frm;
    }

    public void setFontSet(String fontName, int fontSize) {
        baseFont = new Font(fontName, Font.PLAIN, fontSize);
        boldFont = new Font(fontName, Font.BOLD, fontSize);
        italicFont = new Font(fontName, Font.ITALIC, fontSize);
        boldItalicFont = new Font(fontName, Font.BOLD | Font.ITALIC, fontSize);
        setFont(baseFont);
        cellSize = null;
        calcCellSize();
    }

    private void calcCellSize() {
        Graphics g = getGraphics();
        if (g != null) {
            calcCellSize(g);
            g.dispose();
        }
    }

    private void calcCellSize(Graphics g) {
        FontMetrics m = g.getFontMetrics(baseFont);
        int  cellw = m.charWidth('W');
        int  cellh = m.getHeight();
        this.cellSize = new Dimension(cellw,cellh);
        this.ascent = m.getAscent();
    }

    @Override
    public Dimension getPreferredSize() {
        if (cellSize==null) {
            calcCellSize();
        }
        Dimension modelSize = model.getSize();

        int  vieww = cellSize.width * modelSize.width;
        int  viewh = cellSize.height * modelSize.height;

        return new Dimension(vieww, viewh);
    }

    private void paintCell(Graphics g, NeovimDrawModel.Cell cell, Rectangle cellBounds) {
        int str_y = cellBounds.y + ascent;

        if (!cell.getText().isEmpty()) {
            NeovimDrawModel.Hilight  hl = model.getHilight(cell.getHilight());
            Color  fgcol,bgcol;
            if (hl != null) {
                fgcol = hl.getForeground();
                bgcol = hl.getBackground();
                if (fgcol == null) {
                    fgcol = model.getForeground();
                }
                if (bgcol == null) {
                    bgcol = model.getBackground();
                }
                g.setFont(hl.getFont());
            } else {
                    fgcol = model.getForeground();
                    bgcol = model.getBackground();
            }

            g.setColor(bgcol);
            g.fillRect(cellBounds.x, cellBounds.y, cellBounds.width, cellBounds.height);

            g.setColor(fgcol);
            g.drawString(cell.getText(), cellBounds.x, str_y);
        }
    }

    private void paintCursor(Graphics g) {
        if (!model.isBusy()) {
            Rectangle  cursorBounds = getCursorBounds();
            int  mode = model.getMode();
            NeovimModeInfo modeInfo = model.getModeInfo(mode);

            if (modeInfo != null) {
                int   shape = modeInfo.getShape();
                switch (shape) {
                    case NeovimModeInfo.SHAPE_BLOCK -> {
                        g.setXORMode(Color.black);
                        g.fillRect(cursorBounds.x, cursorBounds.y, cursorBounds.width, cursorBounds.height);
                    }
                    case NeovimModeInfo.SHAPE_HORIZONTAL -> {
                        int  height = (cursorBounds.height * modeInfo.getCellPercentage()) / 100;
                        int  y = cursorBounds.y + cursorBounds.height - height;
                        g.setXORMode(Color.black);
                        g.fillRect(cursorBounds.x, y, cursorBounds.width, height);
                    }
                    case NeovimModeInfo.SHAPE_VERTICAL -> {
                        int  width = (cursorBounds.width * modeInfo.getCellPercentage()) / 100;
                        g.setXORMode(Color.black);
                        g.fillRect(cursorBounds.x, cursorBounds.y, width, cursorBounds.height);
                    }
                }
            }
        }
    }

    @Override
    public void paint(Graphics g) {
        g.setFont(getFont());
        if (cellSize==null) { calcCellSize(g); }
        Rectangle  cellBounds = new Rectangle(0, 0, cellSize.width, cellSize.height);
        Dimension  gsize = model.getSize();

        g.setColor(model.getBackground());
        Rectangle   clip = g.getClipBounds();
        g.fillRect(clip.x, clip.y, clip.width, clip.height);

        for (int row = 0; row < gsize.height; row++) {
            cellBounds.y = row * cellBounds.height;
            for (int col = 0; col < gsize.width; col++) {
                cellBounds.x = col * cellBounds.width;
                NeovimDrawModel.Cell  cell = model.getCell(row,col);
                if (cell != null) {
                    paintCell(g, cell, cellBounds);
                }
            }
        }

        g.setColor(model.getForeground());
        inputMethodListener.paint(g);

        paintCursor(g);
    }

    public Rectangle getCursorBounds() {
        if (cellSize==null) { return new Rectangle(0, 0, 0, 0); }
        NeovimDrawModel.Cursor  cursor = model.getCursor();
        Rectangle  cursorBounds = new Rectangle(cursor.getColumn() * cellSize.width,
                                            cursor.getRow() * cellSize.height,
                                            cellSize.width,
                                            cellSize.height);
        //api.uiSetCursor(cellBounds);
        return cursorBounds;
    }

    public Rectangle getCursorBoundsFromDisplay() {
        if (cellSize==null) { return new Rectangle(0, 0, 0, 0); }
        NeovimDrawModel.Cursor  cursor = model.getCursor();
        Point p = new Point(cursor.getColumn() * cellSize.width,
                        cursor.getRow() * cellSize.height);
        SwingUtilities.convertPointToScreen(p, this);
        Rectangle cursorBounds = new Rectangle(p.x, p.y, cellSize.width, cellSize.height);
        return cursorBounds;
    }

    public void drawEventOccurred(int event) {
        switch (event) {
            case NeovimDrawEventListener.EVENT_FLASH -> { repaint();}
            case NeovimDrawEventListener.EVENT_MODE_CHANGE -> { mode_changed(); }
            default -> {
                System.out.println("Unknown draw event: " + event);
            }
        }
    }

    private void mode_changed() {
        String mode = model.getModeName();
        if (mode.equals("insert") || mode.equals("replace")) {
            enableInputMethods(true);
        } else {
            enableInputMethods(false);
        }
    }

    @Override
    public InputMethodRequests getInputMethodRequests() {
        return inputMethodListener;
    }
}

