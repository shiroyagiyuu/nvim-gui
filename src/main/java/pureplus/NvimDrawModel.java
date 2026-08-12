package pureplus;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Color;

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

        public Hilight(Font font, Color fg, Color bg) {
            this.font = font;
            this.foreground = fg;
            this.background = bg;
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

    public Hilight getDefaultHilight() {
        return new Hilight(new Font("Monospaced", Font.PLAIN, 12), Color.black, Color.white);
    }

    public void setHilight(int id, Hilight hl) {
        if (hilights == null || id >= hilights.length) {
             int newlen = ((id/32)+1)*32;
             this.hilights = java.util.Arrays.copyOf(hilights, newlen);
        }

        hilights[id] = hl;
    }

    public Hilight getHilight(int id) {
        return hilights[id];
    }

    public NvimDrawModel() {
        this.hilights = new Hilight[32];
    }
}

