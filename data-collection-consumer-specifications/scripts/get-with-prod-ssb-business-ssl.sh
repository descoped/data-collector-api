#!/bin/bash

# Show usage
usage() {
  if [ -n "$1" ]; then
    echo ""
    echo "$1"
  fi

  cat <<EOP

$0 [options]

    -u | --url      <url>     (required)
    -o | --out      <out>     (required)
    -h | --help

Example:

  $0 --url "https://folkeregisteret.api.skatteetaten.no/folkeregisteret/offentlig-med-hjemmel/api/v1/personer/xsd" --out "person-xsd.xml"

EOP
  exit 0
}

if [ "$*" == "" ]; then
  usage Usage:
fi
# Read commandline arguments
while [ "$1" != "" ]; do
  case $1 in
  -u | --url)
    shift
    url=$1
    ;;
  -o | --out)
    shift
    out=$1
    ;;
  -h | --help)
    usage
    exit
    ;;
  *)
    usage
    exit 1
    ;;
  esac
  shift
done

getCertificateSecrets() {
  P12_FILE="/tmp/ssb.p12"
  gcloud secrets versions access latest --secret=ssb-prod-p12-certificate --project=ssb-team-dapla --format='get(payload.data)' | base64 -d >$P12_FILE
  PASS="$(gcloud secrets versions access latest --secret=ssb-prod-p12-passphrase --project=ssb-team-dapla --format='get(payload.data)' | tr '_-' '/+' | base64 -d)"
}

generatePemCertificates() {
  if [ -f "$P12_FILE" ]; then
    KEY=$(openssl pkcs12 -in $P12_FILE -nocerts -nodes -password pass:"$PASS")
    CERT=$(openssl pkcs12 -in $P12_FILE -clcerts -nokeys -password pass:$PASS)
    rm -f "$P12_FILE"
  else
    echo "Something went wrong with generatePemCertificates"
    exit 1
  fi
}

doRequest() {
  curl -X GET --cert <(echo "$CERT") --key <(echo "$KEY") -s "$url" -o "$out"
  echo "Received: $out"
}

getCertificateSecrets
generatePemCertificates
doRequest
