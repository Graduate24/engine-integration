package codeanalysis.saengine.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;


/**
 * @author Ran Zhang
 * @since 2024/4/12
 */
@ControllerAdvice
@Slf4j
public class ExceptionAdvice {


    /**
     * 全局异常捕捉处理
     *
     * @param ex
     * @return
     */
    @ResponseBody
    @ExceptionHandler(value = Exception.class)
    public HttpResponse errorHandler(Exception ex) {
        log.error("global error", ex);
        return HttpResponse.error(ex);
    }

}
