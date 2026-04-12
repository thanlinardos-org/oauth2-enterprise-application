#!/bin/bash

source ../prod.env &&
cd "cert" &&

env_not_set=()
for var in INTERMEDIATE_CA_PASS VAULT_DEPLOYMENT_PATH BANK_APP_UI_CLASSPATH_RELATIVE_TO_CERT_DIR CLOUD_CONFIG_SERVER_CLASSPATH_RELATIVE_TO_CERT_DIR CSTLO; do
    if [ -z "${!var}" ]; then
        env_not_set+=("$var")
    fi
done
if [ ${#env_not_set[@]} -ne 0 ]; then
    echo "The following environment variables are not set: ${env_not_set[*]}"
    exit 1
fi

client_list=()
while IFS= read -r line || [ -n "$line" ]; do
    client_list+=("$line")
done < ../clients.csv &&
for str in "${client_list[@]}"; do
    IFS=',' read -r -a array <<< "$str"
    NAME="${array[0]}"
    N_CLIENT_PASS="${array[1]}"
    KEYSTORE_N_CLIENT_PASS="${array[2]}"
    CN="${array[3]}"
    san_list=("${array[@]:4}")
    echo "Generating certificate for $NAME with CN $CN and SANS: ${san_list[*]}"
    if [ -d "$NAME" ]; then
        read -r -p "Client directory $NAME already exists. Do you want to overwrite it? \(y/n): " choice
        case "$choice" in
            y|Y|"" ) echo "Overwriting $NAME...";;
            n|N ) echo "Skipping $NAME..."; continue;;
            * ) echo "Invalid choice. Skipping $NAME..."; continue;;
        esac
    fi

    rm -rf "$NAME"
    mkdir "$NAME" &&
    rm -rf "san.ext"
    echo "authorityKeyIdentifier=keyid,issuer" > san.ext &&
    echo "basicConstraints=CA:FALSE" >> san.ext &&
    echo "subjectAltName = @alt_names" >> san.ext &&
    echo "[alt_names]" >> san.ext &&
    echo "DNS.1 = $CN" >> san.ext &&
    for i in "${!san_list[@]}"; do
        index=$((i + 2))
        echo "DNS.$index = ${san_list[$i]}" >> san.ext
    done &&
    chmod +777 san.ext &&

    openssl req -new -newkey rsa:4096 -nodes -keyout "${NAME}"/"${NAME}".key -out "${NAME}"/"${NAME}".csr -subj "$CSTLO AE/OU=AE/CN=$CN" -passin pass:"${N_CLIENT_PASS}" -passout pass:"${N_CLIENT_PASS}" &&
    openssl x509 -req -CA intermediate.crt -CAkey intermediate.key -in "${NAME}"/"${NAME}".csr -out "${NAME}"/"${NAME}".crt -extfile san.ext -days "$DAYS_VALID" -CAcreateserial -passin pass:"${INTERMEDIATE_CA_PASS}" &&
    openssl pkcs12 -export -out "${NAME}"/"${NAME}".p12 -name "$NAME" -inkey "${NAME}"/"${NAME}".key -in "${NAME}"/"${NAME}".crt -passin pass:"${N_CLIENT_PASS}" -passout pass:"${KEYSTORE_N_CLIENT_PASS}"
    openssl pkcs12 -in "${NAME}"/"${NAME}".p12 -out "${NAME}"/"${NAME}".pem -passin pass:"${KEYSTORE_N_CLIENT_PASS}" -passout pass:"${N_CLIENT_PASS}"

    chmod +777 "${NAME}"/"${NAME}".key
    chmod +777 "${NAME}"/"${NAME}".crt

    if [ "$CN" == "keycloak.local" ]; then
        rm ../../keycloak/"${NAME}".p12 || echo "Tried to delete $NAME certs from Keycloak directory but not found." &&
        cp "${NAME}"/"${NAME}".p12 ../../keycloak/ &&

        rm ../../keycloak/truststore.p12 || echo "Tried to delete truststore.p12 from Keycloak directory but not found." &&
        cp truststore.p12 ../../keycloak/ &&
        echo "Copied $NAME certificates to Keycloak directory."
    fi

    if [ "$CN" == "vault.local" ]; then
        rm "${CLOUD_CONFIG_SERVER_CLASSPATH_RELATIVE_TO_CERT_DIR:?}/$NAME".* || echo "Tried to delete $NAME certs from Cloud Config Server directory but not found." &&
        cp "${NAME}"/"${NAME}".p12 "${CLOUD_CONFIG_SERVER_CLASSPATH_RELATIVE_TO_CERT_DIR}" &&

        rm "${CLOUD_CONFIG_SERVER_CLASSPATH_RELATIVE_TO_CERT_DIR:?}/truststore.p12" || echo "Tried to delete truststore.p12 from Cloud Config Server directory but not found." &&
        cp truststore.p12 "${CLOUD_CONFIG_SERVER_CLASSPATH_RELATIVE_TO_CERT_DIR}" &&
        echo "Copied $NAME certificates to Cloud Config Server directory."

        if [ -n "${VAULT_DEPLOYMENT_PATH}" ]; then
          rm "${VAULT_DEPLOYMENT_PATH:?}/$NAME".* || echo "Tried to delete $NAME certs from Vault deployment directory but not found." &&
          cp "${NAME}"/"${NAME}".crt "${VAULT_DEPLOYMENT_PATH}" &&
          cp "${NAME}"/"${NAME}".key "${VAULT_DEPLOYMENT_PATH}" &&

          rm "${VAULT_DEPLOYMENT_PATH}/intermediate.crt" || echo "Tried to delete intermediate.crt from Vault deployment directory but not found." &&
          cp intermediate.crt "${VAULT_DEPLOYMENT_PATH}" &&
          echo "Copied $NAME certificates to Vault deployment directory."
        else
          echo -e "\033[33mVAULT_DEPLOYMENT_PATH is not set. Skipping copy to Vault deployment directory of the following certificate files: $NAME.crt, $NAME.key & intermediate.crt\033[0m"
        fi
    fi

    if [ "$CN" == "angularui.local" ]; then
      rm "${BANK_APP_UI_CLASSPATH_RELATIVE_TO_CERT_DIR:?}/$NAME".* || echo "Tried to delete $NAME certs from bank-ap-ui directory but not found." &&
      cp "${NAME}"/"${NAME}".crt "${BANK_APP_UI_CLASSPATH_RELATIVE_TO_CERT_DIR}" &&
      cp "${NAME}"/"${NAME}".key "${BANK_APP_UI_CLASSPATH_RELATIVE_TO_CERT_DIR}" &&
      cp intermediate.crt "${BANK_APP_UI_CLASSPATH_RELATIVE_TO_CERT_DIR}" &&
      echo "Copied $NAME certificates and intermediate.crt to bank-ap-ui directory."
    fi
done &&
cd - || exit