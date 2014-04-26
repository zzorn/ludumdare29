#!/bin/bash

mvn clean package

rm -rf release-target
rm ludumdare29_zzorn.zip

mkdir release-target
mkdir release-target/ludumdare29_zzorn

cp -r src/main/release_dir/* release-target/ludumdare29_zzorn/
cp -r src/main/assets/ release-target/ludumdare29_zzorn/assets/
cp readme.txt release-target/ludumdare29_zzorn/
cp target/ludumdare29-1.0-SNAPSHOT-jar-with-dependencies.jar release-target/ludumdare29_zzorn/ludumdare29_zzorn.jar

cd release-target
zip -r ../ludumdare29_zzorn.zip ludumdare29_zzorn
cd ..

