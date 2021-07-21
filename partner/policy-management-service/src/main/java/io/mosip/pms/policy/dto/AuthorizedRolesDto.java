package io.mosip.pms.policy.dto;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;


@Component("authorizedRoles")
@ConfigurationProperties(prefix = "mosip.role.pms")
@Getter
@Setter
public class AuthorizedRolesDto {

	//Policy management

	private List<String> postpoliciesgroupnew;

	private List<String> putpoliciesgrouppolicygroupid;

	private List<String> postpolicies;

	private List<String> postpoliciespolicyidgrouppublish;

	private List<String> putpoliciespolicyid;

	private List<String> patchpoliciespolicyidgrouppolicygroupid;

	private List<String> getpolicies;

	private List<String> getpoliciespolicyid;

	private List<String> getpoliciesapikey;

	private List<String> getpoliciespolicyidpartnerpartnerid;

	private List<String> getpoliciesgrouppolicygroupid;

	private List<String> getpoliciesgroupall;

	private List<String> postpoliciesgroupsearch;

	private List<String> postpoliciessearch;

	private List<String> getpoliciesconfigkey;

	private List<String> postpoliciesgroupfiltervalues;

	private List<String> postpoliciesfiltervalues;
	
}