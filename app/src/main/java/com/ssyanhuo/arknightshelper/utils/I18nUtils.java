package com.ssyanhuo.arknightshelper.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.WindowManager;

import com.alibaba.fastjson.JSONObject;
import com.ssyanhuo.arknightshelper.entity.StaticData;

public class I18nUtils {
    final static public String CATEGORY_NAME = "name";
    final static public String CATEGORY_TAG = "tag";
    final static public String LANGUAGE_SIMPLIFIED_CHINESE = "zh-CN";
    final static public String LANGUAGE_JAPANESE = "jp";
    final static public String LANGUAGE_ENGLISH = "en";
    final static public String FILTER_ALL = "all";
    final static public String FILTER_HR = "hr";
    final private String TAG = "I18nUtils";
    public Helper getHelper(Context context, String category) throws Exception {
        return new Helper(context, category);
    }
    public class Helper {
        JSONObject i18nJSON;
        private Context context;
        private String category;
        SharedPreferences preferences;
        private boolean hidden;
        public Helper(Context context, String category) throws Exception{
            this.context = context;
            this.category = category;
            i18nJSON = JSONUtils.getJSONObject(context, FileUtils.readData("i18n.json", context)).getJSONObject(category);
            //Log.e(TAG, i18nJSON.toJSONString());
            preferences = context.getSharedPreferences(StaticData.Const.PREFERENCE_PATH, Context.MODE_PRIVATE);
        }
        public String get(String cnStr, String lang){
            try{
                if (lang.equals(LANGUAGE_SIMPLIFIED_CHINESE)){
                    return cnStr;
                }else {
                    //Log.e(TAG, i18nJSON.getJSONObject(cnStr).toJSONString());
                    String result = i18nJSON.getJSONObject(cnStr).getString(lang);
                    if (result == null || result.equals("")){
                        return cnStr;
                    }
                    return result;
                }
            }catch (Exception e){
                //e.printStackTrace();
                return cnStr;
            }
        }

        public String get(String cnStr){
            String lang = preferences.getString("game_language", LANGUAGE_SIMPLIFIED_CHINESE);
            return get(cnStr, lang);
        }

        public String getCategory() {
            return category;
        }

        public JSONObject getI18nJSON() {
            return i18nJSON;
        }

        public boolean isHidden(String cnStr, String filter){
            String lang = preferences.getString("game_language", LANGUAGE_SIMPLIFIED_CHINESE);
            return isHidden(cnStr, filter, lang);
        }
        public boolean isHidden(String cnStr, String filter, String lang){
            if (lang.equals(LANGUAGE_SIMPLIFIED_CHINESE)){
                return false;
            }
            try {
                if (i18nJSON.getJSONObject(cnStr).getString("hidden") == null || i18nJSON.getJSONObject(cnStr).getString("hidden").equals("")){
                    return false;
                }else if (i18nJSON.getJSONObject(cnStr).getString("hidden").equals(FILTER_ALL)){
                    return true;
                }else {
                    return i18nJSON.getJSONObject(cnStr).getString("hidden").equals(filter);
                }
            }catch (Exception e){
                Log.e(TAG, "Translation of " + cnStr +" not found!");
                return true;
            }
        }
    }
}
