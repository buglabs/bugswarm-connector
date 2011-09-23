#!/bin/sh

SWARM_HOSTNAME="bugswarm-test"

echo "Updating connector and dependencies to latest jars available from Jenkins on Darner."
rm /usr/share/osgi/bundle/com.buglabs.common.jar
wget -P /usr/share/osgi/bundle/ http://192.168.20.16:8085/job/bugswarm-connector-master/lastSuccessfulBuild/artifact/dist/com.buglabs.common.jar
rm /usr/share/osgi/bundle/smack-smackx-osgi.jar
wget -P /usr/share/osgi/bundle/ http://192.168.20.16:8085/job/bugswarm-connector-master/lastSuccessfulBuild/artifact/dist/smack-smackx-osgi.jar
rm /usr/share/osgi/bundle/bugswarm-connector.jar
wget -P /usr/share/osgi/bundle/ http://192.168.20.16:8085/job/bugswarm-connector-master/lastSuccessfulBuild/artifact/dist/bugswarm-connector.jar
chmod u+x /usr/share/osgi/bundle/bugswarm-connector.jar

if [ ! -f /usr/share/osgi/properties/connector.properties ]; then
	echo "Looks like a first install of connector"
	echo "Be sure that the client is able to connect to $SWARM_HOSTNAME"
	echo "Setting connector host property"
	echo "com.buglabs.bugswarm.hostname=$SWARM_HOSTNAME" > /usr/share/osgi/properties/connector.properties
fi

if [ -e /usr/share/osgi/bin/update ]; then
	echo "Signaling knapsack of update"
	/usr/share/osgi/bin/update
else
	echo "Felix is not running, start the framework manually"
fi