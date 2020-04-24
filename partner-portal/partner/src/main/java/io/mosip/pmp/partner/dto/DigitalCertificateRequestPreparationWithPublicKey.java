package io.mosip.pmp.partner.dto;

import javax.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DigitalCertificateRequestPreparationWithPublicKey {
	
	@NotBlank
	private String signature;
	@NotBlank
	private String data;
	@NotBlank
	private String publickey;
}
