package kr.ac.ajou.esd.calculator.jni;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import android.util.Log;

public class SegmentDriver {
	public static final int GPIO_OUTPUT = 0;
	public static final int GPIO_INPUT = 1;
	public static final int GPIO_LOW = 0;
	public static final int GPIO_HIGH = 1;

	public static final int GPIO_SEG_SEL0 = 112;
	public static final int GPIO_SEG_SEL1 = 113;
	public static final int GPIO_SEG_SEL2 = 114;
	public static final int GPIO_SEG_SEL3 = 115;
	public static final int GPIO_SEG_SEL4 = 116;
	public static final int GPIO_SEG_SEL5 = 108;

	public static final int GPIO_SEG_DATA_A = 192;
	public static final int GPIO_SEG_DATA_B = 193;
	public static final int GPIO_SEG_DATA_C = 194;
	public static final int GPIO_SEG_DATA_D = 195;
	public static final int GPIO_SEG_DATA_E = 196;
	public static final int GPIO_SEG_DATA_F = 197;
	public static final int GPIO_SEG_DATA_G = 198;
	public static final int GPIO_SEG_DATA_H = 199;

	int seg_sel[] = { GPIO_SEG_SEL0, GPIO_SEG_SEL1, GPIO_SEG_SEL2,
			GPIO_SEG_SEL3, GPIO_SEG_SEL4, GPIO_SEG_SEL5 };
	int seg_data[] = { GPIO_SEG_DATA_A, GPIO_SEG_DATA_B, GPIO_SEG_DATA_C,
			GPIO_SEG_DATA_D, GPIO_SEG_DATA_E, GPIO_SEG_DATA_F, GPIO_SEG_DATA_G,
			GPIO_SEG_DATA_H };

	private boolean mConnectFlag;

	static {
		System.loadLibrary("JNIDriver");
	}

	private native int gpioexport(int gpio);

	private native int gpiosetdir(int gpio, int dir, int val);

	private native int segcontrol(int val);

	private native int gpiounexport(int gpio);

	private native int StopSegment(int flag);

	public SegmentDriver() {
		mConnectFlag = false;
	}

	public int open() {
		int i = 0;
		for (i = 0; i < 6; i++) {
			gpioexport(seg_sel[i]);
			gpiosetdir(seg_sel[i], GPIO_OUTPUT, GPIO_HIGH);
		}
		for (i = 0; i < 8; i++) {
			gpioexport(seg_data[i]);
			gpiosetdir(seg_data[i], GPIO_OUTPUT, GPIO_LOW);
		}
		return 1;

	}

	public void close() {
		int i = 0;
		for (i = 0; i < 6; i++) {
			gpiounexport(seg_sel[i]);
		}
		for (i = 0; i < 8; i++) {
			gpiounexport(seg_data[i]);
		}
	}

	protected void finalize() throws Throwable {
		close();
		super.finalize();
	}

	public void write(int data) {
		segcontrol(data);
	}

	public void stop(int flag) {
		StopSegment(flag);
	}

	FndTask fndTask = null;

	public void startFndThread() {
		if (fndTask == null) {
			fndTask = new FndTask();
		}
		if (fndTask != null && !fndTask.isInterrupted())
			fndTask.start();
	}

	public void stopFndThread() {
		StopSegment(0);
		if (fndTask != null) {
			fndTask.finish();
			fndTask = null;
		}
	}

	public void setError() {
		fndTask.setState(111111);
	}

	public void setIntMode() {
		fndTask.setState(111112);
	}

	public void setRealMode() {
		fndTask.setState(111113);
	}

	private class FndTask extends Thread {

		private int state = 111113;
		// 111113 = real, 111112=int, 111111=error
		
		private AtomicInteger count;
		private boolean isRunning = true;
		
		public FndTask() {
			count = new AtomicInteger(0);
			isRunning = true;
		}

		@Override
		public void run() {
			while (isRunning) {
				try {
					Log.v("Segment", "Count : " + count.getAndIncrement());
					TimeUnit.MILLISECONDS.sleep(100);
					write(state);
				} catch (InterruptedException e) {
				}
			}
		}

		void setState(int state) {
			this.state = state;
		}
		
		void finish(){
			isRunning = false;
		}
	}
}
