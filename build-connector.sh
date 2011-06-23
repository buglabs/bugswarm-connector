#!/bin/bash

if [ -z $WORKSPACE ]; then
	echo "Setting WORKSPACE TO `pwd`"
	WORKSPACE=`pwd`
fi

if [ -z $BUILD_BRANCH ]; then
	echo "Setting BUILD_BRANCH to 'master'"
	BUILD_BRANCH="master"
fi

if [ -z $TEST_HOST ]; then
	echo "TEST_HOST is not set, but must be a hostname for tests to execute.  Tests will not be run."
fi

DIST_DIR=$WORKSPACE/dist
DEPS_DIR=$WORKSPACE/deps

###### Create build dir layout
mkdir $DIST_DIR
mkdir $DEPS_DIR

###### Get non-compiled external dependencies
if [ ! -f $DEPS_DIR/osgi.core.jar ]; then
	wget -P $DEPS_DIR http://www.osgi.org/download/r4v42/osgi.core.jar
fi

if [ ! -f $DEPS_DIR/osgi.cmpn.jar ]; then
	wget -P $DEPS_DIR http://www.osgi.org/download/r4v42/osgi.cmpn.jar
fi

if [ ! -f $DEPS_DIR/junit-dep-4.9b2.jar ]; then
	wget --no-check-certificate -P $DEPS_DIR https://github.com/downloads/KentBeck/junit/junit-dep-4.9b2.jar
fi 

if [ ! -f $DEPS_DIR/hamcrest-core-1.3.0RC2.jar ]; then
	wget --no-check-certificate -P $DEPS_DIR http://hamcrest.googlecode.com/files/hamcrest-core-1.3.0RC2.jar
fi

if [ ! -f $DEPS_DIR/javax.servlet_2.3.0.v200806031603.jar ]; then
	wget --no-check-certificate -O $DEPS_DIR/javax.servlet_2.3.0.v200806031603.jar "http://www.eclipse.org/downloads/download.php?r=1&file=/tools/orbit/downloads/drops/R20100519200754/bundles/javax.servlet_2.3.0.v200806031603.jar"
fi

###### Clean old checkouts
rm -Rf com.buglabs.common
rm -Rf com.buglabs.osgi.sewing
rm -Rf com.buglabs.osgi.build
rm -Rf smack-smackx-osgi

###### Get source dependencies that will be compiled
git clone git@github.com:buglabs/bug-osgi.git 
cd bug-osgi
git checkout $BUILD_BRANCH
cd ..
mv bug-osgi/com.buglabs.common $WORKSPACE
mv bug-osgi/com.buglabs.osgi.sewing $WORKSPACE
mv bug-osgi/com.buglabs.osgi.build $WORKSPACE
rm -Rf bug-osgi

git clone git://github.com/buglabs/smack-smackx-osgi.git
cd smack-smackx-osgi
git checkout $BUILD_BRANCH
cd ..
# Do not smack and smackx compile/run tests
rm -Rf smack-smackx-osgi/test

###### Build dependencies
set -e

# com.buglabs.common
ant -Dbase.build.dir=$WORKSPACE/com.buglabs.osgi.build -Dcheckout.dir=$WORKSPACE -DexternalDirectory=$DEPS_DIR -DdistDirectory=$DIST_DIR -f com.buglabs.common/build.xml build.jars

# com.buglabs.osgi.sewing
ant -Dbase.build.dir=$WORKSPACE/com.buglabs.osgi.build -Dcheckout.dir=$WORKSPACE -DexternalDirectory=$DEPS_DIR -DdistDirectory=$DIST_DIR -f com.buglabs.osgi.sewing/build.xml build.jars

# smack-smackx-osgi
ant -Dbase.build.dir=$WORKSPACE/com.buglabs.osgi.build -Dcheckout.dir=$WORKSPACE -DexternalDirectory=$DEPS_DIR -DdistDirectory=$DIST_DIR -f smack-smackx-osgi/build.xml build.jars

###### Build bugswarm-connector
echo "Style checking bugswarm-connector"
ant -Dbase.build.dir=$WORKSPACE/com.buglabs.osgi.build -Dcheckout.dir=$WORKSPACE -DexternalDirectory=$DEPS_DIR -DdistDirectory=$DIST_DIR -f $WORKSPACE/bugswarm-connector/build.xml checkstyle
echo "Building bugswarm-connector"
ant -Dbase.build.dir=$WORKSPACE/com.buglabs.osgi.build -Dcheckout.dir=$WORKSPACE -DexternalDirectory=$DEPS_DIR -DdistDirectory=$DIST_DIR -f $WORKSPACE/bugswarm-connector/build.xml clean build.jars
if [ ! -z $TEST_HOST ]; then
	echo "Testing bugswarm-connector"
	ant -Dbugswarm_test_host=$TEST_HOST -Dbase.build.dir=$WORKSPACE/com.buglabs.osgi.build -Dcheckout.dir=$WORKSPACE -DexternalDirectory=$DEPS_DIR -DdistDirectory=$DIST_DIR -f $WORKSPACE/bugswarm-connector/build.xml clean test
else
	echo "Skipping tests of bugswarm-connector"
fi