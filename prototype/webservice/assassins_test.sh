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

lat_one="40.333921"
lat_two="40.333922" 
lat_three="40.333923" 
lat_four="40.333924"
lat_five="40.333925" 
lat_six="40.333926"

lon_one="-111.679971"
lon_two="-111.679972"
lon_three="-111.679973"
lon_four="-111.679974"
lon_five="-111.679975"
lon_six="-111.679976"


install_id_one="1"
install_id_two="2"
install_id_three="3"
install_id_four="4"
install_id_five="5"
install_id_six="6"

match="testmatch"

#create two profiles

echo "CREATING PROFILES:"
curl -d  @profile${install_id_one}.json http://${appname}.cloudfoundry.com/protected/profile
echo ""
curl -d  @profile${install_id_two}.json http://${appname}.cloudfoundry.com/protected/profile
echo ""
curl -d  @profile${install_id_three}.json http://${appname}.cloudfoundry.com/protected/profile
echo ""
curl -d  @profile${install_id_four}.json http://${appname}.cloudfoundry.com/protected/profile
echo ""
curl -d  @profile${install_id_five}.json http://${appname}.cloudfoundry.com/protected/profile
echo ""
curl -d  @profile${install_id_six}.json http://${appname}.cloudfoundry.com/protected/profile
echo ""
echo ""
echo ""

#create a match
echo "CREATING MATCH:"
curl -d  @${match}.json http://${appname}.cloudfoundry.com/protected/match/${oid_one}
echo ""
echo ""

#each player joins match
echo "JOINING MATCH:"
curl http://${appname}.cloudfoundry.com/protected/join/match/${match}/${install_id_one}
echo ""
curl http://${appname}.cloudfoundry.com/protected/join/match/${match}/${install_id_two}
echo ""
curl http://${appname}.cloudfoundry.com/protected/join/match/${match}/${install_id_three}
echo ""
curl http://${appname}.cloudfoundry.com/protected/join/match/${match}/${install_id_four}
echo ""
curl http://${appname}.cloudfoundry.com/protected/join/match/${match}/${install_id_five}
echo ""
curl http://${appname}.cloudfoundry.com/protected/join/match/${match}/${install_id_six}
echo ""
echo ""


#each player updates location
echo "UPDATING LOCATION"
curl -d @${match}.json http://${appname}.cloudfoundry.com/update/location/${oid_one}/${lat_one}/${lon_one}
echo ""
curl -d @${match}.json http://${appname}.cloudfoundry.com/update/location/${oid_two}/${lat_two}/${lon_two}
echo ""
curl -d @${match}.json http://${appname}.cloudfoundry.com/update/location/${oid_three}/${lat_three}/${lon_three}
echo ""
curl -d @${match}.json http://${appname}.cloudfoundry.com/update/location/${oid_four}/${lat_four}/${lon_four}
echo ""
curl -d @${match}.json http://${appname}.cloudfoundry.com/update/location/${oid_five}/${lat_five}/${lon_five}
echo ""
curl -d @${match}.json http://${appname}.cloudfoundry.com/update/location/${oid_six}/${lat_six}/${lon_six}
echo ""
echo ""

#each player attacks
echo "ATTACKING:"
echo "Player 1 attacks: "
curl -d @${match}.json http://${appname}.cloudfoundry.com/attack/${oid_one}/${lat_one}/${lon_one}
echo ""
echo "Player 2 attacks: "
curl -d @${match}.json http://${appname}.cloudfoundry.com/attack/${oid_two}/${lat_two}/${lon_two}
echo ""
echo "Player 3 attacks: "
curl -d @${match}.json http://${appname}.cloudfoundry.com/attack/${oid_three}/${lat_three}/${lon_three}
echo ""
echo "Player 4 attacks: "
curl -d @${match}.json http://${appname}.cloudfoundry.com/attack/${oid_four}/${lat_four}/${lon_four}
echo ""
echo "Player 5 attacks: "
curl -d @${match}.json http://${appname}.cloudfoundry.com/attack/${oid_five}/${lat_five}/${lon_five}
echo ""
echo "Player 6 attacks: "
curl -d @${match}.json http://${appname}.cloudfoundry.com/attack/${oid_six}/${lat_six}/${lon_six}
echo ""

#creating more matches
curl -d  @${match}1.json http://${appname}.cloudfoundry.com/protected/match/${oid_one}
echo ""
echo ""

curl -d  @${match}2.json http://${appname}.cloudfoundry.com/protected/match/${oid_one}
echo ""
echo ""

curl -d  @${match}3.json http://${appname}.cloudfoundry.com/protected/match/${oid_one}
echo ""
echo ""

curl -d  @${match}5.json http://${appname}.cloudfoundry.com/protected/match/${oid_one}
echo ""
echo ""

curl -d  @${match}6.json http://${appname}.cloudfoundry.com/protected/match/${oid_one}
echo ""


lat="40.333921" 
lon="-111.679971"

echo ""

curl http://${appname}.cloudfoundry.com/protected/public/matches/${oid_one}/${lat}/${lon}

echo ""
echo ""




