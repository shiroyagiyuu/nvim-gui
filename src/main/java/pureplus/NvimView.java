package pureplus;

import javax.swing.*;
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

public class NvimView extends JPanel implements NvimDrawEventListener
{
    NvimDrawModel  model;
    NvimApi        api;
    Dimension      cellSize;
    int            ascent;
    boolean        resize_completed;
    NvimInputMethodRequests inputMethodListener;

	public NvimView(NvimDrawModel model) {
        this.model = model;
        setFont(new Font("Monospaced", Font.PLAIN, 12));
        cellSize = null;

        setFocusable(true);
        setFocusTraversalKeysEnabled(false);

        inputMethodListener = new NvimInputMethodRequests(this);
        addInputMethodListener(inputMethodListener);

        resize_completed = false;
	}

    private void calcPrefSize(Graphics g) {
        FontMetrics m = g.getFontMetrics();
        int  cellw = m.charWidth('W');
        int  cellh = m.getHeight();
        this.cellSize = new Dimension(cellw,cellh);
        this.ascent = m.getAscent();

        Dimension modelSize = model.getSize();
        int  vieww = cellw * modelSize.width;
        int  viewh = cellh * modelSize.height;
        setPreferredSize(new Dimension(vieww, viewh));
    
        redrawFrame();    
    }

    public void setApi(NvimApi api) {
        this.api = api;
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent evt) {
                Dimension comp_sz = getSize();
                Dimension grid_size = model.getSize();
                int new_gridw = comp_sz.width / cellSize.width;
                int new_gridh = comp_sz.height / cellSize.height;
                if (new_gridw != grid_size.width || new_gridh != grid_size.height) {
                     try {
                         api.uiTryResize(new_gridw, new_gridh);
                         resize_completed = false;
                     } catch (java.io.IOException ex) {
                         ex.printStackTrace();
                     }
                }
                //redrawFrame();
            }
        });
        inputMethodListener.setApi(api);
    }

    private void paintCell(Graphics g, NvimDrawModel.Cell cell, Rectangle cellBounds) {
        int str_y = cellBounds.y + ascent;

        if (!cell.getText().isEmpty()) {
            NvimDrawModel.Hilight  hl = model.getHilight(cell.getHilight());
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

    @Override
    public void paint(Graphics g) {
        g.setFont(getFont());
        if (cellSize==null || !resize_completed) { calcPrefSize(g); }
        Rectangle  cellBounds = new Rectangle(0, 0, cellSize.width, cellSize.height);
        Dimension  gsize = model.getSize();

        g.setColor(model.background);
        Rectangle   clip = g.getClipBounds();
        g.fillRect(clip.x, clip.y, clip.width, clip.height);

        for (int row = 0; row < gsize.height; row++) {
            cellBounds.y = row * cellBounds.height;
            for (int col = 0; col < gsize.width; col++) {
                cellBounds.x = col * cellBounds.width;
                NvimDrawModel.Cell  cell = model.getCell(row,col);
                if (cell != null) {
                    paintCell(g, cell, cellBounds);
                }
            }
        }

        g.setColor(model.getForeground());
        inputMethodListener.paint(g);

        if (!model.isBusy()) {
            NvimDrawModel.Cursor  cursor = model.getCursor();
            cellBounds.x = cursor.getColumn() * cellBounds.width;
            cellBounds.y = cursor.getRow() * cellBounds.height;
            g.setXORMode(Color.black);
            g.fillRect(cellBounds.x, cellBounds.y, cellBounds.width, cellBounds.height);
        }
    }

    public Rectangle getCursorBounds() {
        if (cellSize==null) { return new Rectangle(0, 0, 0, 0); }
        NvimDrawModel.Cursor  cursor = model.getCursor();
        Rectangle  cursorBounds = new Rectangle(cursor.getColumn() * cellSize.width,
                                            cursor.getRow() * cellSize.height,
                                            cellSize.width,
                                            cellSize.height);
        //api.uiSetCursor(cellBounds);
        return cursorBounds;
    }

    public Rectangle getCursorBoundsFromDisplay() {
        if (cellSize==null) { return new Rectangle(0, 0, 0, 0); }
        NvimDrawModel.Cursor  cursor = model.getCursor();
        Point p = new Point(cursor.getColumn() * cellSize.width,
                        cursor.getRow() * cellSize.height);
        SwingUtilities.convertPointToScreen(p, this);
        Rectangle cursorBounds = new Rectangle(p.x, p.y, cellSize.width, cellSize.height);
        return cursorBounds;
    }

    public void drawEventOccurred(int event) {
        repaint();
    }

    JFrame  frm;

    public JFrame createFrame() {
        frm = new JFrame("NVim");
        frm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frm.setContentPane(this);
        frm.setVisible(true);
        frm.pack();

        return frm;
    }

    public void redrawFrame() {
        SwingUtilities.invokeLater(() -> {
            frm.pack();
            resize_completed = true;
            requestFocusInWindow();
        });
    }

    @Override
    public InputMethodRequests getInputMethodRequests() {
        return inputMethodListener;
    }
}

