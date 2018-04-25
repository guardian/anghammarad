#!/bin/bash
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
SCALA_FOLDER="scala-2.12"
cd $DIR
sbt assembly
cat "$DIR/generate-cli-prefix" "$DIR/dev/target/$SCALA_FOLDER/dev-assembly-1.1.1-SNAPSHOT.jar" > "$DIR/dev/target/$SCALA_FOLDER/anghammarad" 
chmod +x "$DIR/dev/target/$SCALA_FOLDER/anghammarad"
echo "anghammarad executable now available at $DIR/dev/target/$SCALA_FOLDER/anghammarad"

