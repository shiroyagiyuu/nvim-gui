package pureplus;

public interface NvimDrawEventListener
{
	public abstract void cursorGoto(int row, int col);
	public abstract void put(String str);
	public abstract void hilightSet(int type, int value);
}

