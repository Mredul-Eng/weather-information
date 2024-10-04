# Weather Aggregation System

This project implements a distributed weather aggregation system that allows clients to request weather data and content servers to upload new weather updates.

## Table of Contents
- [Overview](#overview)
- [System Components](#system-components)
- [Lamport Clock Synchronization](#lamport-clock-synchronization)
- [Features](#features)
- [Testing](#testing) 
  - [Unit Testing](#unit-testing)
  - [Integration Testing](#integration-testing)
- [Running the System](#running-the-system)

## Overview

The Weather Aggregation System consists of three main components:
1. **Aggregation Server**: Receives weather updates via HTTP PUT from content servers and serves the latest weather data to clients via HTTP GET.
2. **Content Server**: Sends weather updates by reading from a local file and uploading them to the aggregation server.
3. **Client**: Requests weather data from the aggregation server and displays the aggregated weather feed.

The system supports multiple content servers and clients, ensuring that updates and requests are processed in the correct order using Lamport clocks.

## System Components

### 1. Aggregation Server
- Accepts weather data updates from content servers via HTTP PUT.
- Responds to client requests for weather data via HTTP GET.
- Synchronizes PUT and GET operations using Lamport clocks.
- Removes weather data from content servers that have not communicated within the last 30 seconds.

### 2. Content Server
- Reads weather data from local files.
- Sends HTTP PUT requests to update weather data on the aggregation server.
- Supports multiple content servers making simultaneous updates.

### 3. Client
- Sends HTTP GET requests to the aggregation server to fetch the latest weather data.
- Displays the aggregated weather information.

## Lamport Clock Synchronization

Lamport clocks ensure that all operations (PUT and GET) are ordered consistently:
- **PUT Requests**: Multiple content servers uploading data are serialized using Lamport timestamps.
- **GET Requests**: Clients receive the correct weather data after applying any preceding PUT operations.

## Features

- **Concurrency Management**: Supports multiple clients and content servers interacting simultaneously.
- **Weather Data Expiry**: Automatically removes data that has expired or from inactive content servers.
- **Persistent Storage**: The aggregation server stores weather data until it is removed due to expiration.
- **Lamport Clocks**: Synchronizes events across distributed servers and clients.

## Testing

The Weather Aggregation System has been tested using unit tests for individual components and integration tests to verify interactions between the aggregation server, content servers, and clients. The tests cover:
 - Correct behavior of HTTP PUT and GET requests.
 - Handling of concurrent requests using Lamport clocks.
 - Expiration and removal of outdated weather data.

## Unit Testing 

The unit tests are designed to check the functionality of the following components:
1. **Aggregation Server**: Verifies that weather data is stored, retrieved, and expired as expected. 
2. **Content Server**: Ensures weather data is appropriately read from local files and sent to the aggregation server. 
3. **Client**: Confirms that clients can retrieve weather data from the aggregation server.

## Integration Testing

The integration tests simulate real-world interactions between the aggregation server, content servers, and clients.

The integration tests are designed to check the function of the following components:
 

1. **Simultaneous PUT and GET Requests**: 
- Tests the behavior when multiple clients send GET requests while content servers send PUT requests. 
- Verifies that GET requests to return the correct data and that data is recovered and overwritten correctly.

 2. **Concurrent PUT Requests from Multiple Content Servers**: 
- Simulates multiple content servers trying to upload weather data simultaneously.
- Ensures that Lamport clocks are used to maintain the correct order of operations.

 3. **Data Expiry After Server Failure**: 
 - Tests the scenario where a content server stops sending updates for over 30 seconds.
 - Confirms that the aggregation server correctly removes the outdated data from that content server.

## Running the System

### Start the Aggregation Server
To run the aggregation server, execute:
```bash
java -cp ".;gson-2.10.jar" AggregationServer
```

### Start the Content Server
To run the content server, execute:
```bash
java -cp ".;gson-2.10.jar" ContentServer http://localhost:4567 weather-data.txt
```

### Start the Client
To run the client, execute:
```bash
java -cp ".;gson-2.10.jar" GETClient http://localhost:4567
```
 
To know more details, please visit the repository (https://github.com/Mredul-Eng/weather-information.git)


