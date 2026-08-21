package pureplus.neovimgui.neovimif;

import java.awt.Dimension;
import java.util.ArrayList;

public class NeovimDrawModel
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
        private int    foreground;
        private int    background;
        private int    special_color;
        private boolean bold;
        private boolean italic;
        private boolean reverse;
        private boolean underline;
        private boolean strikethrough;
        private boolean undercurl;
        private boolean nocombine;
        private boolean underdouble;
        private int     blend;
        private String  url;

        public Hilight() {
            this.foreground = -1;
            this.background = -1;
            this.special_color = -1;
            this.bold = false;
            this.italic = false;
            this.reverse = false;
            this.underline = false;
            this.strikethrough = false;
            this.undercurl = false;
            this.nocombine = false;
            this.blend = 100;
            this.url = null;
        }

        public int getForeground() {
            return this.foreground;
        }

        public void setForeground(int col) {
            this.foreground = col;
        }

        public int getBackground() {
            return this.background;
        }

        public void setBackground(int col) {
            this.background = col;
        }

        public int getSpecialColor() {
            return special_color;
        }

        public void setSpecialColor(int col) {
            this.special_color = col;
        }

        public boolean isBold() {
            return this.bold;
        }

        public void setBold(boolean b) {
            this.bold = b;
        }

        public boolean isItalic() {
            return this.italic;
        }

        public void setItalic(boolean b) {
            this.italic = b;
        }

        public boolean isUnderline() {
            return this.underline;
        }

        public void setUnderline(boolean b) {
            this.underline = b;
        }

        public boolean isStrikethrough() {
            return this.strikethrough;
        }

        public void setStrikethrough(boolean b) {
            this.strikethrough = b;
        }

        public boolean isReverse() {
            return this.reverse;
        }

        public void setReverse(boolean b) {
            this.reverse = b;
        }

        public boolean isUndercurl() {
            return undercurl;
        }

        public void setUndercurl(boolean undercurl) {
            this.undercurl = undercurl;
        }

        public boolean isUnderDouble() {
            return this.underdouble;
        }

        public void setUnderDouble(boolean b) {
            this.underdouble = b;
        }

        public boolean isNocombine() {
            return nocombine;
        }

        public void setNocombine(boolean nocombine) {
            this.nocombine = nocombine;
        }

        public int getBlend() {
            return blend;
        }

        public void setBlend(int blend) {
            this.blend = blend;
        }

        public String getURL() {
            return this.url;
        }

        public void setURL(String url) {
            this.url = url;
        }
    }

    public class Cursor
    {
        int grid,row,col;

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
    int       foreground=-1,background=-1,special_color=-1;
    Cursor    cursor;
    int       mode;
    String    modeName;
    boolean   cursor_busy;
    String    currentDir;
    NeovimModeInfo[]  modeInfos;

    ArrayList<NeovimDrawEventListener>  drawlisteners;

    public String getCurrentDir() {
        return this.currentDir;
    }

    public void setCurrentDir(String chdir) {
        this.currentDir = chdir;
    }

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
        if (0<=row && row<cells.length &&
            0<=col && col<cells[0].length) {
            return cells[row][col];
        } else {
            return null;
        }
    }

    /**
     * get default foreground color
     * @return foreground color
     */
    public int getForeground() {
        return this.foreground;
    }

    /**
     * get default background color
     * @return background color
     */
    public int getBackground() {
        return this.background;
    }

    /**
     * get default special color
     * @return special color
     */
    public int getSpecialColor() {
        return this.special_color;
    }

    public void setDefaultColor(int fgcolor, int bgcolor, int spcolor) {
        this.foreground = fgcolor;
        this.background = bgcolor;
        this.special_color = spcolor;
    }

    /**
     * set hilight
     * @param id hilight id
     * @param hl hilight
     */
    public void setHilight(int id, Hilight hl) {
        if (hilights == null || id >= hilights.length) {
             int newlen = ((id/32)+1)*32;
             this.hilights = java.util.Arrays.copyOf(hilights, newlen);
        }

        hilights[id] = hl;
    }
    
    /**
     * get hilight
     * @param id hilight id
     * @return hilight
     */
    public Hilight getHilight(int id) {
        if (hilights[id] == null) {
            hilights[id] = new Hilight();
        }
        return hilights[id];
    }

    public Hilight getNewHilight() {
        return new Hilight();
    }

    /**
     * set cursor position
     * @param grid grid
     * @param row  row
     * @param col  column
     */
    public void setCursor(int grid, int row, int col) {
        cursor.setCursor(grid,row,col);
    }

    /**
     * get cursor position
     * @return cursor
     */
    public Cursor getCursor() {
        return cursor;
    }

    /**
     * set mode
     * @param mode new mode
     */
    public void setMode(int mode, String mode_name) {
        this.mode = mode;
        this.modeName = mode_name;
        fireDrawEvent(NeovimDrawEventListener.EVENT_MODE_CHANGE);
    }

    /**
     * get mode
     * @return mode
     */
    public int getMode() {
        return this.mode;
    }

    /**
     * get mode name
     * @return mode name
     */
    public String getModeName() {
        return this.modeName;
    }

    /**
     * get mode info
     * @param mode_idx
     * @return
     */
    public NeovimModeInfo getModeInfo(int mode_idx) {
        if (this.modeInfos == null || mode_idx >= this.modeInfos.length) {
            return null;
        }
        return this.modeInfos[mode_idx];
    }
    
    /**
     * set mode info
     * @param mode_idx
     * @param modeInfo
     */
    public void setModeInfo(int mode_idx, NeovimModeInfo modeInfo) {
        if (this.modeInfos == null || mode_idx >= this.modeInfos.length) {
            int newlen = ((mode_idx/8)+1)*8;
            this.modeInfos = java.util.Arrays.copyOf(modeInfos, newlen);
        }
        this.modeInfos[mode_idx] = modeInfo;
    }

    /**
     * busy mouse cursor
     * @param busy true:cursor not draw, false: cursor will draw
     */
    public void setBusy(boolean busy) {
        this.cursor_busy = busy;
    }

    public boolean isBusy() {
        return this.cursor_busy;
    }

    /**
     * do scroll
     * @param top top row
     * @param bottom bottom row
     * @param left left column
     * @param right right column
     * @param rowoff row offset
     * @param coloff column offset
     */
    public void scroll(int top, int bottom, int left, int right, int rowoff, int coloff) {
        //System.out.println("scroll top:"+top+" bottom:"+bottom+" row:"+rowoff);
        Cell[][]  new_cells = new Cell[cells.length][cells[0].length];
        for (int row = 0; row<new_cells.length; row++) {
            for (int col = 0; col<new_cells[0].length; col++) {
                if ((top<=row) && (row<bottom) && (left<=col) && (col<right) &&
                    (0<=row+rowoff) && (row+rowoff<cells.length) &&
                    (0<=col+coloff) && (col+coloff<cells[0].length) ){
                    new_cells[row][col] = cells[row+rowoff][col+coloff];
                } else {
                    new_cells[row][col] = cells[row][col];
                }
            }
        }

        this.cells = new_cells;
    }

    /**
     * clear all cells
     */
    public void clear() {
        for (int row=0; row<cells.length; row++) {
            for (int col=0; col<cells[0].length; col++) {
                cells[row][col] = null;
            }
        }
    }
    /**
     * flush DrawEvent
     */
    public void flush() {
        fireDrawEvent(NeovimDrawEventListener.EVENT_FLASH);
    }

    /**
     * Add DrawEventListener
     * @param l listener
     */
    public void addDrawEventListener(NeovimDrawEventListener l) {
        if (!drawlisteners.contains(l)) {
            drawlisteners.add(l);
        }
    }

    /**
     * fire DrawEvent
     * @param event event type
     */
    public void fireDrawEvent(int event) {
        for (NeovimDrawEventListener l : drawlisteners) {
            l.drawEventOccurred(event);
        }
    }

    public NeovimDrawModel() {
        this.hilights = new Hilight[32];
        this.modeInfos = new NeovimModeInfo[8];
        this.cursor = new Cursor();
        this.drawlisteners = new ArrayList<NeovimDrawEventListener>();
    }
}

