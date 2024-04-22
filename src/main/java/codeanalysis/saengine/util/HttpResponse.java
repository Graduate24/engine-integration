package codeanalysis.saengine.util;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;

/**
 * @author Ran Zhang
 * @since 2024/4/12
 */
@Slf4j
@Data
public final class HttpResponse<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    private String code;
    private String msg;
    private String subCode;
    private String toast;
    private long timestamp = System.currentTimeMillis();
    private T result;


    public HttpResponse() {

    }


    public HttpResponse(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public HttpResponse(String code, String msg, T result) {
        this.code = code;
        this.msg = msg;
        this.result = result;
    }

    /**
     * 返回错误信息
     *
     * @return
     */
    public static <T> HttpResponse<T> error(String ret, String msg) {
        return new HttpResponse<T>(ret, msg);
    }

    public static <T> HttpResponse<T> error(Errors errors) {
        return new HttpResponse<T>(errors.getCode(), errors.getName());
    }

    public static <T> HttpResponse<T> error(Exception errors) {
        String msg = errors.getMessage();
        return new HttpResponse<T>("500", msg.length() < 200 ? msg : msg.substring(0, 200));
    }

    /**
     * <p>
     * 直接返回成功
     * </p>
     *
     * @return
     */
    public static <T> HttpResponse<T> success() {
        return new HttpResponse<T>(HttpStatus.OK.toString(), HttpStatus.OK.name());
    }

    /**
     * 返回成功信息 此方法将传入参数装到result中，因此只有返回业务数据时调用此方法
     *
     * @return
     */

    public static <T> HttpResponse<T> success(T result) {
        return new HttpResponse<>(HttpStatus.OK.toString(), HttpStatus.OK.name(), result);
    }

}
