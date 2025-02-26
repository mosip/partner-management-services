package io.mosip.pms.partner_batch_job.exceptions;

import io.mosip.kernel.core.exception.BaseUncheckedException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Custom Exception Class for PMS application.
 *
 * @see io.mosip.kernel.core.exception.BaseUncheckedException
 */
@Getter
public class PartnerServiceException extends BaseUncheckedException {
    private static final long serialVersionUID = 687991492884005033L;
    private final HttpStatus status;

    /**
     * Constructor to initialize CustomException
     *
     * @param errorCode    The error code for this exception
     * @param errorMessage The error message for this exception
     */
    public PartnerServiceException(String errorCode, String errorMessage) {
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
    public PartnerServiceException(String errorCode, String errorMessage, Throwable rootCause) {
        super(errorCode, errorMessage, rootCause);
        this.status = HttpStatus.BAD_REQUEST;
    }
}
