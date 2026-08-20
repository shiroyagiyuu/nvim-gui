package pureplus.neovimgui.neovimif;

public class NeovimModeInfo {
    int shape;
    int cell_percentage;
    int blinkWait, blinkOn, blinkOff;
    int attrId;
    String shortName;
    String name;
    int mouseShape;

    /* cursor shapes */
    public final static int SHAPE_BLOCK = 0;
    public final static int SHAPE_HORIZONTAL = 1;
    public final static int SHAPE_VERTICAL = 2;

    /**
     * Set the shape of the cursor.
     * @param shape
     */
    public void setShape(int shape) {
        this.shape = shape;
    }

    /**
     * Gets the shape of the cursor.
     * @return
     */
    public int getShape() {
        return shape;
    }

    /**
     * Gets the percentage of the cursor.
     * @return
     */
    public int getCellPercentage() {
        return cell_percentage;
    }

    /**
     * Sets the percentage of the cursor.
     * @param percentage
     */
    public void setCellPercentage(int percentage) {
        this.cell_percentage = percentage;
    }

    /**
     * Gets the blink-wait duration.
     * @return
     */
    public int getBlinkWait() {
        return blinkWait;
    }

    /**
     * Sets the blink-wait duration.
     * @param blinkWait
     */
    public void setBlinkWait(int blinkWait) {
        this.blinkWait = blinkWait;
    }

    /**
     * Gets the blink-on duration.
     * @return
     */
    public int getBlinkOn() {
        return blinkOn;
    }

    /**
     * Sets the blink-on duration.
     * @param blinkOn
     */
    public void setBlinkOn(int blinkOn) {
        this.blinkOn = blinkOn;
    }

    /**
     * Gets the blink-off duration.
     * @return
     */
    public int getBlinkOff() {
        return blinkOff;
    }

    /**
     * Sets the blink-off duration.
     * @param blinkOff
     */
    public void setBlinkOff(int blinkOff) {
        this.blinkOff = blinkOff;
    }

    /**
     * Gets the attribute ID.
     * @return
     */
    public int getAttrId() {
        return attrId;
    }

    /**
     * Sets the attribute ID.
     * @param attrId
     */
    public void setAttrId(int attrId) {
        this.attrId = attrId;
    }

    /**
     * Gets the short name of this Attribute.
     * @return
     */
    public String getShortName() {
        return shortName;
    }


    /**
     * Sets the short name of this Attribute.
     * @param shortName
     */
    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    /**
     * Gets the name of this Attribute.
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this Attribute.
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the mouse shape associated with this Attribute.
     * @return
     */
    public int getMouseShape() {
        return mouseShape;
    }

    /**
     * Sets the mouse shape associated with this Attribute.
     * @param mouseShape
     */
    public void setMouseShape(int mouseShape) {
        this.mouseShape = mouseShape;
    }
}
