package io.mosip.pms.device.authdevice.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="reg_device_type")
public class RegistrationDeviceType implements Serializable {
		private static final long serialVersionUID = 1L;
		
		@Id
		@Column(name="code",length=36,nullable=false)
		private String code;
		
		@Column(name="name",length=64,nullable=false)
		private String name;
		
		@Column(name="descr",length=512,nullable=false)
		private String desciption;
		
		@Column(name="is_active",nullable=false)
		private boolean isActive;
		
		@Column(name="is_deleted")
		private boolean isDeleted;
		
		@Column(name="cr_by",length=256,nullable=false)
		private String crBy;

		@Column(name="cr_dtimes",nullable=false)
		private LocalDateTime crDtimes;

		@Column(name="del_dtimes")
		private LocalDateTime delDtimes;
		
		@Column(name="upd_by",length=256)
		private String updBy;

		@Column(name="upd_dtimes")
		private LocalDateTime updDtimes;

		public String getCode() {
			return code;
		}

		public void setCode(String code) {
			this.code = code;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getDesciption() {
			return desciption;
		}

		public void setDesciption(String desciption) {
			this.desciption = desciption;
		}

		public boolean isActive() {
			return isActive;
		}

		public void setActive(boolean isActive) {
			this.isActive = isActive;
		}

		public boolean isDeleted() {
			return isDeleted;
		}

		public void setDeleted(boolean isDeleted) {
			this.isDeleted = isDeleted;
		}

		public String getCrBy() {
			return crBy;
		}

		public void setCrBy(String crBy) {
			this.crBy = crBy;
		}

		public LocalDateTime getCrDtimes() {
			return crDtimes;
		}

		public void setCrDtimes(LocalDateTime crDtimes) {
			this.crDtimes = crDtimes;
		}

		public LocalDateTime getDelDtimes() {
			return delDtimes;
		}

		public void setDelDtimes(LocalDateTime delDtimes) {
			this.delDtimes = delDtimes;
		}

		public String getUpdBy() {
			return updBy;
		}

		public void setUpdBy(String updBy) {
			this.updBy = updBy;
		}

		public LocalDateTime getUpdDtimes() {
			return updDtimes;
		}

		public void setUpdDtimes(LocalDateTime updDtimes) {
			this.updDtimes = updDtimes;
		}
		
		
}
