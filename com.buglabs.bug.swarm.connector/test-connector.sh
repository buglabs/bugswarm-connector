#!/bin/bash

if [ -z $WORKSPACE ]; then
	echo "Setting WORKSPACE TO `pwd`"
	WORKSPACE=`pwd`
fi

if [ -z $BUILD_BRANCH ]; then
	echo "Setting BUILD_BRANCH to 'master'"
	BUILD_BRANCH="master"
fi

if [ -z $REPORT_DIR ]; then
	echo "Setting REPORT_DIR to 'junit-reports'"
	REPORT_DIR=$WORKSPACE/com.buglabs.bug.swarm.connector/junit-reports
fi

if [ -z $TEST_HOST ]; then
	echo "TEST_HOST is not set, but must be a hostname for tests to execute.  Tests will not be run."
fi

DIST_DIR=$WORKSPACE/dist
DEPS_DIR=$WORKSPACE/deps

echo "Building OSGi test bundle"
ant -Dbase.build.dir=$WORKSPACE/bug-osgi/com.buglabs.osgi.build -Dcheckout.dir=$WORKSPACE -DexternalDirectory=$DEPS_DIR -DdistDirectory=$DIST_DIR -f $WORKSPACE/com.buglabs.bug.swarm.connector/build.xml osgi-test-jar

BLACKBOX_ROOT=$WORKSPACE/blackbox_tests
mkdir -p $BLACKBOX_ROOT
mkdir -p $BLACKBOX_ROOT/bundle
mkdir -p $BLACKBOX_ROOT/properties

wget -P $BLACKBOX_ROOT -nc https://leafcutter.ci.cloudbees.com/job/knapsack/lastSuccessfulBuild/artifact/knapsack.jar

wget -P $BLACKBOX_ROOT/bundle -nc https://github.com/downloads/buglabs/bug-osgi/junit-osgi-4.9b2.jar
wget -P $BLACKBOX_ROOT/bundle -nc http://ftp.riken.jp/net/apache//felix/org.apache.felix.http.jetty-2.2.0.jar
cp $DIST_DIR/bugswarm-connector-tests.jar $BLACKBOX_ROOT/bundle
cp $DIST_DIR/bugswarm-connector.jar $BLACKBOX_ROOT/bundle
cp $DIST_DIR/com.buglabs.bug.dragonfly.jar $BLACKBOX_ROOT/bundle
cp $DIST_DIR/com.buglabs.bug.swarm.client.jar $BLACKBOX_ROOT/bundle
cp $DIST_DIR/com.buglabs.common.jar $BLACKBOX_ROOT/bundle
cp $DIST_DIR/com.buglabs.osgi.sewing.jar $BLACKBOX_ROOT/bundle
cp $DIST_DIR/org.touge.testbuddy.jar $BLACKBOX_ROOT/bundle
cp $DIST_DIR/org.touge.restclient.jar $BLACKBOX_ROOT/bundle
cp $DIST_DIR/smack-smackx-osgi.jar $BLACKBOX_ROOT/bundle

chmod u+x $BLACKBOX_ROOT/bundle/*

cp $DEPS_DIR/commons-io-2.1.jar $BLACKBOX_ROOT/bundle      
cp $DEPS_DIR/jackson-core-asl-1.9.1.jar $BLACKBOX_ROOT/bundle    
cp $DEPS_DIR/osgi.core.jar $BLACKBOX_ROOT/bundle
cp $DEPS_DIR/hamcrest-core-1.3.0RC2.jar $BLACKBOX_ROOT/bundle 
cp $DEPS_DIR/jackson-mapper-asl-1.9.1.jar $BLACKBOX_ROOT/bundle        
cp $DEPS_DIR/osgi.cmpn.jar $BLACKBOX_ROOT/bundle

echo "report.src=$WORKSPACE/com.buglabs.bug.swarm.client/test" > $BLACKBOX_ROOT/properties/test.properties
echo "testrunner.report.dir=$REPORT_DIR" >> $BLACKBOX_ROOT/properties/test.properties
echo "bugswarm_test_host=$TEST_HOST" >> $BLACKBOX_ROOT/properties/test.properties
echo "testrunner.report.dir=$REPORT_DIR" >> $BLACKBOX_ROOT/properties/test.properties
echo "report.misc=test.bugswarm.net,connector_test,3077514aa9aa5a5826cfd9d04ee059db1a18057d,7339d4a60c729308086341600d44c6424a4079cb,connector_test2,ddef1fa815d8549fa184e2716405f2cc553b5316,af9c58ce70d031934826bd9662f00420863e752b" >> $BLACKBOX_ROOT/properties/test.properties

cd $BLACKBOX_ROOT

if [ -z $REMOTE_DEBUG ]; then
	java -jar knapsack.jar		
else
	java -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5000 -jar knapsack.jar
fi