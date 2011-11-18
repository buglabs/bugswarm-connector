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

###### Create build dir layout
rm -Rf $DIST_DIR
mkdir $DIST_DIR
mkdir $DEPS_DIR

###### Get non-compiled external dependencies
if [ ! -f $DEPS_DIR/osgi.core.jar ]; then
	wget -P $DEPS_DIR http://www.osgi.org/download/r4v42/osgi.core.jar
fi

if [ ! -f $DEPS_DIR/osgi.cmpn.jar ]; then
	wget -P $DEPS_DIR -nc http://www.osgi.org/download/r4v42/osgi.cmpn.jar
fi

if [ ! -f $DEPS_DIR/junit-dep-4.9b2.jar ]; then
	wget --no-check-certificate -P $DEPS_DIR -nc https://github.com/downloads/KentBeck/junit/junit-dep-4.9b2.jar
fi 

if [ ! -f $DEPS_DIR/hamcrest-core-1.3.0RC2.jar ]; then
	wget --no-check-certificate -P $DEPS_DIR -nc http://hamcrest.googlecode.com/files/hamcrest-core-1.3.0RC2.jar
fi

if [ ! -f $DEPS_DIR/javax.servlet_2.3.0.v200806031603.jar ]; then
	wget --no-check-certificate -nc -O $DEPS_DIR/javax.servlet_2.3.0.v200806031603.jar "http://www.eclipse.org/downloads/download.php?r=1&file=/tools/orbit/downloads/drops/R20100519200754/bundles/javax.servlet_2.3.0.v200806031603.jar"
fi

if [ ! -f $DEPS_DIR/commons-io-2.1.jar ]; then
	wget --no-check-certificate -O /tmp/commons-io-2.1-bin.zip -nc "http://www.meisei-u.ac.jp/mirror/apache/dist/commons/io/binaries/commons-io-2.1-bin.zip"
	unzip -d /tmp /tmp/commons-io-2.1-bin.zip
	cp /tmp/commons-io-2.1/commons-io-2.1.jar $DEPS_DIR
	rm -Rf /tmp/commons-io-2.1*
fi

if [ ! -f $DEPS_DIR/jackson-core-asl-1.9.1.jar ]; then
	wget --no-check-certificate -nc -O $DEPS_DIR/jackson-core-asl-1.9.1.jar "http://repository.codehaus.org/org/codehaus/jackson/jackson-core-asl/1.9.1/jackson-core-asl-1.9.1.jar"
fi

if [ ! -f $DEPS_DIR/jackson-mapper-asl-1.9.1.jar ]; then
	wget --no-check-certificate -nc -O $DEPS_DIR/jackson-mapper-asl-1.9.1.jar "http://repository.codehaus.org/org/codehaus/jackson/jackson-mapper-asl/1.9.1/jackson-mapper-asl-1.9.1.jar"
fi

###### Get source dependencies that will be compiled
if [ -d bug-osgi ]; then
	cd bug-osgi
	git reset --hard
	git pull
	cd ..
else
	git clone git://github.com/buglabs/bug-osgi.git
	cd bug-osgi
	git checkout $BUILD_BRANCH
	cd ..
fi

if [ -d touge ]; then
	cd touge
	git reset --hard
	git pull
	cd ..
else
	git clone git://github.com/kgilmer/touge.git
fi	

if [ -d smack-smackx-osgi ]; then
	cd smack-smackx-osgi
	git reset --hard
	git checkout $BUILD_BRANCH
	git pull
	cd ..
else
	git clone git://github.com/buglabs/smack-smackx-osgi.git
	cd smack-smackx-osgi
	git checkout $BUILD_BRANCH
	cd ..
fi
# Do not compile and run smack and smackx tests
rm -Rf smack-smackx-osgi/test

###### Build dependencies
set -e

# com.buglabs.common
ant -Dbase.build.dir=$WORKSPACE/bug-osgi/com.buglabs.osgi.build -Dcheckout.dir=$WORKSPACE -DexternalDirectory=$DEPS_DIR -DdistDirectory=$DIST_DIR -f $WORKSPACE/bug-osgi/com.buglabs.common/build.xml clean create_dirs build.jars

# com.buglabs.common
ant -Dbase.build.dir=$WORKSPACE/bug-osgi/com.buglabs.osgi.build -Dcheckout.dir=$WORKSPACE -DexternalDirectory=$DEPS_DIR -DdistDirectory=$DIST_DIR -f $WORKSPACE/bug-osgi/com.buglabs.bug.dragonfly/build.xml clean create_dirs build.jars

# com.buglabs.osgi.sewing
ant -Dbase.build.dir=$WORKSPACE/bug-osgi/com.buglabs.osgi.build -Dcheckout.dir=$WORKSPACE -DexternalDirectory=$DEPS_DIR -DdistDirectory=$DIST_DIR -f $WORKSPACE/bug-osgi/com.buglabs.osgi.sewing/build.xml clean create_dirs build.jars

