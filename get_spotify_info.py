# coding: utf-8
import base64
import configparser
import json
import requests
import sys
import urllib.parse

def get_client_values(file_loc=None):
    if file_loc is None:
        file_loc = sys.argv[1]
    config = configparser.ConfigParser()
    config.read(file_loc)
    return config["Spotify"]["client_id"], config["Spotify"]["client_secret"]

def get_auth_token():
    client_id, client_secret = get_client_values()
    auth_field = f"{client_id}:{client_secret}"
    auth_field = auth_field.encode('utf-8')
    encoded_auth_header = base64.b64encode(auth_field)

    headers = {"Authorization": "Basic {}".format(encoded_auth_header.decode('utf-8'))}
    body = {"grant_type": "client_credentials"}
    spotify_auth_url = "https://accounts.spotify.com/api/token"
    r = requests.post(spotify_auth_url, headers=headers, data=body)
    return r.json()['access_token']

def get_album_information(album, artist, auth_token=None):
    if auth_token is None:
        auth_token = get_auth_token()
    search_url = "https://api.spotify.com/v1/search"
    query = f"album:{album} artist:{artist}"
    encoded_query_str = urllib.parse.urlencode({"q": query,
                                                "type": "album"})
    headers = {"Authorization": f"Bearer {auth_token}"}
    r = requests.get(f"{search_url}?{encoded_query_str}", headers=headers)
    # TODO: Figure out a workaround so that you get
    # Whitney by Whitney instead of Whitney by Whitney Houston
    return r.json()['albums']['items'][0]

def parse_spotify_album_info(album_info):
    album_name = album_info["name"]
    album_url = album_info["external_urls"]["spotify"]
    artist = album_info["artists"][0]
    artist_url = artist["external_urls"]["spotify"]
    artist_name = artist["name"]
    images = album_info["images"] #Preserve all the images.
                                  # Just in case I want to add logic to switch
                                  # to smaller images on smaller devices
    return {"album-name": album_name,
            "album-url": album_url,
            "artist-name": artist_name,
            "artist-url": artist_url,
            "images": images}

def get_album_list():
    with open('favorites.txt', 'r') as f:
        data = f.read().strip()
    albums = data.split('\n')
    def parse_album_info(line):
        parts = line.split(' by ')
        album, artist = parts
        return {"album": album, "artist": artist}
    return [parse_album_info(x) for x in albums]

album_list = get_album_list()
auth_token = get_auth_token()

album_infos = [get_album_information(x["album"], x["artist"], auth_token=auth_token) for x in album_list]
output = [parse_spotify_album_info(x) for x in album_infos]

print(json.dumps(output))

