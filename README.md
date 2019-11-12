# Local Gallery Server

Backend server for [Local Gallery Android app](https://github.com/memostark/LocalGallery) that provides child folders and images of selected directory.

# How to setup
- Install python 3.6
- Install and setup an Apache server, [Apache Haus](https://www.apachehaus.com/) are the recommended binaries.
- Download the repo source code with `git clone https://github.com/memostark/LocalGalleryServer`
- Create a virtual environment `python -m venv venv`
- Install the required dependencies `pip install -r requirements.txt`
- Configure the apache server, go where your Apcha directory (Apache24) is located and add this snippet:
    ```
    Define root_path "YOUR-PROJECT-PATH"
    Define files_path "IMAGE-SOURCE-PATH"

    <VirtualHost *:80>
        ServerAdmin admin-name-here
        WSGIScriptAlias / "${root_path}/index/web.wsgi"
        DocumentRoot ${root_path}

        SetEnv FOLDERS_PATH ${files_path}

        <Directory "${root_path}/index">
                Require all granted
        </Directory>

        Alias "/static/" "${root_path}/static/"
        <Directory "${root_path}/static/">
            Require all granted
        </Directory>

        # Need to match any folder/file inside images directory
        AliasMatch "^/images/(.*)$" "${files_path}/$1"
        <Directory ~ "${files_path}/.*">
            Require all granted
        </Directory>

        ErrorLog "${root_path}/logs/error.log"
        CustomLog "${root_path}/logs/access.log" common
    </VirtualHost>
    ```
    - Replace `root_path` with the full path where the code is located e.g. "C:/Documents/LocalGalleryServer"
    - Replace `files_path` with the full path where your directory with your files is located

- Go to services (press Windows key and search for services), search for Apache 2.4 and start the service
- You should be good to go, if you have your app setup you you should be able to see folders with the images inside.
