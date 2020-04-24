package io.mosip.pmp.misp.dto;

import java.time.LocalDateTime;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 
 * @author Nagarjuna Kuchi <br/> 
 * @version 1.0 <br/>
 * @since 2019-Oct-11 <br/>
 * 
 * 
 * Defines an object to provide a wrapper for all common properties to  misp dto's. <br/>
 * All the misp dto's must extend from this class.<br/>
 */

@Data
@ApiModel(value = "base dto", description = "this class will contains metadata")
public class BaseDto  {

	//isActive	
	@ApiModelProperty(value = "isActive", required = true, dataType = "java.lang.Boolean")
	public Boolean isActive;


	// String createdBy 
	@ApiModelProperty(value = "createdBy", required = true, dataType = "java.lang.String")
	public String createdBy;

	// createdDateTime	
	@ApiModelProperty(value = "createdBy", required = true, dataType = "java.time.LocalDateTime")
	public LocalDateTime createdDateTime;

	//updatedBy
	@ApiModelProperty(value = "updatedBy", required = false, dataType = "java.lang.String")
	public String updatedBy;

	//updatedDateTime
	@ApiModelProperty(value = "updatedDateTime", required = false, dataType = "java.time.LocalDateTime")
	public LocalDateTime updatedDateTime;

	//isDeleted
	@ApiModelProperty(value = "isDeleted", required = false, dataType = "java.lang.Boolean")
	public Boolean isDeleted;

	//deletedDateTime
	@ApiModelProperty(value = "deletedDateTime", required = false, dataType = "java.time.LocalDateTime")
	public LocalDateTime deletedDateTime;

}

