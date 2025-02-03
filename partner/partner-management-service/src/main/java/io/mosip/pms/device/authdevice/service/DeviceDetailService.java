package io.mosip.pms.device.authdevice.service;

import io.mosip.pms.common.dto.PageResponseV2Dto;
import io.mosip.pms.common.response.dto.ResponseWrapperV2;
import io.mosip.pms.device.dto.DeviceDetailFilterDto;
import io.mosip.pms.device.dto.DeviceDetailSummaryDto;
import io.mosip.pms.partner.request.dto.SbiAndDeviceMappingRequestDto;
import io.mosip.pms.device.response.dto.DeviceDetailResponseDto;
import org.springframework.stereotype.Service;

import io.mosip.pms.common.dto.DeviceFilterValueDto;
import io.mosip.pms.common.dto.PageResponseDto;
import io.mosip.pms.device.exception.DeviceServiceException;
import io.mosip.pms.device.request.dto.DeviceDetailDto;
import io.mosip.pms.device.request.dto.DeviceDetailUpdateDto;
import io.mosip.pms.device.request.dto.DeviceSearchDto;
import io.mosip.pms.device.request.dto.UpdateDeviceDetailStatusDto;
import io.mosip.pms.device.request.dto.DeactivateDeviceRequestDto;
import io.mosip.pms.device.response.dto.DeviceDetailSearchResponseDto;
import io.mosip.pms.device.response.dto.FilterResponseCodeDto;
import io.mosip.pms.device.response.dto.IdDto;
import io.mosip.pms.device.response.dto.RegistrationSubTypeDto;

@Service
public interface DeviceDetailService {
	/**
	 * Function to save Device  Details to the Database
	 * 
	 * @param deviceDetails input from user deviceDetails DTO
	 * 
	 * @return IdResponseDto Device Details ID which is successfully inserted
	 * @throws DeviceServiceException if any error occurred while saving device
	 *                                    Specification
	 */
	public IdDto createDeviceDetails(DeviceDetailDto deviceDetails);

	/**
	 * Function to update Device Details
	 * 
	 * @param deviceDetails input from user deviceDetails DTO
	 * 
	 * @return IdResponseDto Device Details ID which is successfully updated
	 * @throws DeviceServiceException if any error occurred while updating
	 *                                    device Specification
	 */

	public IdDto updateDeviceDetails(DeviceDetailUpdateDto deviceDetails);
	
	/**
	 * Function to approve/reject device details
	 * 
	 * @param deviceDetails
	 * @return 
	 */
	public String updateDeviceDetailStatus(UpdateDeviceDetailStatusDto deviceDetails);	

	/**
	 * 
	 * @param <E>
	 * @param entity
	 * @param dto
	 * @return
	 */
	public <E> PageResponseDto<DeviceDetailSearchResponseDto> searchDeviceDetails(Class<E> entity, DeviceSearchDto dto);
	
	/**
	 * 
	 * @param <E>
	 * @param entity
	 * @param dto
	 * @return
	 */
	public <E> PageResponseDto<RegistrationSubTypeDto> searchDeviceType(Class<E> entity, DeviceSearchDto dto);
	
	/**
	 * 
	 * @param deviceFilterValueDto
	 * @return
	 */
	public FilterResponseCodeDto deviceFilterValues(DeviceFilterValueDto deviceFilterValueDto);
	
	/**
	 * 
	 * @param deviceFilterValueDto
	 * @return
	 */
	public FilterResponseCodeDto deviceSubTypeFilterValues(DeviceFilterValueDto deviceFilterValueDto);
	
	/**
	 * 
	 * @param deviceFilterValueDto
	 * @return
	 */
	public FilterResponseCodeDto deviceTypeFilterValues(DeviceFilterValueDto deviceFilterValueDto);

	public ResponseWrapperV2<DeviceDetailResponseDto> deactivateDevice(String deviceDetailId, DeactivateDeviceRequestDto requestDto);

    ResponseWrapperV2<PageResponseV2Dto<DeviceDetailSummaryDto>> getAllDeviceDetails(String sortFieldName, String sortType, Integer pageNo, Integer pageSize, DeviceDetailFilterDto filterDto);

	public ResponseWrapperV2<Boolean> approveOrRejectMappingDeviceToSbi(String deviceId, SbiAndDeviceMappingRequestDto requestDto);
}
