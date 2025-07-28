#!/bin/sh
# Initialised keycloak for pms requirements.
## Usage: ./keycloak-init.sh [kubeconfig]

if [ $# -ge 1 ] ; then
  export KUBECONFIG=$1
fi

NS=pms
CHART_VERSION=0.0.1-develop
COPY_UTIL=../copy_cm_func.sh

helm repo add mosip https://mosip.github.io/mosip-helm
helm repo update

echo "checking if mosip-pms-client is created already"
IAMHOST_URL=$(kubectl get cm global -o jsonpath={.data.mosip-iam-external-host})
PMS_CLIENT_SECRET_KEY='mosip_pms_client_secret'
PMS_CLIENT_SECRET_VALUE=$(kubectl -n keycloak get secrets keycloak-client-secrets -o jsonpath={.data.$PMS_CLIENT_SECRET_KEY} | base64 -d)

echo "Copying keycloak configmaps and secret"
$COPY_UTIL configmap keycloak-env-vars keycloak $NS
$COPY_UTIL secret keycloak keycloak $NS

echo "creating and adding roles to keycloak pms clients for PMS UI-V2"
kubectl -n $NS delete secret --ignore-not-found=true keycloak-client-secrets
helm -n $NS delete pms-keycloak-init
helm -n $NS install pms-keycloak-init mosip/keycloak-init \
  -f keycloak-init-values.yaml \
  --set clientSecrets[0].name="$PMS_CLIENT_SECRET_KEY" \
  --set clientSecrets[0].secret="$PMS_CLIENT_SECRET_VALUE" \
  --set keycloak.realms.mosip.realm_config.attributes.frontendUrl="https://$IAMHOST_URL/auth" \
  --version $CHART_VERSION -f keycloak-init-values.yaml  --wait-for-jobs

PMS_CLIENT_SECRET_VALUE=$(kubectl -n $NS get secrets keycloak-client-secrets -o jsonpath={.data.$PMS_CLIENT_SECRET_KEY})

# Check if the secret exists
if kubectl get secret keycloak-client-secrets -n keycloak >/dev/null 2>&1; then
  echo "Secret 'keycloak-client-secrets' exists. Performing secret update..."
  kubectl -n keycloak get secret keycloak-client-secrets -o json |
  jq ".data[\"$PMS_CLIENT_SECRET_KEY\"]=\"$PMS_CLIENT_SECRET_VALUE\"" |
  kubectl apply -f -
else
  echo "Secret 'keycloak-client-secrets' does not exist. Copying the secret to the keycloak namespace."
  $COPY_UTIL secret keycloak-client-secrets $NS keycloak
fi
