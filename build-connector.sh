#!/bin/bash

if [ -z $WORKSPACE ]; then
	echo "Setting WORKSPACE TO `pwd`"
	WORKSPACE=`pwd`
fi

if [ -z $WORKSPACE ]; then
	DIST_DIR=$WORKSPACE/dist
else
	DIST_DIR=$WORKSPACE/dist
fi

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

if [ ! -f $DEPS_DIR/smack-smackx-osgi.jar ]; then
	wget --no-check-certificate -P $DEPS_DIR https://github.com/downloads/buglabs/smack-smackx-osgi/smack-smackx-osgi.jar
fi

if [ ! -f $DEPS_DIR/junit-dep-4.9b2.jar ]; then
	wget --no-check-certificate -P $DEPS_DIR https://github.com/downloads/KentBeck/junit/junit-dep-4.9b2.jar
fi 

if [ ! -f $DEPS_DIR/json_simple-1.1.jar ]; then
	wget --no-check-certificate -P $DEPS_DIR http://json-simple.googlecode.com/files/json_simple-1.1.jar
fi

if [ ! -f $DEPS_DIR/hamcrest-core-1.3.0RC2.jar ]; then
	wget --no-check-certificate -P $DEPS_DIR http://hamcrest.googlecode.com/files/hamcrest-core-1.3.0RC2.jar
fi

if [ ! -f $DEPS_DIR/xpp3-1.1.4c.jar ]; then
	wget --no-check-certificate -P $DEPS_DIR http://www.extreme.indiana.edu/dist/java-repository/xpp3/jars/xpp3-1.1.4c.jar
fi

if [ ! -f $DEPS_DIR/javax.servlet_2.3.0.v200806031603.jar ]; then
	wget --no-check-certificate -O $DEPS_DIR/javax.servlet_2.3.0.v200806031603.jar "http://www.eclipse.org/downloads/download.php?r=1&file=/tools/orbit/downloads/drops/R20100519200754/bundles/javax.servlet_2.3.0.v200806031603.jar"
fi

###### Clean old checkouts
rm -Rf com.buglabs.common
rm -Rf com.buglabs.osgi.sewing
rm -Rf com.buglabs.osgi.build

###### Get source dependencies that will be compiled
git clone git@github.com:buglabs/bug-osgi.git
mv bug-osgi/com.buglabs.common $WORKSPACE
mv bug-osgi/com.buglabs.osgi.sewing $WORKSPACE
mv bug-osgi/com.buglabs.osgi.build $WORKSPACE
rm -Rf bug-osgi

###### Build dependencies

# com.buglabs.common
ant -Dbase.build.dir=$WORKSPACE/com.buglabs.osgi.build -Dcheckout.dir=$WORKSPACE -DexternalDirectory=$DEPS_DIR -DdistDirectory=$DIST_DIR -f com.buglabs.common/build.xml build.jars

# com.buglabs.osgi.sewing
ant -Dbase.build.dir=$WORKSPACE/com.buglabs.osgi.build -Dcheckout.dir=$WORKSPACE -DexternalDirectory=$DEPS_DIR -DdistDirectory=$DIST_DIR -f com.buglabs.osgi.sewing/build.xml build.jars

###### Build bugswarm-connector
echo "Building bugswarm-connector"
ant -Dbase.build.dir=$WORKSPACE/com.buglabs.osgi.build -Dcheckout.dir=$WORKSPACE -DexternalDirectory=$DEPS_DIR -DdistDirectory=$DIST_DIR -f $WORKSPACE/bugswarm-connector/build.xml build.jars
echo "Testing bugswarm-connector"
ant -Dbase.build.dir=$WORKSPACE/com.buglabs.osgi.build -Dcheckout.dir=$WORKSPACE -DexternalDirectory=$DEPS_DIR -DdistDirectory=$DIST_DIR -f $WORKSPACE/bugswarm-connector/build.xml test
echo "Style checking bugswarm-connector"
set +e
ant -Dbase.build.dir=$WORKSPACE/com.buglabs.osgi.build -Dcheckout.dir=$WORKSPACE -DexternalDirectory=$DEPS_DIR -DdistDirectory=$DIST_DIR -f $WORKSPACE/bugswarm-connector/build.xml checkstyle >& /dev/null