package codeanalysis.saengine.util;

/**
 * @author Ran Zhang
 * @since 2024/4/12
 */
public enum ErrorEnums implements codeanalysis.saengine.util.Errors {

    /***********系统异常************/
    SERVER_ERR0R("500", "服务异常"),
    REPEAT_SUBMIT_ERR0R("505", "重复提交");

    private String code;
    private String name;

    ErrorEnums(String code, String name) {
        this.code = code;
        this.name = name;
    }


    public String toString() {
        return code;
    }

    @Override
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
