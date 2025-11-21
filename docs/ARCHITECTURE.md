# Platform BOM Architecture

The `platform-bom` repository defines a **polyglot platform baseline** for all backend services:

- **Java BOM** (`java/service-bom/pom.xml`)
- **.NET Package Directory** (`dotnet/Directory.Packages.props`)
- **Python Constraints** (`python/constraints.txt`)

## Goals

- Homogeneous architecture across services (WebFlux, gRPC, logging, observability, security).
- Centralized dependency management per language.
- Easier upgrades (Spring, Reactor, gRPC, Mongo, Testcontainers, etc.).
- Governance: enforce allowed frameworks and versions.

## Usage

- Java services must import the BOM and drop local dependency versions.
- .NET services must import the central package props file.
- Python services must install under the shared constraints.

## Validation & Governance

- CI checks validate:
    - Java BOM builds and imports vendor BOMs correctly.
    - .NET BOM is syntactically valid and has a small test project.
    - Python constraints are pinned and consistent.

Service-level repos may add their own meta-tests to ensure:
- No local versions are declared for BOM-managed dependencies.
- No unapproved libraries are used.
