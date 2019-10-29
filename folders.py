import os
from urllib.parse import urljoin

from flask import abort

ARCHIVED_FOLDERS_PATH = os.environ['FOLDERS_PATH']
IMAGES_URL = os.environ['IMAGES_SERVER']

IMAGE_EXTENSIONS = ['jpg','jpeg', 'png']

FOLDERS = None

def read_all():
    '''
    This function responds to a request for api/folders

    :return:        json string of list of folders
    '''
    FOLDERS = os.listdir(ARCHIVED_FOLDERS_PATH)

    folderItems = []
    for folder in FOLDERS:
        folderPath = os.path.join(ARCHIVED_FOLDERS_PATH, folder)
        count = len(os.listdir(folderPath))

        # Find first file with image type to use as cover, else return None
        coverFile = next((fn for fn in os.listdir(folderPath)
              if any(fn.endswith(ext) for ext in IMAGE_EXTENSIONS)), None)

        if coverFile is None:
            coverUrl = ""
        else:
            # Create full url path, e.g. http://1.1.1.1:5000/images/folder/file.jpg
            fullImagePath = urljoin(IMAGES_URL, "images/") 
            personPath = urljoin(fullImagePath, folder + "/")
            coverUrl = urljoin(personPath, coverFile)

        folderItems.append({"name": folder, "coverUrl": coverUrl, "count": count})

    return folderItems

def read_folder(name):
    '''
    This function responds to a request for api/folders/{name}

    :param name:    name of folder to find
    :return:        folder matching name
    '''
    Local_Folders = FOLDERS
    if Local_Folders is None:
        Local_Folders = os.listdir(ARCHIVED_FOLDERS_PATH)

    if name in Local_Folders:
        path = os.path.join(ARCHIVED_FOLDERS_PATH, name)
        fileNames = os.listdir(path)

        # We put trailing slash because some paths are for folders
        fullImagePath = urljoin(IMAGES_URL, "images/") 
        personPath = urljoin(fullImagePath, name + "/")
        files = [urljoin(personPath, fileName) for fileName in fileNames]
    else:
        abort(404, f"Folder with {name} not found")

    return files
