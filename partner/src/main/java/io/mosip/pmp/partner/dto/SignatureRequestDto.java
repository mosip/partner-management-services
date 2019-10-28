package io.mosip.pmp.partner.dto;

import javax.validation.constraints.NotBlank;

import io.mosip.pmp.partner.constant.KeymanagerConstant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Crypto-Manager-Request model
 * 
 * @author sanjeev.shrivastava
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SignatureRequestDto {
	/**
	 * Application id of decrypting module
	 */
	@NotBlank(message = KeymanagerConstant.INVALID_REQUEST)
	private String applicationId;
	/**
	 * Refrence Id
	 */
	private String referenceId;
	/**
	 * Timestamp
	 */
	@NotBlank(message = KeymanagerConstant.INVALID_REQUEST)
	private String timeStamp;
	/**
	 * Data in BASE64 encoding to encrypt/decrypt
	 */
	@NotBlank(message = KeymanagerConstant.INVALID_REQUEST)
	private String data;

}
