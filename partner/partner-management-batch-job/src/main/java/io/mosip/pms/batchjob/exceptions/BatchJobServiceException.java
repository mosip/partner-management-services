package io.mosip.pms.batchjob.exceptions;

import io.mosip.kernel.core.exception.BaseUncheckedException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BatchJobServiceException extends BaseUncheckedException {
    /**
	 * 
	 */
	private static final long serialVersionUID = 8201268577451936423L;
	private final HttpStatus status;

    /**
     * Constructor to initialize CustomException
     *
     * @param errorCode    The error code for this exception
     * @param errorMessage The error message for this exception
     */
    public BatchJobServiceException(String errorCode, String errorMessage) {
        super(errorMessage);
        addInfo(errorCode, errorMessage);
        this.status = HttpStatus.BAD_REQUEST;
    }

    /**
     * Constructor to initialize CustomException with root cause
     *
     * @param errorCode    The error code for this exception
     * @param errorMessage The error message for this exception
     * @param rootCause    The specified cause
     */
    public BatchJobServiceException(String errorCode, String errorMessage, Throwable rootCause) {
        super(errorCode, errorMessage, rootCause);
        this.status = HttpStatus.BAD_REQUEST;
    }
}
