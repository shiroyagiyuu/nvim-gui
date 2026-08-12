package pureplus;

import javax.swing.*;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class NvimView extends JPanel implements NvimDrawEventListener
{
    NvimDrawModel  model;
    NvimApi        api;
    Dimension      cellSize;
    int            ascent;
    boolean        resize_completed;

	public NvimView(NvimDrawModel model) {
        this.model = model;
        setFont(new Font("Monospaced", Font.PLAIN, 12));
        cellSize = null;

        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
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
            }
        });
    }

    @Override
    public void paint(Graphics g) {
        if (cellSize==null || !resize_completed) { calcPrefSize(g); }
        Rectangle  cellrect = new Rectangle(0, 0, cellSize.width, cellSize.height);
        Dimension  gsize = model.getSize();
        
        for (int row = 0; row < gsize.height; row++) {
            cellrect.y = row * cellrect.height;
            for (int col = 0; col < gsize.width; col++) {
                NvimDrawModel.Cell  cell = model.getCell(row,col);
                if (cell == null) continue;
                NvimDrawModel.Hilight  hl = model.getHilight(cell.getHilight());
                cellrect.x = col * cellrect.width;
                int str_y = cellrect.y+ascent;
                
                if (hl != null) {
                    g.setColor(hl.getBackground());
                    g.fillRect(cellrect.x, cellrect.y, cellrect.width, cellrect.height);

                    g.setFont(hl.getFont());
                    g.setColor(hl.getForeground());
                } else {
                    g.setColor(Color.white);
                    g.fillRect(cellrect.x, cellrect.y, cellrect.width, cellrect.height);
                    g.setColor(Color.black);
                }
                g.drawString(cell.getText(), cellrect.x, str_y);
            }
        }

        NvimDrawModel.Cursor  cursor = model.getCursor();
        cellrect.x = cursor.getColumn() * cellrect.width;
        cellrect.y = cursor.getRow() * cellrect.height;
        g.setXORMode(Color.black);
        g.fillRect(cellrect.x, cellrect.y, cellrect.width, cellrect.height);
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
}

