package com.android.downloadlib.processor.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.android.downloadlib.processor.entiry.ZThreadBean;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

public class ZDBManager {
    private static final String TAG = "DBUtils";
    private static ZDBManager sDbUtils;
    private DBHelper mDbHelper;
    private boolean mUseLitePal;
    private static final String whereClause = DBHelper.URL + " = ? and " + DBHelper.THREADID + " = ?";

    private static class Holder {
        static final ZDBManager INSTANCE = new ZDBManager();
    }

    public static ZDBManager getInstance() {
        return Holder.INSTANCE;
    }

    private ZDBManager() {
    }

    public ZDBManager config(Context context) {
        mDbHelper = new DBHelper(context);
        LitePal.initialize(context);
        return this;
    }

    public ZDBManager useLitePal(boolean useLitePal) {
        mUseLitePal = useLitePal;
        return this;
    }

    /**
     * normal
     */
    public synchronized void saveOrUpdate(ZThreadBean bean) {
        if (mUseLitePal) {
            bean.saveOrUpdate(whereClause, bean.url, bean.threadId + "");
        } else {
            if (!isLockInfoExsits(bean)) {
                //save
                SQLiteDatabase db = mDbHelper.getWritableDatabase();
                ContentValues values = new ContentValues();
                values.put(DBHelper.URL, bean.url);
                values.put(DBHelper.THREADID, bean.threadId);
                values.put(DBHelper.THREAD_LENGTH, bean.threadLength);
                values.put(DBHelper.THREAD_START, bean.startPos);
                values.put(DBHelper.THREAD_END, bean.endPos);
                db.insert(DBHelper.BOOK_TABLE, null, values);
            } else {
                // update
                SQLiteDatabase db = mDbHelper.getWritableDatabase();
                ContentValues values = new ContentValues();
                values.put(DBHelper.URL, bean.url);
                values.put(DBHelper.THREADID, bean.threadId);
                values.put(DBHelper.THREAD_LENGTH, bean.threadLength);
                values.put(DBHelper.THREAD_START, bean.startPos);
                values.put(DBHelper.THREAD_END, bean.endPos);

                db.update(DBHelper.BOOK_TABLE, values, whereClause,
                        new String[]{bean.url, bean.threadId + ""});
            }
        }

    }

    public synchronized void delete(ZThreadBean bean) {
        if (mUseLitePal) {
            LitePal.deleteAll(whereClause, bean.url, bean.threadId + "");
        } else {
            if (isLockInfoExsits(bean)) {
                SQLiteDatabase db = mDbHelper.getWritableDatabase();
                db.delete(DBHelper.BOOK_TABLE, whereClause, new String[]{bean.url, bean.threadId + ""});
            }
        }
    }

    public synchronized void deleteAll() {
        if (mUseLitePal) {
            LitePal.deleteAll(ZThreadBean.class);
        } else {
            for (ZThreadBean bean : getAllInfo()) {
                delete(bean);
            }
        }
    }


    public synchronized List<ZThreadBean> getAllInfo() {
        if (mUseLitePal) {
            return LitePal.findAll(ZThreadBean.class);
        } else {
            try {
                List<ZThreadBean> infos = new ArrayList<>();
                SQLiteDatabase db = mDbHelper.getReadableDatabase();
                Cursor cursor = db.query(DBHelper.BOOK_TABLE, null, null,
                        null, null, null, null, null);
                if (cursor.moveToFirst()) {
                    do {
                        ZThreadBean info = new ZThreadBean();
                        info.url = cursor.getString(cursor.getColumnIndex(DBHelper.URL));
                        info.threadId = cursor.getInt(cursor.getColumnIndex(DBHelper.THREADID));
                        info.threadLength = cursor.getInt(cursor.getColumnIndex(DBHelper.THREAD_LENGTH));
                        info.startPos = cursor.getInt(cursor.getColumnIndex(DBHelper.THREAD_START));
                        info.endPos = cursor.getInt(cursor.getColumnIndex(DBHelper.THREAD_END));
                        infos.add(info);
                    } while (cursor.moveToNext());

                }
                cursor.close();
                return infos;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    public synchronized boolean isLockInfoExsits(ZThreadBean bean) {
        if (mUseLitePal) {
            List<ZThreadBean> zThreadBeans = LitePal.where(whereClause, bean.url, bean.threadId + "").find(ZThreadBean.class);
            if (!zThreadBeans.isEmpty()){
                return true;
            }else{
                return false;
            }
        } else {
            try {
                SQLiteDatabase db = mDbHelper.getWritableDatabase();
                Cursor cursor = db.query(DBHelper.BOOK_TABLE, null, DBHelper.URL + " = ? and " +
                                DBHelper.THREADID + " = ?",
                        new String[]{bean.url, bean.threadId + ""}, null, null, null, null);
                if (cursor.moveToFirst()) {
                    cursor.close();
                    return true;
                }
                cursor.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }


}