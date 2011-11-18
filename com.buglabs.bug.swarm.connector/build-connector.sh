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
	./test-connector.sh
fi
