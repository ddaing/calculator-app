package kr.ac.ajou.esd.calculator;

import kr.ac.ajou.esd.calculator.jni.SegmentDriver;
import kr.ac.ajou.esd.calculator.jni.SwitchDriver;
import kr.ac.ajou.esd.calculator.jni.SwitchListener;
import kr.ac.ajou.esd.calculator.jni.TextLCDDriver;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.Toast;

/**
 * 
 * App 실행시 최초로 실행되는 main activity. 보드에 있는 Switch 의 동작 상태를 수신받기 위한 interface인
 * SwitchListener 를 상속받아 구현한다.
 * 
 */
public class MainActivity extends Activity implements SwitchListener {
	private static final String TAG = MainActivity.class.getSimpleName();

	private StringBuilder mMathExpr; // 사용자가 입력한 수식을 저장하는 변수
	private HistoryAdapter mAdapter; // 사용자가 계산한 결과들을 보여주는 ListView의 Adapter
	private DBHelper mDb; // 사용자가 계산했던 내역을 CRD 하기위한 DB Helper

	// FND 드라이버. 모드, 에러표시를 위해 사용.
	private SegmentDriver mSegmentDriver = new SegmentDriver();
	// Switch 드라이버. 모드 변환, TextLCD 에서 커서이동을 하기 위해 사용.
	private SwitchDriver mSwitchDriver = new SwitchDriver();
	// Text LCD 드라이버. 사용자가 입력한 값 및 계산결과를 보여주기 위해 사용.
	private TextLCDDriver mTextLCDDriver = new TextLCDDriver();

	// Switch 드라이버에서
	private int cnt = 1;
	// 계산기의 현재 모드 flag(true=실수모드, false=정수모드)
	private boolean isRealMode = true;
	// Text LCD 에서 cursor 의 position
	private int cursorPointer = 1;

	// 화면에 있는 버튼 ID 배열
	private int[] btns = { R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3,
			R.id.btn4, R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9,
			R.id.btn_add, R.id.btn_and, R.id.btn_bracket1, R.id.btn_bracket2,
			R.id.btn_clear, R.id.btn_del, R.id.btn_div, R.id.btn_left_shift,
			R.id.btn_mul, R.id.btn_or, R.id.btn_result, R.id.btn_right_shift,
			R.id.btn_sqrt, R.id.btn_sub, R.id.btn_dot, R.id.btn_pow};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mMathExpr = new StringBuilder(256);

		// 앱이 실행될 때 savedInstanceState 에 저장되어 있는 수식을 불러온다.
		if (savedInstanceState != null) {
			String expr = savedInstanceState.getString("MathExpr");
			if (expr != null) {
				mMathExpr.append(expr);
			}
		}

		// button 들에 click listener 를 달아준다.
		for (int i = 0; i < btns.length; i++) {
			findViewById(btns[i]).setOnClickListener(listener);
		}

		// DBHalper 초기화
		mDb = new DBHelper(this);

		// ListView의 Adapter를 초기화 해주면서 mDb.getAll() 을 호출해 SQLite에 저장되어 있는 값도
		// 초기값으로 넘겨준다.
		mAdapter = new HistoryAdapter(this, R.layout.item_history,
				mDb.getAll(), mDb, new HistoryAdapter.CopyClickListener() {
					
					@Override
					public void onCopy(HistoryVO vo) {
						resetLCD();
						String resultCopy = vo.getResult();
						mTextLCDDriver.setText(TextLCDDriver.FIRST_LINE, resultCopy);
						mTextLCDDriver.setCursorHome();
						mMathExpr.append(resultCopy);
					
						if (cursorPointer > mMathExpr.length() + 1)
							cursorPointer = mMathExpr.length() + 1;
						if (cursorPointer < 1)
							cursorPointer = 1;
						for (int i = 1; i < resultCopy.length(); i++) {
							mTextLCDDriver.cursorRight();
						}
						cursorPointer = resultCopy.length()+1;
						showToast("TextLCD로 복사되었습니다.");
					}
				});
		ListView lv = (ListView) findViewById(R.id.lv_history);
		lv.setAdapter(mAdapter);

