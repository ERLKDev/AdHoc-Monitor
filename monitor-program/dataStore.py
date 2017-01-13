'''
Data structure to store the packets of the different nodes
For each node a packet list is created to store the packets of each node seperate
'''

from packet import Packet
from packetList import PacketList
from threading import Lock

class DataStore:
	def __init__(self, packetLimit=600, maxOffset=1000, arrayLength=10000):
		self.packetLimit = packetLimit
		self.maxOffset = maxOffset
		self.arrayLength = arrayLength
		self.nodes = {}
		self.lock = Lock()

	# Add packet to the datastore
	def addPacket(self, packet):
		self.lock.acquire()
		node = packet.getNode()

		# Creates new packetlist if it doesn't already exists
		if node not in self.nodes:
			self.nodes[node] = PacketList(node, self.packetLimit, self.maxOffset, self.arrayLength)

		# Add packet to the packet list of the node
		self.nodes[node].addPacket(packet)

		self.lock.release()


	# Iters over the different nodes in the datastore
	def __iter__(self):
		return iter(self.nodes.keys())


	# Returns the packet of a node for a certain time stamp
	def getNodeData(self, node, timeStamp):
		packet = self.nodes[node].getPacket(timeStamp)

		if packet is None:
			return None

		return packet.getData()


	# Returns the packet for a certain timestamp for each node in the datastore 
	def getAllNodeData(self, timeStamp):
		output = {}
		for x in self:
			packet = self.nodes[x].getPacket(timeStamp)
			
			if packet is None:
				continue

			output[x] = packet.getData()
		return output

	# Returns a array of values to create a chart for a node
	def getNodeChart(self, node, timeStamp, chartName):

		# Gets all the packet of a certain interval
		packets = self.nodes[node].getPacketArray(timeStamp)

		# Adds the values of the chart to an output array
		output = []
		for x in packets:
			data = x.getData()[chartName]
			for y in data:
				output.append([long(y), data[y]])

		# Sort and return the output array
		return sorted(output, key=lambda x: x[0], reverse=False)
