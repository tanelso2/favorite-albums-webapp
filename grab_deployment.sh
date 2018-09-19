#!/bin/bash

FILE_LOCATION=${1?"Error! Download location not specified"}

curl -s https://api.github.com/repos/tanelso2/favorite-albums-webapp/releases/latest \
| grep "browser_download_url" \
| cut -d : -f 2,3 \
| tr -d \" \
| wget -qi - -O $FILE_LOCATION
