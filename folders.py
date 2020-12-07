import os, socket
from urllib.parse import urljoin

from flask import abort

ARCHIVED_FOLDERS_PATH = os.environ['FOLDERS_PATH']

dirName = os.path.basename(ARCHIVED_FOLDERS_PATH)

IMAGE_EXTENSIONS = ['jpg','jpeg', 'png']

FOLDERS = None

def _get_default_path():
    IMAGES_URL = os.environ.get('IMAGES_SERVER')

    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    s.connect(("8.8.8.8", 80))
    path = s.getsockname()[0].strip()
    s.close()

    path = path if not IMAGES_URL.strip() else IMAGES_URL
    if(not path.startswith(r"http://")):
        path = r"http://" + path
    return path

DEFAULT_PATH = _get_default_path()

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
            # Create full url path, e.g. http://1.1.1.1:5000/images/folderName/fileName.jpg
            fullImagePath = urljoin(DEFAULT_PATH, "images/") 
            personPath = urljoin(fullImagePath, folder + "/")
            coverUrl = urljoin(personPath, coverFile)

        folderItems.append({"name": folder, "coverUrl": coverUrl, "count": count})

    return {"name": dirName, "folders": folderItems}

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
        fullImagePath = urljoin(DEFAULT_PATH, "images/") 
        personPath = urljoin(fullImagePath, name + "/")
        files = [urljoin(personPath, fileName) for fileName in fileNames]
    else:
        abort(404, f"Folder with {name} not found")

    return files