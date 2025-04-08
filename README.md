# Chem Query Platform Demo

This repository contains a **chem-query-platform-demo** showcasing how to use the [`chem-query-platform`](https://github.com/quantori/chem-query-platform) library.

## Overview

The goal of this project is to demonstrate the configuration and execution of an **Akka-based asynchronous data processing pipeline** using the `chem-query-platform` library.

With this library, you can easily define and orchestrate a highly scalable and fault-tolerant data flow in an **Akka cluster environment**.

This demo specifically showcases a simple analyzer for an SDF (Structure Data File). Using interfaces such as `TaskDescriptionSerDe`, `ResultAggregator`, and `DataProvider`, it demonstrates a pipeline that processes an SDF file and counts the number of molecules contained within.

## Features

- 🧪 Example integration of `chem-query-platform`
- ⚙️ Configurable Akka-based pipeline setup
- ⚡ Asynchronous data stream processing
- ☁️ Cluster-ready architecture using Akka Cluster
- 🧬 Demonstrates SDF file parsing and molecule counting

## Getting Started

> Note: This is a demo repository. Make sure to clone and explore the [`chem-query-platform`](https://github.com/your-org/chem-query-platform) for full library documentation.

### Prerequisites

- JDK 17
- Gradle 8+
- PostgreSQL (used for data storage via Slick)
- Docker (optional for running PostgreSQL or simulating an Akka cluster)

### Running the Demo

```bash
gradle clean build
docker-compose up --build
```

Once the application is running, you can upload an SDF file for processing using the following HTTP request:

### Upload SDF File

**POST** `http://localhost:80/api/v1/upload`

**Body**: `multipart/form-data`
- `file`: (select your `.sdf` file or use files/example.sdf)

### Configuration

Pipeline stages and Akka settings can be customized via `application.conf` in the `resources` directory.

## Repository Structure

```
.
├── src/main/java           # Demo pipeline logic
├── resources/application.conf
├── build.gradle
└── README.md