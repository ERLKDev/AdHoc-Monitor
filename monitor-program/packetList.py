'''
Data structure to create a packet list
The packet list is automatically ordered on the time stamps of the packets.
'''
from packet import Packet
from threading import Lock
import copy

class PacketList:
	def __init__(self, node, packetLimit=600, maxOffset=1000, arrayLength=10000):
		self.node = node
		self.packetLimit = packetLimit
		self.packets = []
		self.maxOffset = maxOffset
		self.arrayLength = arrayLength

		self.lock = Lock()

	# Returns the address of the node
	def getNode(self):
		return self.node

	# Adds a packet to the list
	def addPacket(self, new):
		self.lock.acquire()

		# If the list is full, it removes the first packet
		if len(self) >= self.packetLimit:			
			del self.packets[-1]

		# Adds the packet and sorts the list
		self.packets.append(new)
		self.packets.sort(reverse=True)
		self.lock.release()

	# Returns the length of the packet list
	def __len__(self):
		return len(self.packets)

	# Iters of the packet list
	def __iter__(self):
		return iter(self.packets)

	# To str function
	def __str__(self):
		output = ""
		for x in self:
			output += str(x) + "\n"
		return output
	
	# Returns a packet for a certain time stamp
	def getPacket(self, timeStamp, maxOffset=None):
		if maxOffset is None:
			maxOffset = self.maxOffset

		for x in self:
			if x.getTimeStamp() <= timeStamp and timeStamp - x.getTimeStamp() <= maxOffset:
				return x

		return None

	# Returns a array of packets for a certain interval
	def getPacketArray(self, timeStamp, arrayLength=None):
		if arrayLength is None:
			arrayLength = self.arrayLength

		output = []
		for x in self:
			if x.getTimeStamp() <= timeStamp and x.getTimeStamp() >= timeStamp - arrayLength:
				output.append(x)

		return output
