'''
JSONReceiver
Receiver to receive the packets from the nodes.

The TCP receiver is to synchronize the time between the node and the monitor service.
The UDP receiver is to receive the node information packets.
'''

from socket import *
import thread as thread
import json
from copy import copy

from dataStore import DataStore
from packet import Packet
import time
import struct

class JSONReceiver:
	def __init__(self, dataStore, IP="127.0.0.1", UDP_PORT=7000, TCP_PORT=7001, UDP_MaxPacketSize=8096, TCP_MaxPacketSize=8096):
		self.HOST = IP
		self.UDP_PORT = UDP_PORT
		self.TCP_PORT = TCP_PORT
		self.UDP_MaxPacketSize = UDP_MaxPacketSize
		self.TCP_MaxPacketSize = TCP_MaxPacketSize
		self.dataStore = dataStore

		# Starts the receivers
		self.jsonListener()

	# Function to start the TCP and UDP receiver in seperate threads
	def jsonListener(self):
		thread.start_new_thread(self.tcpReceiver, ())
		thread.start_new_thread(self.udpReceiver, ())


	# Starts a UDP socket to receive node information packets
	def udpReceiver(self):
		print self.UDP_PORT
		s = socket(AF_INET, SOCK_DGRAM)
		s.bind((self.HOST, self.UDP_PORT))

		while 1:
			# Decodes the packets
			data = s.recvfrom(self.UDP_MaxPacketSize)[0].decode('utf-8')

			# Sends the packet to the nodeUpdate function
			thread.start_new_thread(self.nodeUpdate, (json.loads(data),))


	# TCP receiver to synchronize the time between the node and the monitor
	def tcpReceiver(self):
		s = socket(AF_INET, SOCK_STREAM)
		s.bind((self.HOST, self.TCP_PORT))
		s.listen(10) #how many connections can it receive at one time

		while 1:
			conn, addr = s.accept() #accept the connection
			thread.start_new_thread(self.sync, (conn, addr,))


	# Function to synchronize the time between the monitor and node
	def sync(self, connection, addr):
		data = connection.recv(self.UDP_MaxPacketSize)
		time1d = long(round(time.time() * 1000))
		try:
			json_data = json.loads(data.decode('utf-8'))

			if "type" in json_data and json_data["type"] == "sync":
				json_data["time1d"] = time1d
				json_data["time2"] = long(round(time.time() * 1000))
				connection.send(str(json_data).encode())

		except Exception, e:
			pass
		finally:
			connection.close()


	# Function to add the new node information to the data store
	def nodeUpdate(self, json_data):
		if "type" in json_data and json_data["type"] == "data" and "node" in json_data:
			packet = Packet(json_data["node"]["address"], json_data["node"]["timeStamp"], json_data["node"])
			self.dataStore.addPacket(packet)

