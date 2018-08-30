#!/bin/bash

rm deployment.tar.gz || true

curl -s https://api.github.com/repos/tanelso2/favorite-albums-webapp/releases/latest \
| grep "browser_download_url" \
| cut -d : -f 2,3 \
| tr -d \" \
| wget -qi -

tar -zxf deployment.tar.gz -C /www/landing_page/favorites/albums
