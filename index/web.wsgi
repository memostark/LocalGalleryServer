import sys, os
 
# Project root dir
sys.path.insert(0, 'D:/Documentos/Proyectos/Python/GalleryServer')

def application(environ, start_response):
    for key in ['FOLDERS_PATH', 'IMAGES_SERVER']:
        os.environ[key] = environ.get(key, '')
    
    from app import flask_app as _application
    return _application(environ, start_response)