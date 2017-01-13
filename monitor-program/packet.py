'''
Class to store packets.
A packet consists of a timeStamp, the address of the sender(node) and the data.
The get functions copies the data to keep the original data intact.
'''
from copy import copy

class Packet:
	def __init__(self, node, timeStamp, data):
		self.node = node
		self.timeStamp = timeStamp
		self.data = data

	def __copy__(self):
		return Packet(copy(self.node), copy(self.timeStamp), copy(self.data))

	def getNode(self):
		return copy(self.node)

	def getTimeStamp(self):
		return copy(long(self.timeStamp))

	def getData(self):
		return copy(self.data)

	def __str__(self):
		return  "node: %s, %d" % (self.node, self.timeStamp)

	def __lt__(self, other):
		return self.getTimeStamp() < other.getTimeStamp()