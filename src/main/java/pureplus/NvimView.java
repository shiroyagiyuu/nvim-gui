package pureplus;

import javax.swing.*;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Color;

public class NvimView extends JPanel implements NvimDrawEventListener
{
    NvimDrawModel  model;
    Dimension      cellSize;
    int            ascent;

	public NvimView(NvimDrawModel model) {
        this.model = model;
        setFont(new Font("Monospaced", Font.PLAIN, 12));
        cellSize = null;

        setFocusable(true);
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
    }

    @Override
    public void paint(Graphics g) {
        if (cellSize==null) { calcPrefSize(g); }
        Rectangle  cellrect = new Rectangle(0, 0, cellSize.width, cellSize.height);
        Dimension  gsize = model.getSize();
        
        for (int row = 0; row < gsize.height; row++) {
            cellrect.y = row * cellrect.height;
            for (int col = 0; col < gsize.width; col++) {
                NvimDrawModel.Cell  cell = model.getCell(row,col);
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
    }

    public void drawEventOccurred(int event) {
        repaint();
        //frm.pack();
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
        frm.pack();
        requestFocusInWindow();
    }
}

