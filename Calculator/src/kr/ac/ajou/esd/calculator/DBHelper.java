package kr.ac.ajou.esd.calculator;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 안드로이드 내장 DB 인 SQLite 에 접근해서 CRD를 수행하는 Helper. 
 *
 */
public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "history.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TB_HISTORY_NAME = "TB_HISTORY";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(String.format("CREATE TABLE %s (_id INTEGER PRIMARY KEY AUTOINCREMENT, uuid TEXT, expression TEXT, result TEXT);", TB_HISTORY_NAME));
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(String.format("DROP TABLE IF EXITS %s", TB_HISTORY_NAME));
        onCreate(db);
    }

    /**
     * 계산이 완료되면 History table 에 데이터를 Create 하는 메서드  
     * @param vo 계산정보 객체
     */
    public void insert(HistoryVO vo) {
        ContentValues values = new ContentValues();
        values.put("uuid", vo.getUuid());
        values.put("expression", vo.getExpression());
        values.put("result", vo.getResult());
        getWritableDatabase().insert(TB_HISTORY_NAME, null, values);
    }

    /**
     * 계산했던 내역을 History table 에서 지우는 메서드
     * UUID (Key) 값을 이용해 삭제한다.
     * @param vo 계산정보 객체
     */
    public void delete(HistoryVO vo) {
        getWritableDatabase().delete(TB_HISTORY_NAME, "uuid=?", new String[]{vo.getUuid()});
    }

    /**
     * 계산했던 전체 내역을 History table 에서 불러오는 메서드
     * @return
     */
    public List<HistoryVO> getAll() {
        List<HistoryVO> list = new ArrayList<HistoryVO>();

        Cursor cursor = getReadableDatabase().rawQuery("SELECT * FROM TB_HISTORY ORDER BY _id ASC", null);
        while (cursor.moveToNext()) {
            list.add(new HistoryVO(
                            cursor.getString(cursor.getColumnIndex("uuid")),
                            cursor.getString(cursor.getColumnIndex("expression")),
                            cursor.getString(cursor.getColumnIndex("result"))
                    )
            );
        }
        cursor.close();
        return list;
    }
}
