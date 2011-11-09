#!/bin/sh
# This script is intented to be run on a BUG.  It will download the latest connector jars from the build server
# and update the running Felix system if active.

SWARM_HOSTNAME="test.bugswarm.net"
BUILD_SERVER="192.168.20.16:8085"
BUNDLE_DIR=/usr/share/osgi/bundle
APP_DIR=/usr/share/osgi/apps

SWARM_BRANCH=0.3

echo "Updating connector and dependencies to latest jars available from $BUILD_SERVER."

if [ ! -f $BUNDLE_DIR/com.buglabs.common.jar ]; then 
  wget -P $BUNDLE_DIR/ http://$BUILD_SERVER/job/bugswarm-connector-$SWARM_BRANCH/lastSuccessfulBuild/artifact/dist/com.buglabs.common.jar
fi

if [ ! -f $BUNDLE_DIR/smack-smackx-osgi.jar ]; then
	wget -P $BUNDLE_DIR/ http://$BUILD_SERVER/job/bugswarm-connector-$SWARM_BRANCH/lastSuccessfulBuild/artifact/dist/smack-smackx-osgi.jar
fi

wget -nc -P $BUNDLE_DIR "http://repository.codehaus.org/org/codehaus/jackson/jackson-core-asl/1.9.1/jackson-core-asl-1.9.1.jar"
wget -nc -P $BUNDLE_DIR "http://repository.codehaus.org/org/codehaus/jackson/jackson-mapper-asl/1.9.1/jackson-mapper-asl-1.9.1.jar"

if [ ! -f $BUNDLE_DIR/commons-io-2.1.jar ]; then
	wget -O /tmp/commons-io-2.1-bin.zip -nc "http://www.meisei-u.ac.jp/mirror/apache/dist/commons/io/binaries/commons-io-2.1-bin.zip"
	unzip -d /tmp /tmp/commons-io-2.1-bin.zip
	cp /tmp/commons-io-2.1/commons-io-2.1.jar $BUNDLE_DIR
	rm -Rf /tmp/commons-io-2.1*
fi

rm $BUNDLE_DIR/bugswarm-connector.jar
wget -P $BUNDLE_DIR/ http://$BUILD_SERVER/job/bugswarm-connector-$SWARM_BRANCH/lastSuccessfulBuild/artifact/dist/bugswarm-connector.jar

rm $APP_DIR/bugswarm-devicestats.jar
wget -P $APP_DIR/ http://$BUILD_SERVER/job/bugswarm-connector-$SWARM_BRANCH/lastSuccessfulBuild/artifact/dist/bugswarm-devicestats.jar

rm $BUNDLE_DIR/com.buglabs.bug.swarm.restclient.jar
wget -P $BUNDLE_DIR/ http://$BUILD_SERVER/job/bugswarm-connector-$SWARM_BRANCH/lastSuccessfulBuild/artifact/dist/com.buglabs.bug.swarm.restclient.jar

rm $BUNDLE_DIR/org.touge.restclient.jar
wget -P $BUNDLE_DIR/ http://$BUILD_SERVER/job/bugswarm-connector-$SWARM_BRANCH/lastSuccessfulBuild/artifact/dist/org.touge.restclient.jar

chmod u+x $BUNDLE_DIR/bugswarm-connector.jar
chmod u+x $APP_DIR/bugswarm-devicestats.jar

if [ ! -f /usr/share/osgi/properties/connector.properties ]; then
	echo "Looks like a first install of connector"
	echo "Be sure that the client is able to connect to $SWARM_HOSTNAME"
	echo "Setting connector host property"
	echo "com.buglabs.bugswarm.hostname=$SWARM_HOSTNAME" > /usr/share/osgi/properties/connector.properties
	echo "Please restart Felix for system property change to take effect"
fi

if [ -e /usr/share/osgi/bin/update ]; then
	echo "Signaling knapsack of update"
	/usr/share/osgi/bin/update
else
	echo "Felix is not running, start the framework manually"
fi
