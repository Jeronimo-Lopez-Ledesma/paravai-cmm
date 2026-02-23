# Platform BOM (Java + .NET + Python)

This repository defines the **platform-wide baseline** for all backend services in DPS:

- **Java**: central Maven BOM (`java/service-bom/pom.xml`)
- **.NET**: central package versions (`dotnet/Directory.Packages.props`)
- **Python**: central dependency constraints (`python/constraints.txt`)

Every service must:

- Import the corresponding BOM/constraints for its language.
- Avoid declaring dependency versions locally, unless explicitly approved.
- Track the BOM version used (see `docs/VERSIONING.md`).

## Structure

- `java/service-bom`: Maven BOM for all Java services (WebFlux, Reactor, gRPC, Mongo, Service Foundation, etc.).
- `dotnet/Directory.Packages.props`: central NuGet package versions for .NET-based services.
- `python/constraints.txt`: pinned versions for Python services (FastAPI, gRPC, etc.).
- `docs/`: architecture and versioning guidelines.
- `.gitlab-ci.yml`: CI pipeline to validate all BOMs on every change.

## How services should use it

### Java services

In the root `pom.xml` of each service monorepo:

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.dekra.platform</groupId>
            <artifactId>service-bom</artifactId>
            <version>1.0.0</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```
Then declare dependencies without versions.

### .NET services

At solution or project root:
```xml
<Project Sdk="Microsoft.NET.Sdk">
    <Import Project="path/to/Directory.Packages.props" />
    ...
</Project>
```
No <PackageReference Version="..."> should be used locally if the package is controlled by the BOM.

### Python services
Install dependencies using the shared constraints file:

```
pip install -r requirements.txt -c constraints.txt
```
Services must not override versions defined in constraints.txt unless explicitly allowed.

See docs/ARCHITECTURE.md and docs/VERSIONING.md for details.

## 3. CHANGELOG

`platform-bom/CHANGELOG.md`

```markdown
# Changelog – DEKRA Platform BOM

## [1.0.0] - 2025-11-20

### Added
- Initial Maven BOM for Java services (`java/service-bom/pom.xml`).
- Initial central `Directory.Packages.props` for .NET services.
- Initial `constraints.txt` for Python services.
- CI pipeline to validate Java/.NET/Python BOMs.

### Changed
- N/A

### Removed
- N/A