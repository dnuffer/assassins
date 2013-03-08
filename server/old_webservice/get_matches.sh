
#!/bin/bash

set -e
set -x
set -u

appname="vegasdev"
match="testmatch"
oid="one"
install_id_one="1"
lat="40.333921" 
lon="-111.679971"

curl -d  @${match}1.json http://${appname}.cloudfoundry.com/protected/match/${oid}
curl -d  @${match}2.json http://${appname}.cloudfoundry.com/protected/match/${oid}
curl -d  @${match}3.json http://${appname}.cloudfoundry.com/protected/match/${oid}
curl -d  @${match}5.json http://${appname}.cloudfoundry.com/protected/match/${oid}
curl -d  @${match}6.json http://${appname}.cloudfoundry.com/protected/match/${oid}

curl http://${appname}.cloudfoundry.com/protected/public/matches/${oid}/${lat}/${lon}