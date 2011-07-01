#!/bin/bash
# Copyright (c) 2010, Bug Labs, Inc.
# All rights reserved.
#
# Filename: install-test-connector.sh
#
# Description: Swarm Connector setup and test script.
#
#              Note that this is for internal testing, uses internal ip
#              addresses, hot mess, etc.
#
#			   Based on "swarmconsetup" script
#
# Version:  1.0
# Created: June 30, 2011
# Author:  Mike Grundy <michael.grundy@buglabs.net>
# License: All Rights Reserved, it's SWARM, Bitches!

help () {

	echo
	echo This is the Swarm Connector setup script.
	echo
	echo Either give it your username and password from buglabs.net:
	echo
	echo $0 "-u [your bugnet userid] -p [your bugnet passwd]"
	echo
	echo or just run it and it will prompt you for that stuff
}

# Check for passed params
while [ $# -gt 0 ]; do
	# -u user name
	if [ "$1" == "-u" ]; then
		shift
		BUGNETUSER=$1
		shift
		# -p password
	elif [ "$1" == "-p" ]; then
		shift
		BUGNETPSWD=$1
		shift
	else
		help
		exit -1
	fi
done

# If they didn't pass us params, prompt for them
while [ -z $BUGNETUSER ]; do
	echo Enter your buglabs.net userid
	read BUGNETUSER
done

while [ -z $BUGNETPSWD ]; do
	echo Enter the password for $BUGNETUSER
	read BUGNETPSWD
done

set -x

# Set up the hosts file entries (this is for the test servers)
echo 192.168.20.121 bugswarm-test xmpp.bugswarm-test api.bugswarm-test db.bugswarm-test >> /etc/hosts
echo "192.168.20.16 darner" >> /etc/hosts
echo 'com.buglabs.bugswarm.hostname=bugswarm-test' >> /usr/share/java/conf/config.properties

# oh dear, like this won't break regularly
 wget -q -p /usr/share/java/bundle http://darner:8085/job/bugswarm-connector-0.1/lastSuccessfulBuild/artifact/dist/smack-smackx-osgi.jar
 wget -q -p /usr/share/java/bundle http://darner:8085/job/bugswarm-connector-0.1/lastSuccessfulBuild/artifact/dist/bugswarm-connector.jar
 wget -q -p /usr/share/java/bundle http://darner:8085/job/bugswarm-connector-0.1/lastSuccessfulBuild/artifact/dist/com.buglabs.common.jar
 wget -q -p /usr/share/java/bundle http://www.meisei-u.ac.jp/mirror/apache/dist//felix/org.apache.felix.log-1.0.1.jar
 
 wget -q -p /usr/share/java/bundle http://darner:8085/job/bugswarm-connector-master/lastSuccessfulBuild/artifact/deps/junit-dep-4.9b2.jar
 wget -q -p /usr/share/java/bundle http://darner:8085/job/bugswarm-connector-master/lastSuccessfulBuild/artifact/dist/bugswarm-connector-tests.jar
rm -Rf /var/volatile/felix-cache 
/etc/init.d/felix restart 

# Get an API key based on the user id and password we were given
export MYAPIKEY=$(curl -s -X POST --user "$BUGNETUSER:$BUGNETPSWD" http://bugswarm-test/keys | cut -d\" -f4)

if [ "$MYAPIKEY" == "Authorization Required" ]; then
	echo
	echo
	echo Failed to get the API KEY. You probably type the userid or password wrong
	echo If you did then Pastebin this info to us:
	echo ==============================================================================
	curl -vvv -X POST --user "$BUGNETUSER:$BUGNETPSWD" http://bugswarm-test/keys 
	echo
	echo ==============================================================================
	exit -1
else
	echo API Key is $MYAPIKEY
fi
# Get this devices IP address
IPADDR=$(ifconfig eth0 | grep "inet addr" | sed 's/.*inet addr:\(.*\)  Bcas.*/\1/')

sleep 2
# Activate Swarm Connector (yeah, I'll put error checking in for this one later)
curl -s -X POST -d "action=activate&user-name=$BUGNETUSER&api-key=$MYAPIKEY" http://$IPADDR/bugswarm > /dev/null

# Install the test runner, then bounce the framework.  The connector should be activated, and the test runner will execute any TestSuites it finds in the registry.
 wget -q -p /usr/share/java/bundle http://darner:8085/job/bugswarm-connector-master/lastSuccessfulBuild/artifact/dist/com.buglabs.osgi.tester.jar
rm -Rf /var/volatile/felix-cache 
/etc/init.d/felix restart 

sleep 20

cat /var/log/felix.log

