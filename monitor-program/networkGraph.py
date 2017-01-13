'''
The networkGraph is used to make a JSON object that can be used to generate the UI.
It combines the different packets of the nodes into one JSON object for a certain time.
'''

from packet import Packet
from packetList import PacketList
from dataStore import DataStore
import time
import datetime
import json

class NetworkGraph:
	def __init__(self, dataStore, defaultDelay=500, statusColors={}, statusUnknown="gray"):
		self.dataStore = dataStore
		self.defaultDelay = defaultDelay
		self.delay = 0
		self.statusColors = statusColors
		self.statusUnknown = statusUnknown
		self.charts = ["speedChart", "ioTotalChart", "ioSendChart", "ioRecvChart", "cpuUsageChart", "cpuTotalUsageChart"]


	# Prepares the node data for the UI
	def processNodeData(self, node, timeStamp, data):
		# Adds additional information for the UI
		data["id"] = node
		data["label"] = node
		data["color"] = self.getStatusColor(data["status"])

		# Adds the charts for the node
		for x in self.charts:
			data[x] = self.dataStore.getNodeChart(node, timeStamp, x)
		return data


	# Returns the color of the status
	def getStatusColor(self, status):
		if (status not in self.statusColors):
			return self.statusUnknown
		return self.statusColors[status]


	# Creates the edges for the UI
	def processEdges(self, data):
		edges = []

		# For each node
		for node in data:
			neighbours = data[node]["neighbours"]

			# For each neighbour of the node
			for neighbour in neighbours:
				# Gets the edge information
				edgeId, edgeFrom, edgeTo, tag, other = self.getEdgeData(node, neighbour["address"])
			
				# Check if the edge already exist
				edge = None
				for y in edges:
					if y["id"] == edgeId:
						edge = y
						break

				# Creates the edge if it doesn't exist
				if edge is None:
					edge = {"id" : edgeId, "from" : edgeFrom, "to" : edgeTo, "arrows" : "", "color" : "orange"}
					edges.append(edge)

				# Adds the arrow to show the data flow
				arrow = ""
				if neighbour["dataSend"]:
					edge["arrows"] += other + ";"
					edge["color"] = "green"

				# Adds the additional information to the edge
				edge[tag+"Send"] = neighbour["bytesSend"]
				edge[tag+"Receive"] = neighbour["bytesReceived"]

		return edges


	# Generates the edge information
	def getEdgeData(self, node1, node2):
		if node1 < node2:
			return (node1 + "_:_" + node2, node1, node2, "from", "to")

		return (node2 + "_:_" + node1, node2, node1, "to", "from")

	# Generates the graph for the UI
	def generateGraph(self):
		output = {}

		# Gets the current timestamp (current time + default delay + extra delay set by the user)
		timeStamp = long(round(time.time() * 1000)) - self.defaultDelay - self.delay
		output["time"] = datetime.datetime.fromtimestamp(timeStamp / 1000.0).strftime('%H:%M:%S')

		# Get all the data from the nodes
		data = self.dataStore.getAllNodeData(timeStamp)

		# For each node process the data and add it to the output
		output["nodes"] = []
		for x in data:
			output["nodes"].append(self.processNodeData(x, timeStamp, data[x]))

		# Get the edge information of the nodes
		output["edges"] = self.processEdges(data)

		return json.dumps(output)

	# Adds a delay to the visualization
	def setDelay(self, delay):
		self.delay = delay

