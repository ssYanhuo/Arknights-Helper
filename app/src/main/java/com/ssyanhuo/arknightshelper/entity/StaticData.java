package com.ssyanhuo.arknightshelper.entity;

import android.icu.util.ChineseCalendar;

public class StaticData {
    public static class Exp{
        public static class Book{
            public static final int EXP_BOOK_1 = 200;
            public static final int EXP_BOOK_2 = 400;
            public static final int EXP_BOOK_3 = 1000;
            public static final int EXP_BOOK_4 = 2000;
        }
        public static class ExpLevel{
            public static final int LS_5 = 7400;
        }
        public static class MoneyLevel{
            public static final int CE_5 = 7500;
            public static final int LS_5 = 360;
        }
        public static class Stamina{
            public static final int CE_5 = 30;
            public static final int LS_5 = 30;
        }
        public static class Limit {
            public static class Stage {
                public static final int STAR_1 = 0;
                public static final int STAR_2 = 0;
                public static final int STAR_3 = 1;
                public static final int STAR_4 = 2;
                public static final int STAR_5 = 2;
                public static final int STAR_6 = 2;
            }
        }
    }
    public static class HR{
        public static String[] tagList = {
                "新手",
                "资深干员",
                "高级资深干员",
                "近战位",
                "远程位",
                "男性干员",
                "女性干员",
                "先锋干员",
                "狙击干员",
                "医疗干员",
                "术师干员",
                "近卫干员",
                "重装干员",
                "辅助干员",
                "特种干员",
                "治疗",
                "支援",
                "输出",
                "群攻",
                "减速",
                "生存",
                "防护",
                "削弱",
                "位移",
                "控场",
                "爆发",
                "召唤",
                "快速复活",
                "费用回复",
                "支援机械"
        };
    }
    public static class Const{
        static public String PREFERENCE_PATH = "com.ssyanhuo.arknightshelper_preferences";
        static public String PACKAGE_OFFICIAL = "com.hypergryph.arknights";
        static public String PACKAGE_BILIBILI = "com.hypergryph.arknights.bilibili";
        static public String PACKAGE_ENGLISH = "com.YoStarEN.Arknights";
        static public String PACKAGE_JAPANESE = "com.YoStarJP.Arknights";
        static public String PACKAGE_KOREAN = "com.YoStarKR.Arknights";
        static public String PACKAGE_MANUAL = "manual";
        static public String[] PACKAGE_LIST = {PACKAGE_OFFICIAL, PACKAGE_BILIBILI, PACKAGE_ENGLISH, PACKAGE_JAPANESE, PACKAGE_KOREAN};
        static public String[] DATA_LIST = {
                "akhr.json",
                "aklevel.json",
                "charMaterials.json",
                "datainfo.json",
                "items.json",
                "material.json",
                "matrix.json",
                "stages.json",
                "i18n.json",
                "formula.json",
                "versioninfo.json"
        };
    }
}

