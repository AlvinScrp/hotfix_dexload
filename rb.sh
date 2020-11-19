#!/usr/bin/env bash
gradlew :plugin:uploadArchives
gradlew clean :app:assemble
gradlew clean :appa:assemble
echo "------------rebuild success!------------------"