# org.touge.test_buddy
ant -Dproduct.dir=$WORKSPACE/ -Dbase.build.dir=$WORKSPACE/bug-osgi/com.buglabs.osgi.build -Dcheckout.dir=$WORKSPACE -DexternalDirectory=$DEPS_DIR -Ddeps=$DEPS_DIR -DdistDirectory=$DIST_DIR -Ddist=$DIST_DIR  -f $WORKSPACE/touge/org.touge.test_buddy/build.xml clean jar

# smack-smackx-osgi
ant -Dbase.build.dir=$WORKSPACE/bug-osgi/com.buglabs.osgi.build -Dcheckout.dir=$WORKSPACE -DexternalDirectory=$DEPS_DIR -DdistDirectory=$DIST_DIR -f $WORKSPACE/smack-smackx-osgi/build.xml clean create_dirs build.jars

# com.buglabs.util.shell
ant -Dbase.build.dir=$WORKSPACE/bug-osgi/com.buglabs.osgi.build -Dcheckout.dir=$WORKSPACE -DexternalDirectory=$DEPS_DIR -DdistDirectory=$DIST_DIR -f $WORKSPACE/bug-osgi/com.buglabs.util.shell/build.xml clean create_dirs build.jars

# touge restclient
ant -Dproduct.dir=$WORKSPACE/ -Dbase.build.dir=$WORKSPACE/bug-osgi/com.buglabs.osgi.build -Dcheckout.dir=$WORKSPACE -DexternalDirectory=$DEPS_DIR -Ddeps=$DEPS_DIR -DdistDirectory=$DIST_DIR -Ddist=$DIST_DIR  -f $WORKSPACE/touge/org.touge.restclient/build.xml clean jar

# bugswarm-devicestats
ant -Dbase.build.dir=$WORKSPACE/bug-osgi/com.buglabs.osgi.build -Dcheckout.dir=$WORKSPACE -DexternalDirectory=$DEPS_DIR -DdistDirectory=$DIST_DIR -f $WORKSPACE/com.buglabs.bug.swarm.devicestats/build.xml clean create_dirs build build.jars

###### Build com.buglabs.bug.swarm.connector
if [ ! -z $TEST_HOST ]; then
	echo "Building and testing com.buglabs.bug.swarm.connector"
	# swarm.client
	ant -Dreport.src=$WORKSPACE/com.buglabs.bug.swarm.client/test -Dreport.dir=$REPORT_DIR -Dgenerate.docs=true -Dbugswarm_test_host=$TEST_HOST -Dbase.build.dir=$WORKSPACE/com.buglabs.osgi.build -Dcheckout.dir=$WORKSPACE -DexternalDirectory=$DEPS_DIR -DdistDirectory=$DIST_DIR -f $WORKSPACE/com.buglabs.bug.swarm.client/build.xml clean create_dirs build document build.jars test
	# connector	
	ant -Dreport.src=$WORKSPACE/com.buglabs.bug.swarm.connector/test -Dreport.dir=$REPORT_DIR -Dgenerate.docs=true -Dbugswarm_test_host=$TEST_HOST -Dbase.build.dir=$WORKSPACE/com.buglabs.osgi.build -Dcheckout.dir=$WORKSPACE -DexternalDirectory=$DEPS_DIR -DdistDirectory=$DIST_DIR -f $WORKSPACE/com.buglabs.bug.swarm.connector/build.xml clean create_dirs build document build.jars test
else
	echo "Building com.buglabs.bug.swarm.connector.  To also run tests, TEST_HOST variable must be defined."
	# swarm.client
	ant -Dgenerate.docs=true -Dbase.build.dir=$WORKSPACE/com.buglabs.osgi.build -Dcheckout.dir=$WORKSPACE -DexternalDirectory=$DEPS_DIR -DdistDirectory=$DIST_DIR -f $WORKSPACE/com.buglabs.bug.swarm.client/build.xml clean create_dirs build build.jars
	# connector
	ant -Dgenerate.docs=true -Dbase.build.dir=$WORKSPACE/com.buglabs.osgi.build -Dcheckout.dir=$WORKSPACE -DexternalDirectory=$DEPS_DIR -DdistDirectory=$DIST_DIR -f $WORKSPACE/com.buglabs.bug.swarm.connector/build.xml clean create_dirs build document build.jars
fi

echo "Analyzing source"
ant -Dbase.build.dir=$WORKSPACE/bug-osgi/com.buglabs.osgi.build -Dcheckout.dir=$WORKSPACE -DexternalDirectory=$DEPS_DIR -DdistDirectory=$DIST_DIR -f $WORKSPACE/com.buglabs.bug.swarm.client/build.xml checkstyle cpd
ant -Dbase.build.dir=$WORKSPACE/bug-osgi/com.buglabs.osgi.build -Dcheckout.dir=$WORKSPACE -DexternalDirectory=$DEPS_DIR -DdistDirectory=$DIST_DIR -f $WORKSPACE/com.buglabs.bug.swarm.connector/build.xml checkstyle cpd

if [ -z $BLACKBOX_TEST ]; then
	echo "BLACKBOX_TEST is not set, Black box tests will not be run."
else
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
fi
