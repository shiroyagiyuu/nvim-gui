package pureplus;

import java.awt.Dimension;

public class NvimDrawModel
{
	public class Cell
    {
        char c;
        int  hilight;

        public Cell(char c, int hilight) {
             this.c = c;
             this.hilight = hilight;
        }

        public Cell() {
             this(' ', 0);
        }
    }

    Cell[][]  cells;

    public void setSize(int rows, int cols) {
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

    public Dimension getSize(int rows, int cols) {
        return new Dimension(rows, cols);
    }

    public void setCell(int row, int col, char c, int hilight) {
        cells[row][col] = new Cell(c, hilight);
    }

    public Cell getCell(int row, int col) {
        return cells[row][col];
    }
}

