package pureplus;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Color;
import java.util.ArrayList;

public class NvimDrawModel
{
	public class Cell
    {
        private String text;
        private int    hilight;

        public Cell(String text, int hilight) {
            this.text    = text;
            this.hilight = hilight;
        }

        public Cell() {
            this(" ", 0);
        }

        public String getText() {
            return this.text;
        }

        public int getHilight() {
            return this.hilight;
        }
    }

    public class Hilight
    {
        private Font   font;
        private Color  foreground;
        private Color  background;
        private Color  special_color;

        public Hilight(Font font, Color fg, Color bg, Color sp) {
            this.font = font;
            this.foreground = fg;
            this.background = bg;
            this.special_color = sp;
        }

        public Font getFont() {
            return this.font;
        }

        public Color getForeground() {
            return this.foreground;
        }

        public Color getBackground() {
            return this.background;
        }

        public void setForeground(int col) {
            this.foreground = new Color(col);
        }

        public void setBackground(int col) {
            this.background = new Color(col);
        }

        public void setBold(boolean b) {
            if (b) {
                this.font = font.deriveFont(Font.BOLD);
            } else {
                this.font = font.deriveFont(Font.PLAIN);
            }
        }

        public void setItalic(boolean b) {
            if (b) {
                this.font = font.deriveFont(Font.ITALIC);
            } else {
                this.font = font.deriveFont(Font.PLAIN);
            }
        }
    }

    Cell[][]  cells;
    Hilight[] hilights;
    Color     foreground,background,special_color;

    ArrayList<NvimDrawEventListener>  drawlisteners;

    public void setSize(int cols, int rows) {
        Cell[][]  bkcells=cells;

        cells = new Cell[rows][cols];

        for (int ir=0; ir<rows; ir++) {
           for (int ic=0; ic<cols; ic++) {
                if (bkcells != null && bkcells.length > ic && bkcells[0].length > ir) {
                    /* copy old to new */
                	cells[ir][ic] = bkcells[ir][ic];
                } else {
                    cells[ir][ic] = new Cell();
                }
            }
        }
    }

    /* return grid size */
    public Dimension getSize() {
        return new Dimension(cells[0].length, cells.length);
    }

    public void setCell(int row, int col, String text, int hilight) {
        cells[row][col] = new Cell(text, hilight);
    }

    public Cell getCell(int row, int col) {
        return cells[row][col];
    }

    public void setDefaultColor(int fgcolor, int bgcolor, int spcolor) {
        this.foreground = new Color(fgcolor);
        this.background = new Color(bgcolor);
        this.special_color = new Color(spcolor);
    }

    public Hilight getDefaultHilight() {
        return new Hilight(new Font("Monospaced", Font.PLAIN, 12), foreground, background, special_color);
    }

    /**
     * ハイライトを設定します
     * @param id 設定するハイライトのid
     * @param hl 設定するハイライト
     */
    public void setHilight(int id, Hilight hl) {
        if (hilights == null || id >= hilights.length) {
             int newlen = ((id/32)+1)*32;
             this.hilights = java.util.Arrays.copyOf(hilights, newlen);
        }

        hilights[id] = hl;
    }
    
    /**
     * ハイライトを取得します
     * @param id 取得するハイライトのid
     */
    public Hilight getHilight(int id) {
        if (hilights[id] == null) {
            hilights[id] = getDefaultHilight();
        }
        return hilights[id];
    }

    /**
     * DrawEventListenerを登録します
     * @param l 登録するリスナ
     */
    public void addDrawEventListener(NvimDrawEventListener l) {
        if (!drawlisteners.contains(l)) {
            drawlisteners.add(l);
        }
    }

    /**
     * DrawEventを発生させます
     * @param event 発生させるイベントのタイプ
     */
    public void fireDrawEvent(int event) {
        for (NvimDrawEventListener l : drawlisteners) {
            l.drawEventOccurred(event);
        }
    }

    /**
     * nvimのflushコマンドを受け付けます
     */
    public void flush() {
        fireDrawEvent(0);
    }

    public NvimDrawModel() {
        this.hilights = new Hilight[32];
        this.drawlisteners = new ArrayList<NvimDrawEventListener>();
    }
}

