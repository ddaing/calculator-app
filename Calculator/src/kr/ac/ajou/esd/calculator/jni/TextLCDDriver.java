package kr.ac.ajou.esd.calculator.jni;

import android.content.Context;
import android.widget.Toast;

public class TextLCDDriver {
	public static final int FIRST_LINE = 1;	
	public static final int SECOND_LINE = 2;	
	private boolean mConnectFlag;
	static {
		System.loadLibrary("JNIDriver");
	}
	private native static int gpioexport(int gpio);
	private native static int gpiounexport(int gpio);
	private native static int gpiosetdir(int gpio, int dir, int val);
	private native static int DisplayControl(int display_enable);
	private native static int DisplayClear();
	private native static int CursorControl(int cursor_enable);
	private native static int CursorShift(int set_shift);
	private native static int Cursorhome();
	private native static int DisplayWrite(int line, String str, int len);
	private native static int initializetextlcd();	

	private String mText = "";
	
	public TextLCDDriver() {
		mConnectFlag = false;
	}
	public int open() {
		int i = 0;
		if (mConnectFlag)
			return -1;

		for (i = 104; i < 107; i++) {
			gpioexport(i);
			gpiosetdir(i, 0, 0);
		}
		for (i = 150; i < 158; i++) {
			gpioexport(i);
			gpiosetdir(i, 0, 0);
		}
		initializetextlcd();

		mConnectFlag = true;
		return 1;
	}

	public void close() {
		if (!mConnectFlag)
			return;
		mConnectFlag = false;
	}

	protected void finalize() throws Throwable {
		close();
		super.finalize();
	}

	public void setDisplayVisible(int display_enable) {
		DisplayControl(display_enable);
	}

	public void clearDisplay() {

		DisplayClear();
	}

	public void setCursorVisible(int cursor_enable) {
		CursorControl(cursor_enable);
	}

	public void cursorLeft(){

		CursorShift(1);
	}
	
	public void cursorRight(){

		CursorShift(0);
	}
	
	private void setCursorShift(int set_shift) {
		CursorShift(set_shift);
	}

	public void setCursorHome() {
		Cursorhome();
	}

	public void setText(int line, String str) {
		if(line != 2)
			DisplayClear();
		DisplayWrite(line, str, str.length());
		mText = str;
	}

}
