package io.mosip.pmp.misp.constant;

/**
 * Property constant for MISPID generator.
 * 
 * @author Nagarjuna
 * @since 1.0.0
 *
 */
public enum MispIdPropertyConstant {

	ID_BASE("10");

	/**
	 * The property of MISPID generator.
	 */
	private String property;

	/**
	 * Getter for property.
	 * 
	 * @return the property.
	 */
	public String getProperty() {
		return property;
	}

	/**
	 * Constructor for MispIdPropertyConstant.
	 * 
	 * @param property the property.
	 */
	MispIdPropertyConstant(String property) {
		this.property = property;
	}

}
