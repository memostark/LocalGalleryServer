import sys, os
 
# Project root dir
BASE_DIR = os.path.join( os.path.dirname( __file__ ), '..' )
sys.path.insert(0, BASE_DIR)

def application(environ, start_response):
    for key in ['FOLDERS_PATH', 'IMAGES_SERVER']:
        os.environ[key] = environ.get(key, '')
    
    from app import flask_app as _application
    return _application(environ, start_response)