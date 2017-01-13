'''
Monitor service

The monitor service provides a JSON receiver to receive the packets from the nodes
The packets are stored in a dataStore

A networkGraph is used to process the packets from the different nodes into one network graph
for a certain timestamp.

The webServer serves the UI.
'''
import ConfigParser
from jsonReceiver import JSONReceiver
from dataStore import DataStore
from webServer import constructWebUI
from networkGraph import NetworkGraph

def main():
	config = ConfigParser.ConfigParser()
	
	config.read("config.ini")
	IP = config.get("Connection", "ip")
	UDP_PORT = int(config.get("Connection", "udp_port"))
	TCP_PORT = int(config.get("Connection", "tcp_port"))
	UDP_MaxPacketSize = int(config.get("Connection", "udp_maxpacketsize"))
	TCP_MaxPacketSize = int(config.get("Connection", "tcp_maxpacketsize"))

	packetListLimit = int(config.get("Settings", "packetlistlimit"))
	maxOffset = int(config.get("Settings", "maxoffset"))
	arrayLength = int(config.get("Settings", "arraylenght"))

	uiPort = int(config.get("UI", "port"))
	defaultDelay = int(config.get("UI", "defaultdelay"))
	statusColors = eval(config.get("UI", "statuscolors"))
	statusUnknown = config.get("UI", "statusunknown")

	# Start the services
	dataStore = DataStore(packetListLimit, maxOffset, arrayLength)
	jsonRecv = JSONReceiver(dataStore, IP, UDP_PORT, TCP_PORT, UDP_MaxPacketSize, TCP_MaxPacketSize)
	networkGraph = NetworkGraph(dataStore, defaultDelay, statusColors, statusUnknown)
	uiService = constructWebUI(networkGraph, uiPort)

	print("Monitor service started")


if __name__ == '__main__':
	main()