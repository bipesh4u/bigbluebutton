#!/bin/bash
# deploying 'deskshare' to /usr/share/red5/webapps

sbt compile
sudo cp -r target/webapp/ /usr/share/red5/webapps/deskshare
sudo chmod -R 777 /usr/share/red5/webapps/deskshare
sudo chown -R ubuntu:ubuntu /usr/share/red5/webapps/deskshare

# TODO change the owner username to 'firstuser'

