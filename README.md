# Edge Computing

## Overview
This repository is intended to keep track of the project related to the Distributed and Pervasive Systems course for the Computer Science Master at Università degli Studi di Milano.

## Info
This project aims at building a simulated city with Sensors sending signals and Edge Nodes receiving and elaborating them, to send them to a RESTful Web Service.

Modeling a city with Sensors spread over the city sending measurements to the nearest Edge Node connected to the Edge Nodes peer-to-peer network. Each Node receives measurements from different Sensors, compute statistics using those measurements and send them to the Edge Node Coordinator. This last one compute statistics using the statistics received and send every statistic to a RESTful Web Service Server Cloud. Analist clients may query the Server Cloud to obtain global and local statistics computed by Edge Nodes.

For more info, read the report inside the repository.
