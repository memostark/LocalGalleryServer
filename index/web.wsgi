import sys, os, socket, threading
 
# Project root dir
BASE_DIR = os.path.join( os.path.dirname( __file__ ), '..' )
sys.path.insert(0, BASE_DIR)

# Settings for UDP server
UDP_IP = "0.0.0.0"
UDP_PORT = 80
RESPONSE_MESSAGE = b"DISCOVER_FUIFSERVER_RESPONSE"

def start_udp_server():
    sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    sock.bind((UDP_IP, UDP_PORT))

    while True:
        data, addr = sock.recvfrom(1024) # buffer size is 1024 bytes
        print(f"received message: {data}, address: {addr}")

        if (data == b"DISCOVER_FUIFSERVER_REQUEST"):
            print("Responding to message...")
            sock.sendto(RESPONSE_MESSAGE, addr)


def application(environ, start_response):
    for key in ['FOLDERS_PATH', 'IMAGES_SERVER']:
        os.environ[key] = environ.get(key, '')

    # Run in another thread to avoid blocking flask
    t = threading.Thread(target=start_udp_server)
    t.daemon = True
    t.start()

    
    from app import flask_app as _application
    return _application(environ, start_response)