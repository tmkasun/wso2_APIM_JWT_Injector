#!/usr/bin/env python3
import json
from http import server, HTTPStatus
import socketserver
import ssl
from os import path
import jwt
from pprint import pprint


class EchoMicroService(server.BaseHTTPRequestHandler):
    def do_GET(self):
        self.common_handler()

    def do_POST(self):
        self.common_handler()

    def setStatusCode(self):
        self.send_response(HTTPStatus.OK)

    def common_handler(self):
        self.setStatusCode()
        self.send_header("Content-type", "application/json")
        self.end_headers()
        jwt = self.decodeJWT()
        print("DEBUG: ------- JWT Token Info -------")
        pprint(jwt)
        print("DEBUG: ------- JWT Token Info END -------")
        request_headers = {}
        request_body = self.getBody()
        for header in self.headers._headers:
            request_headers[header[0]] = header[1]
        response = {
            'description': "This response contains most of the information came into the Echo Micro Service including request headers and body",
            'ok': 200,
            'request_connection': {
                'ip': self.client_address[0],
                'port': self.client_address[1]
            },
            'path': self.path,
            'HTTP Method': self.command,
            'request_headers': request_headers,
            'body': request_body,
        }
        jresponse = json.dumps(response)
        self.wfile.write(str.encode(jresponse))

    @staticmethod
    def run():
        port = EchoMicroService.port
        print('INFO: (Secured: {})Echo Micro Service listening on localhost:{}...'.format(EchoMicroService.secured,
                                                                                          port))
        socketserver.TCPServer.allow_reuse_address = True
        httpd = socketserver.TCPServer(('', port), EchoMicroService)
        cert_path = path.dirname(__file__) + 'yourpemfile.pem'
        print("DEBUG: cert_path = " + cert_path)
        if EchoMicroService.secured:
            httpd.socket = ssl.wrap_socket(
                httpd.socket, server_side=True, certfile=cert_path)
        httpd.serve_forever()

    def getBody(self):
        blocking = False
        content_length = int(self.headers.get("content-length", -1))
        content_type = self.headers.get("content-type", -1)
        method = self.command
        if blocking and content_length == -1 and method in ["POST", "PUT", "PATCH"] and content_type != -1:
            request_body = ""
            while True:
                line = self.rfile.readline().decode("UTF-8").strip()
                if(len(line) == 0):
                    break
                request_body += line
            return request_body
        return None if content_length in [0, -1] else self.rfile.read(content_length).decode("UTF-8")

    def decodeJWT(self, headerName="X-JWT-Assertion"):
        jwt_header = self.headers.get(headerName)
        pub_key_path = path.dirname(__file__) + '/jwt_validation.pub.key'
        with open(pub_key_path, 'r') as public_key:
            return jwt.decode(jwt_header, public_key.read(), algorithms='RS512')

    port = 8008
    secured = False
    digestAuthSecret = 'this()is+my#s3cR3a1i4'


def main():
    """
        Run as a standalone server if needed
    """
    EchoMicroService.run()


if __name__ == '__main__':
    main()
