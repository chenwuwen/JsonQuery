package com.kanyun.sql.func;

/**
 * 函数类型枚举
 * 枚举类型实现接口,则每个枚举实例都需要实现接口的方法
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


    public String getType() {
        return type;
    }

    @Override
    public AbstractFuncSource newInstance() throws InstantiationException, IllegalAccessException {
        return null;
    }

    /**
     * 根据type获取枚举实例
     * @param type
     * @return
     */
    public static FuncSourceType getInstanceFromType(String type) {
        for (FuncSourceType value : FuncSourceType.values()) {
            if (value.getType().equals(type)) {
                return value;
            }
        }
        return null;
    }
}
