import os
from flask import abort

ARCHIVED_FOLDERS_PATH = os.environ['FOLDERS_PATH']

FOLDERS = {
    "First": {
        "name": "John"
    },
    "Second": {
        "name": "Paul"
    },
    "Third": {
        "name": "George"
    },
    "Fourth": {
        "name": "Ringo"
    }
}

def read_all():
    '''
    This function responds to a request for api/folders

    :return:        json string of list of folders
    '''
    folders = os.listdir(ARCHIVED_FOLDERS_PATH)
    return [{"name": folder} for folder in folders]

def read_folder(name):
    '''
    This function responds to a request for api/folders/{name}

    :param name:    name of folder to find
    :return:        folder matching name
    '''

    if name in FOLDERS:
        folder = FOLDERS.get(name)
    else:
        abort(404, f"Folder with {name} not found")

    return folder
