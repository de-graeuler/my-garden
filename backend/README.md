Steps to setup a LAMP server to build and run this program

 1. Setup Ubuntu Server (VM) using the latest LTS version 
 1. Add 'universe' channel to all apt sources
 1. Install aptitude (for convenience)
 1. safe-upgrade the system
 1. install tasksel
 1. tasksel remove cloud-image (it's not required)
 1. tasksel install lamp-server
 1. a2enmod php7.2
 1. install unzip php7.2-dom php7.2-mbstring php7.2-xdebug php7.2-zip
 1. clone this git repo
 1. run ./composer.sh in /backend to install composer.phar 
 1. ./composer.phar install 
 1. ./vendor/phing/phing/bin/phing
