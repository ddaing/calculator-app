package kr.ac.ajou.esd.calculator.jni;

import java.util.concurrent.TimeUnit;

import android.util.Log;

public class SwitchDriver implements SwitchListener{

	int switch_control[]= {11,14,15,26,22};
	private boolean mConnectFlag;

	private TranseThread mTranseThread;
	private  SwitchListener mMainActivity;
	static {
		System.loadLibrary("JNIDriver");
	}
	
	private native static int gpioexport(int gpio);
	private native static int gpiounexport(int gpio);
	private native static int gpiogetval(int gpio);
 

	public SwitchDriver(){
		mConnectFlag = false;
	}
	@Override
	public void onReceive(int val) {
		if(mMainActivity!=null){
			mMainActivity.onReceive(val);
			
		}
	}
	public void setListener(SwitchListener a){
		mMainActivity = a;
	}
	public int open(){
		int i=0;
		if(mConnectFlag) return -1;

		for(i=0; i<5;i++)
			if(gpioexport(switch_control[i]) != 0)
				return -1;
		mConnectFlag = true;
		mTranseThread = new TranseThread();
		mTranseThread.start();
		return 0;
		
	}
	
	public void close(){
		int i=0;
		if(!mConnectFlag) return;
		mConnectFlag = false;
		for(i=0; i<5;i++)
		gpiounexport(switch_control[i]);
	}

	protected void finalize() throws Throwable{
		close();
		super.finalize();
	}
	private class TranseThread extends Thread {

		int i=0;
		@Override
		public void run() {
			super.run();
			try {
				while(mConnectFlag){
					try {
						for(i=0;i<5;i++)
							onReceive(gpiogetval(switch_control[i]));
						TimeUnit.MILLISECONDS.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}catch(Exception e){
				
			}
		}
	}
}
