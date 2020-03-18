package k8sbook.sampleapp.common.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.PrintWriter;
import java.io.StringWriter;

@RestControllerAdvice
public class SampleAppExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SampleAppExceptionHandler.class);

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public void handle(HttpMessageNotWritableException e) {
        var stringWriter = new StringWriter();
        var writer = new PrintWriter(stringWriter);
        e.printStackTrace(writer);
        var stacktrace = stringWriter.toString();
        LOGGER.info(stacktrace);
    }

}
