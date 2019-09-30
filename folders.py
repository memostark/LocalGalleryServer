import os
from flask import abort

ARCHIVED_FOLDERS_PATH = os.environ['FOLDERS_PATH']

FOLDERS = None

def read_all():
    '''
    This function responds to a request for api/folders

    :return:        json string of list of folders
    '''
    FOLDERS = os.listdir(ARCHIVED_FOLDERS_PATH)
    return [{"name": folder} for folder in FOLDERS]

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
        files = os.listdir(path)
    else:
        abort(404, f"Folder with {name} not found")

    return files
