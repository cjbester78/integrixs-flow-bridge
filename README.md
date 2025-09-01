# Integrix Flow Bridge Backend

Enterprise integration middleware platform for visual flow composition, adapter management, and orchestration.

## Quick Start

```bash
# Build and deploy
./deploy.sh

# Access the application
http://localhost:8080
```

## Project Structure

```
integrix-flow-bridge/
├── backend/            # Spring Boot application (clean architecture)
├── shared-lib/         # Common DTOs, enums, utilities
├── adapters/           # Adapter implementations
├── data-access/        # JPA entities & repositories
├── engine/             # Flow execution engine
├── db/                 # Database migrations
├── docs/               # Project documentation
└── frontend-ui/        # React/TypeScript frontend
```

## Documentation

See the [docs](docs/) folder for detailed documentation:
- [Architecture Documentation](docs/README.md)
- [API Documentation](docs/INTEGRIX_FLOW_BRIDGE_ARCHITECTURE.md)
- [Production Deployment](docs/PRODUCTION_DEPLOYMENT.md)

## Development

- **Backend**: Spring Boot 3.3, Java 21
- **Frontend**: React 18, TypeScript, Vite
- **Database**: PostgreSQL
- **Build**: Maven

## License

See [EULA.md](EULA.md) for license information.