		// Switch 드라이버에 현재의 Activity 를 넘겨줘 Event 를 수신 받는다.
		mSwitchDriver.setListener(this);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mMathExpr.length() > 0) {
			outState.putString("MathExpr", mMathExpr.toString());
		}
	}

	@Override
	protected void onResume() {
		// 앱이 시작 혹은 재시작되면 위에서 선언한 3개의 드라이버를 open 해준다.
		if (mSegmentDriver.open() < 0) {
			Toast.makeText(MainActivity.this, "Segment Driver Open Failed",
					Toast.LENGTH_SHORT).show();
		}else{
			// FND 스레드를 실행시킨다.
			mSegmentDriver.startFndThread();
		}
		if (mSwitchDriver.open() < 0) {
			Toast.makeText(MainActivity.this, "Switch Driver Open Failed",
					Toast.LENGTH_SHORT).show();
		}
		if (mTextLCDDriver.open() < 0) {
			Toast.makeText(MainActivity.this, "Text LCD Driver Open Failed",
					Toast.LENGTH_SHORT).show();
		}
		super.onResume();
	}

	@Override
	protected void onPause() {
		// 앱이 시작 혹은 중지되면 위에서 선언한 3개의 드라이버를 close 해준다.
		mSegmentDriver.stopFndThread();
		mSegmentDriver.close();
		mSwitchDriver.close();
		mTextLCDDriver.close();
		super.onPause();

	}

	private OnClickListener listener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			mTextLCDDriver.setCursorVisible(0);
			mTextLCDDriver.setCursorHome();
			switch (v.getId()) {
			case R.id.btn0:
				mMathExpr.insert(cursorPointer - 1, "0");
				break;
			case R.id.btn1:
				mMathExpr.insert(cursorPointer - 1, "1");
				break;
			case R.id.btn2:
				mMathExpr.insert(cursorPointer - 1, "2");
				break;
			case R.id.btn3:
				mMathExpr.insert(cursorPointer - 1, "3");
				break;
			case R.id.btn4:
				mMathExpr.insert(cursorPointer - 1, "4");
				break;
			case R.id.btn5:
				mMathExpr.insert(cursorPointer - 1, "5");
				break;
			case R.id.btn6:
				mMathExpr.insert(cursorPointer - 1, "6");
				break;
			case R.id.btn7:
				mMathExpr.insert(cursorPointer - 1, "7");
				break;
			case R.id.btn8:
				mMathExpr.insert(cursorPointer - 1, "8");
				break;
			case R.id.btn9:
				mMathExpr.insert(cursorPointer - 1, "9");
				break;
			case R.id.btn_dot:
				mMathExpr.insert(cursorPointer - 1, ".");
				break;
			case R.id.btn_add:
				mMathExpr.insert(cursorPointer - 1, "+");
				break;
			case R.id.btn_sub:
				mMathExpr.insert(cursorPointer - 1, "-");
				break;
			case R.id.btn_mul:
				mMathExpr.insert(cursorPointer - 1, "*");
				break;
			case R.id.btn_div:
				mMathExpr.insert(cursorPointer - 1, "/");
				break;
			case R.id.btn_and:
				mMathExpr.insert(cursorPointer - 1, "&");
				break;
			case R.id.btn_or:
				mMathExpr.insert(cursorPointer - 1, "|");
				break;
			case R.id.btn_left_shift:
				mMathExpr.insert(cursorPointer - 1, "<");
				break;
			case R.id.btn_right_shift:
				mMathExpr.insert(cursorPointer - 1, ">");
				break;
			case R.id.btn_bracket1:
				mMathExpr.insert(cursorPointer - 1, "(");
				break;
			case R.id.btn_bracket2:
				mMathExpr.insert(cursorPointer - 1, ")");
				break;
			case R.id.btn_sqrt:
				mMathExpr.insert(cursorPointer - 1, "√");
				break;
			case R.id.btn_pow:
				mMathExpr.insert(cursorPointer - 1, "^");
				break;
			case R.id.btn_del:
				if (mMathExpr.length() > 0 && cursorPointer > 1) {

					mMathExpr.deleteCharAt(cursorPointer - 2);
					cursorPointer -= 2;

				}
				break;
			case R.id.btn_clear:
				if (mMathExpr.length() > 0)
					mMathExpr.delete(0, mMathExpr.length());
				mTextLCDDriver.clearDisplay();
				cursorPointer = 1;
				break;

			case R.id.btn_result:
				mTextLCDDriver.setCursorHome();
				if (mMathExpr.length() == 0)
					return;
				String exp = mMathExpr.toString();
				String result1;
				if (isRealMode) {
					try {
						result1 = CalUtil.calcDouble(exp);
					} catch (Exception e) {
						showError();
						return;
					}
				} else {
					try {
						result1 = CalUtil.calcInt(exp);
					} catch (Exception e) {
						showError();
						return;
					}
				}
				if (result1 != null) {
					mMathExpr.delete(0, mMathExpr.length());
					mMathExpr.append(result1);
					mTextLCDDriver.setText(TextLCDDriver.SECOND_LINE, result1);

					HistoryVO vo = new HistoryVO(exp, result1);
					mDb.insert(vo);
					mAdapter.add(vo);
					mAdapter.notifyDataSetChanged();
				}
				break;
			}
			if (v.getId() != R.id.btn_result) {
				mTextLCDDriver.setText(
						TextLCDDriver.FIRST_LINE,
						mMathExpr.toString().trim()
								.replace(getString(R.string.sqrt), "V"));
			}
			if (v.getId() != R.id.btn_result || v.getId() != R.id.btn_del
					|| v.getId() != R.id.btn_clear) {
				cursorPointer++;
			}
			mTextLCDDriver.setCursorHome();
			for (int i = 1; i < cursorPointer - 1; i++) {
				mTextLCDDriver.cursorRight();
			}
			if (cursorPointer > mMathExpr.length() + 1)
				cursorPointer = mMathExpr.length() + 1;
			if (cursorPointer < 1)
				cursorPointer = 1;
		}
	};

	public Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (cnt) {

			case 1:
				// UP Switch, Cursor 를 맨 앞으로 옮긴다.
				if (msg.arg1 == 1) {
					mTextLCDDriver.setCursorHome();
					cursorPointer = 2;
				}
				cnt++;
				break;
			case 2:
				// DOWN Switch, Cursor 를 맨 뒤로 옮긴다.
				if (msg.arg1 == 1) {
					mTextLCDDriver.setCursorHome();
					cursorPointer = mMathExpr.length() + 1;
					for (int i = 1; i < cursorPointer - 1; i++) {
						mTextLCDDriver.cursorRight();
					}

				}

				cnt++;
				break;
			case 3:
				// LEFT, Cursor를 왼쪽으로 이동
				if (msg.arg1 == 1) {
					if (cursorPointer != 0) {
						cursorPointer--;
						mTextLCDDriver.cursorLeft();
					}
				}
				cnt++;
				break;
			case 4:
				// RIGHT, Cursor를 왼쪽으로 이동
				if (msg.arg1 == 1) {
					if (cursorPointer != 18
							&& cursorPointer < mMathExpr.length() + 1) {
						cursorPointer++;
						mTextLCDDriver.cursorRight();
					}
				}
				cnt++;
				break;
			case 5:
				// CENTER, 모드 변경
				if (msg.arg1 == 1) {
					isRealMode = !isRealMode;
					resetLCD();
					setMode(isRealMode);
					if (isRealMode) {
						mSegmentDriver.setRealMode();
					} else {
						mSegmentDriver.setIntMode();
					}
					
			
				}

				cnt = 1;
				break;
			}

		}
	};

	/**
	 * 계산하다가 Exception 발생시 FND 에서 Error 를 표시해주기 위한 메서드
	 */
	private void showError() {
		showToast("수식 오류!!");

		mSegmentDriver.setError();
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {}
		if (isRealMode) {
			mSegmentDriver.setRealMode();
		} else {
			mSegmentDriver.setIntMode();
		}
	}

	@Override
	public void onReceive(int val) {
		Message text = Message.obtain();
		text.arg1 = val;
		handler.sendMessage(text);
	}

	
	//모드에 따라 layout 을 다르게 보여주게 하기 위한 메서드.
	private void setMode(boolean isReal){
		realMode(!isReal);
		intMode(isReal);
	}

	private void realMode(boolean isVisible) {
		int[] btns = new int[] { R.id.btn_left_shift, R.id.btn_right_shift,
				R.id.btn_and, R.id.btn_or };
		for (int i : btns) {
			findViewById(i).setVisibility(
					isVisible ? View.VISIBLE : View.INVISIBLE);
		}
	}

	private void intMode(boolean isVisible) {
		int[] btns = new int[] { R.id.btn_dot };
		for (int i : btns) {
			findViewById(i).setVisibility(
					isVisible ? View.VISIBLE : View.INVISIBLE);
		}
	}
	
	/**
	 * LCD를 Clear 해주고 계산식 buffer 를 비워준다.
	 */
	private void resetLCD(){
		mTextLCDDriver.clearDisplay();
        if (mMathExpr.length() > 0)
            mMathExpr.delete(0, mMathExpr.length());
		cursorPointer = 1;
	}

	// Toast를 간편하게 호출하게 하기 위한 메서드.
	private void showToast(String msg) {
		Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
	}
}
