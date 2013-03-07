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

#verify playerstate
echo "GETTING PLAYERSTATE:"
curl  http://${appname}.cloudfoundry.com/playerstate/${oid_one}
echo ""
curl  http://${appname}.cloudfoundry.com/playerstate/${oid_two}
echo ""
curl  http://${appname}.cloudfoundry.com/playerstate/${oid_three}
echo ""
curl  http://${appname}.cloudfoundry.com/playerstate/${oid_four}
echo ""
curl  http://${appname}.cloudfoundry.com/playerstate/${oid_five}
echo ""
curl  http://${appname}.cloudfoundry.com/playerstate/${oid_six}
echo ""
echo ""
