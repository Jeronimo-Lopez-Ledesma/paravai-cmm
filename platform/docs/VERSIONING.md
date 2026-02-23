# Versioning Strategy – Platform BOM

We use **semantic versioning** for the Platform BOM:

- **MAJOR** – Breaking changes for any language stack.
- **MINOR** – Backwards-compatible upgrades (new libraries, version bumps).
- **PATCH** – Security updates and bug fixes.

Example: `1.2.3`
- `1` – Major baseline (Java 21 + Spring Boot 3.x + .NET 8 + Python stack)
- `2` – Minor updates (Reactor upgrade, new Service Foundation modules)
- `3` – Security patches

## Release Process

1. Open a ticket: request to add/change/remove a dependency in any of the BOMs.
2. Platform team evaluates impact and decides target version bump (major/minor/patch).
3. Implement changes in:
    - `java/service-bom/pom.xml`
    - `dotnet/Directory.Packages.props`
    - `python/constraints.txt`
4. Update `CHANGELOG.md`.
5. Tag the repo (`vX.Y.Z`) and let CI publish the BOM artifacts.

## Service Obligations

- Each service must declare the BOM version it uses.
- Services should not fall behind more than **2 minor versions** from the latest BOM.
