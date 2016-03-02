#!/bin/bash
# deploying 'deskshare' to /usr/share/red5/webapps

sbt compile
sbt package
sudo rm -r /usr/share/red5/webapps/deskshare
sudo cp -r target/webapp/ /usr/share/red5/webapps/deskshare
sudo rm /usr/share/red5/webapps/deskshare/WEB-INF/lib/*
sudo cp ~/dev/bigbluebutton/deskshare/app/target/webapp/WEB-INF/lib/bbb-deskshare-akka_2.11-0.0.1.jar \
 ~/dev/bigbluebutton/deskshare/app/target/webapp/WEB-INF/lib/scala-library-* \
 ~/dev/bigbluebutton/deskshare/app/target/webapp/WEB-INF/lib/akka-* \
 ~/dev/bigbluebutton/deskshare/app/target/webapp/WEB-INF/lib/config-1.3.0.jar \
  /usr/share/red5/webapps/deskshare/WEB-INF/lib/
sudo chmod -R 777 /usr/share/red5/webapps/deskshare
sudo chown -R ubuntu:ubuntu /usr/share/red5/webapps/deskshare

# TODO change the owner username to 'firstuser'

