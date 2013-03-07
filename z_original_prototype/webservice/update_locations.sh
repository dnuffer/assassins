#!/bin/bash

appname="vegasdev"

oid_one="one"
oid_two="two"
oid_three="three"
oid_four="four"
oid_five="five"
oid_six="six"


install_id_one="1"
install_id_two="2"
install_id_three="3"
install_id_four="4"
install_id_five="5"
install_id_six="6"

match="test_match"

#each player updates location
echo "UPDATING LOCATION"
curl -d  @location${install_id_one}.json http://${appname}.cloudfoundry.com/update/location/${oid_one}
echo ""
curl -d  @location${install_id_two}.json http://${appname}.cloudfoundry.com/update/location/${oid_two}
echo ""
curl -d  @location${install_id_thee}.json http://${appname}.cloudfoundry.com/update/location/${oid_three}
echo ""
curl -d  @location${install_id_four}.json http://${appname}.cloudfoundry.com/update/location/${oid_four}
echo ""
curl -d  @location${install_id_five}.json http://${appname}.cloudfoundry.com/update/location/${oid_five}
echo ""
curl -d  @location${install_id_six}.json http://${appname}.cloudfoundry.com/update/location/${oid_six}
echo ""
echo ""