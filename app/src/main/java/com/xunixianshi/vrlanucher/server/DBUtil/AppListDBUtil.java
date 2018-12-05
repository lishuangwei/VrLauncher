package com.xunixianshi.vrlanucher.server.DBUtil;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.activeandroid.query.Update;
import com.hch.utils.MLog;
import com.xunixianshi.vrlanucher.server.DBUtil.DbObj.AppDetialObj;

import java.util.List;

/**
 * Created by Administrator on 2017/10/6.
 */

public class AppListDBUtil {
    public static int inster(AppDetialObj appDetialObj){
        ActiveAndroid.beginTransaction();
        try {
            appDetialObj.save();
            ActiveAndroid.setTransactionSuccessful();
            return 0;
        }catch (Exception e){
            ActiveAndroid.endTransaction();
            return 1;
        }
        finally {
            ActiveAndroid.endTransaction();
        }
    }
    public static int insterList(List<AppDetialObj> appDetialObjs){
        ActiveAndroid.beginTransaction();
        try {
            for(AppDetialObj appDetialObj : appDetialObjs){
                appDetialObj.save();
            }
            ActiveAndroid.setTransactionSuccessful();
            return 0;
        }catch (Exception e){
            ActiveAndroid.endTransaction();
            return 1;
        }
        finally {
            ActiveAndroid.endTransaction();
        }
    }

    //查询所有应用
    public static List<AppDetialObj> selectAll(){
        return new Select()
                .from(AppDetialObj.class)
                .where("app_type in ('0','1','2','3')")
                .execute();
    }
    //根据包名查询应用
    public static AppDetialObj selectByPackageName(String packageName){
        return new Select()
                .from(AppDetialObj.class)
                .where("app_package_name = ?", packageName)
                .executeSingle();
    }
    //根据类型查询应用列表
    public static List<AppDetialObj> selectByCndition(String type){
        return new Select()
                .from(AppDetialObj.class)
                .where("app_type = ?", type)
                .execute();
    }
    //根据类型查询应用列表
    public static List<AppDetialObj> selectByCndition(String type,String typeOther){
        return new Select()
                .from(AppDetialObj.class)
                .where("app_type in ('"+type+"','"+typeOther+"')")
                .execute();
    }
    //更新指定应用类型
    public static int updataAppTypeByPackageName(String packageName,String type){
        MLog.d("更新指定应用类型:"+type);
        ActiveAndroid.beginTransaction();
        try {
            Update update = new Update(AppDetialObj.class);
            update.set("app_type = "+type).where("app_package_name = '"+packageName+"'").execute();
            ActiveAndroid.setTransactionSuccessful();
            return 0;
        }catch (Exception e){
            MLog.d("更新指定应用类型失败了");
            ActiveAndroid.endTransaction();
            return 1;
        }
        finally {
            ActiveAndroid.endTransaction();
        }
    }
    //根据包名删除应用
    public static int deleteByPackageName(String packageName){
        ActiveAndroid.beginTransaction();
        try {
            new Delete().from(AppDetialObj.class).where("app_package_name = ?", packageName).execute();
            ActiveAndroid.setTransactionSuccessful();
            return 0;
        }catch (Exception e){
            ActiveAndroid.endTransaction();
            return 1;
        }
        finally {
            ActiveAndroid.endTransaction();
        }
    }
}
