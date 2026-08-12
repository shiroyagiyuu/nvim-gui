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

    public class Cursor
    {
        int grid,row,col;
        int shape;

        final int SHAPE_BLOCK=0;
        final int SHAPE_HORIZONTAL=1;
        final int SHAPE_VERTICAL=2;

        public void setCursor(int grid, int row, int col) {
            this.grid = grid;
            this.row  = row;
            this.col  = col;
        }

        public int getRow() {
            return this.row;
        }

        public int getColumn() {
            return this.col;
        }
    }

    Cell[][]  cells;
    Hilight[] hilights;
    Color     foreground,background,special_color;
    Cursor    cursor;
    int       mode;

    ArrayList<NvimDrawEventListener>  drawlisteners;

    public void setSize(int cols, int rows) {
        Cell[][]  newcells = new Cell[rows][cols];

        for (int ir=0; ir<rows; ir++) {
           for (int ic=0; ic<cols; ic++) {
                if (cells != null && ir < cells.length && ic < cells[0].length) {
                    /* copy old to new */
                    newcells[ir][ic] = cells[ir][ic];
                } else {
                    newcells[ir][ic] = new Cell();
                }
            }
        }

        this.cells = newcells;
    }

    /**
     * return grid size 
     * @return grid size
     */
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
     * カーソルを設定します
     * @param grid grid
     * @param row  row
     * @param col  column
     */
    public void setCursor(int grid, int row, int col) {
        cursor.setCursor(grid,row,col);
    }

    /**
     * カーソルを返します
     * @return カーソル
     */
    public Cursor getCursor() {
        return cursor;
    }

    /**
     * modeを設定します
     * @param mode 設定先のモード
     */
    public void setMode(int mode) {
        this.mode = mode;
    }

    /**
     * scrollします
     */
    public void scroll(int top, int bottom, int left, int right, int rowoff, int coloff) {
        System.out.println("scroll top:"+top+" bottom:"+bottom+" row:"+rowoff);
        Cell[][]  new_cells = new Cell[cells.length][cells[0].length];
        for (int row = 0; row<new_cells.length; row++) {
            for (int col = 0; col<new_cells[0].length; col++) {
                if ((top-rowoff)<=row && row<(bottom-rowoff) &&
                    (left-coloff)<=col && col<(right-coloff)) {
                    new_cells[row][col] = cells[row+rowoff][col+coloff];
                } else {
                    new_cells[row][col] = new Cell();
                }
            }
        }

        this.cells = new_cells;
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
        this.cursor = new Cursor();
        this.drawlisteners = new ArrayList<NvimDrawEventListener>();
    }
}

