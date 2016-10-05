package com.capstone.locker.database;

import android.provider.BaseColumns;

/**
 * Created by parkkyounghyun
 */
public final class DataBases {

    public static final class CreateDB implements BaseColumns {
        public static final String IDENTIFICATIONNAME = "ident_name";
        public static final String IDENTIFICATIONNUM = "ident_num";
        public static final String NICKNAME = "nickname";
        public static final String QUALIFICATION = "qualificaion";
        public static final String OWNERPWD = "owner_pwd";
        public static final String QUESTPWD = "quest_pwd";
        public static final String ICON = "icon";
        public static final String CREATED = "created";
        public static final String PUSHCHECK = "pushcheck";
        public static final String _TABLENAME = "moduleinfo";
        // id name number time image
        public static final String _CREATE =
                "create table "+_TABLENAME+"("
                        +_ID+" integer primary key autoincrement, "
                        +IDENTIFICATIONNAME+" varchar(25) not null , "
                        +IDENTIFICATIONNUM+" int not null , "
                        +NICKNAME+" varchar(10) not null , "
                        +QUALIFICATION+" varchar(10) not null , "
                        +OWNERPWD+" varchar(20) not null , "
                        +QUESTPWD+" varchar(20) not null , "
                        +ICON+" int not null , "
                        +PUSHCHECK+" int not null , "
                        +CREATED+" datetime not null );";

    }

}
