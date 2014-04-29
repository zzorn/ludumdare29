#!/bin/bash

mvn clean package

rm -rf release-target
rm ludumdare29_CrushingDepth_zzorn.zip

mkdir release-target
mkdir release-target/ludumdare29_CrushingDepth_zzorn

cp -r src/main/release_dir/* release-target/ludumdare29_CrushingDepth_zzorn/
cp -r assets/ release-target/ludumdare29_CrushingDepth_zzorn/assets/
cp readme.txt release-target/ludumdare29_CrushingDepth_zzorn/
cp target/ludumdare29-1.0-SNAPSHOT-jar-with-dependencies.jar release-target/ludumdare29_CrushingDepth_zzorn/ludumdare29_CrushingDepth_zzorn.jar

cd release-target
zip -r ../ludumdare29_CrushingDepth_zzorn.zip ludumdare29_CrushingDepth_zzorn
cd ..

