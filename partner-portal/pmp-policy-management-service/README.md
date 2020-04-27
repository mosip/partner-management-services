## Policy Management Service </br>
This service would be used by Policy Manager for management of Policies. 
Policy manager would be able to create, update and get policy details.

#Policy Management Srvice is having following operations </br>
a. POST /policies  </br>
b. POST /policies/{policyID}/authPolicies </br>
c. POST /policies/{policyID} </br>
d. PUT /policies/{policyID} </br>
e. GET /policies </br>
f. GET /policies/{policyID} </br>
g. GET /policies/{PartnerAPIKey} </br>

a.POST /policies  </br>
  This API would be used to create new Policy for policy group.   </br>
  
b.POST /policies/{policyID}/authPolicies   </br>
  This API would be used to create new auth policies for existing policy group. </br>
 
c. POST /policies/{policyID} </br>
  This API would be used to update existing policy for a policy group </br>

d. PUT /policies/{policyID} </br>
  This API would be used to update the status (activate/deactivate) for the given policy id. </br>

e. GET /policies </br>
  Policy manager would require this service to get details for the policies in the policy group he belongs to. All the policy groups 
  are required to be back filled in the partner management database through an offline process based on country specific requirements. 
  Partner Manager and Policy Manager assigned for the Policy group are also required to be back filled along with creation of the policy 
  group. Partner management would depend on Kernel IAM module services for all user management related activities. User ID and Password 
  are shared using off-line process.

f. GET /policies/{policyID} </br>
  This API would be used to retrieve existing policy for a policy group based on the policy id. </br>

g. GET /policies/{PartnerAPIKey} </br>
  This API would be used to retrieve the partner policy details for given PartnerAPIKey.
