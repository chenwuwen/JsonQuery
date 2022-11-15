package com.kanyun.sql.func;

/**
 * 函数类型枚举
 */
public enum FuncSourceType implements FuncSelection {
    MAVEN("maven"){
        @Override
        public AbstractFuncSource newInstance() throws InstantiationException, IllegalAccessException {
            return MavenFuncInstance.class.newInstance();
        }
    }, FILE("file"){
        @Override
        public AbstractFuncSource newInstance() throws InstantiationException, IllegalAccessException {
            return FileFuncInstance.class.newInstance();
        }
    };


    private String type;

    FuncSourceType(String type) {
        this.type = type;
    }


    @Override
    public AbstractFuncSource newInstance() throws InstantiationException, IllegalAccessException {
        return null;
    }
}
