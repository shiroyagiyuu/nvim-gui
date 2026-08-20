package pureplus.neovimgui.neovimif;

public interface NeovimDrawEventListener
{
	final static int EVENT_FLASH = 1;
	final static int EVENT_MODE_CHANGE = 2;

	public abstract void drawEventOccurred(int event);
}

