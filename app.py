from flask import render_template
import connexion, socket, threading, sys

app = connexion.App(__name__, specification_dir='./')

app.add_api('swagger.yml')

flask_app = app.app

UDP_IP = "0.0.0.0"
UDP_PORT = 80
RESPONSE_MESSAGE = b"DISCOVER_FUIFSERVER_RESPONSE"

@app.route('/')
def home():
    return render_template('home.html')

# For debugging purposes
def send_broadcast():
    sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    sock.bind((UDP_IP, UDP_PORT))

    while True:
        data, addr = sock.recvfrom(1024) # buffer size is 1024 bytes
        print(f"received message: {data}, address: {addr}")

        if (data == b"DISCOVER_FUIFSERVER_REQUEST"):
            print("Responding to message...")
            sock.sendto(RESPONSE_MESSAGE, addr)

if __name__ == '__main__':
    t = threading.Thread(target=send_broadcast)
    t.daemon = True
    t.start()
    flask_app.run(debug=True)