#!/bin/bash

function usage()
{
  echo "Usage: cert_generator.sh {dir where certs will be stored} {domain}"
  echo "Example: ./cert_generator.sh /opt/mykeystore iad1.infery.com"
  exit 1
}

if [[ $# -ne 2 ]];then
  usage
fi

dest=$1
domain=$2

if [ ! -d $dest ]; then
    echo "Destination directory doesn't exist"
    exit 1
fi

cd $dest

pv_password=changeit
wildcard_name=*.${domain}
pv_startdate=$(date "+%Y/%m/%d")
pv_validity=36500

keytool -genkeypair -alias harmony_ca -keystore harmony_ca_ks.jks -storepass $pv_password -keypass $pv_password -dname "CN=${domain}" -startdate $pv_startdate -validity $pv_validity -ext BasicConstraints:critical=ca:true

keytool -exportcert -alias harmony_ca -keystore harmony_ca_ks.jks -storepass $pv_password -rfc > harmony_ca_cert.pem

cat harmony_ca_cert.pem |
keytool -importcert -alias harmony_ca -keystore harmony_trust_ks.jks -storepass $pv_password -noprompt

keytool -genkeypair -alias harmony_key -keystore harmony_server_ks.jks -storepass $pv_password -keypass $pv_password -dname "CN=${wildcard_name}" -startdate $pv_startdate -validity $pv_validity

keytool -certreq -alias harmony_key -keystore harmony_server_ks.jks -storepass $pv_password |
keytool -gencert -alias harmony_ca -keystore harmony_ca_ks.jks -storepass $pv_password -rfc -startdate $pv_startdate -validity $pv_validity > harmony_server_cert.pem

cat harmony_ca_cert.pem |
keytool -importcert -alias harmony_ca -keystore harmony_server_ks.jks -storepass $pv_password -noprompt

cat harmony_server_cert.pem |
keytool -importcert -alias harmony_key -keystore harmony_server_ks.jks -storepass $pv_password


