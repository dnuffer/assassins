#!/bin/bash

set -e
#set -x
#set -u

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

#each player attacks
echo "ATTACKING:"
echo "Player 1 attacks: "
curl  -d  @location${install_id_one}.json http://${appname}.cloudfoundry.com/attack/${oid_one}
echo ""
echo "Player 2 attacks: "
curl  -d  @location${install_id_two}.json http://${appname}.cloudfoundry.com/attack/${oid_two}
echo ""
echo "Player 3 attacks: "
curl  -d  @location${install_id_three}.json http://${appname}.cloudfoundry.com/attack/${oid_three}
echo ""
echo "Player 4 attacks: "
curl  -d  @location${install_id_four}.json http://${appname}.cloudfoundry.com/attack/${oid_four}
echo ""
echo "Player 5 attacks: "
curl  -d  @location${install_id_five}.json http://${appname}.cloudfoundry.com/attack/${oid_five}
echo ""
echo "Player 6 attacks: "
curl  -d  @location${install_id_six}.json http://${appname}.cloudfoundry.com/attack/${oid_six}
echo ""