# Development Workflow Guidelines

This document outlines the development workflow and best practices for the Integrix Flow Bridge project.

## Prerequisites

- Java 17 or higher
- Maven 3.8+
- Node.js 18+
- Git
- IDE with Java support (VSCode, IntelliJ IDEA, or Eclipse)

## Development Setup

### 1. IDE Configuration

#### Visual Studio Code
- Install recommended extensions: Run "Extensions: Show Recommended Extensions" in Command Palette
- Settings are pre-configured in `.vscode/settings.json`
- Java formatting will be applied automatically on save

#### IntelliJ IDEA
- Import the project as a Maven project
- Code style settings are in `.idea/codeStyles/Project.xml`
- Enable "Reformat code on save" in Settings → Tools → Actions on Save

#### Eclipse
- Import as "Existing Maven Projects"
- Use the provided `eclipse-formatter.xml` for code formatting
- Configure save actions to format code and organize imports

### 2. Git Hooks

A pre-commit hook has been installed that will:
- Run `mvn compile` to check for compilation errors
- Check for basic code formatting issues
- Prevent commits if compilation fails

To skip the hook in emergency situations (not recommended):
```bash
git commit --no-verify -m "Emergency fix"
```

### 3. Code Formatting

The project uses consistent code formatting across all files:

#### Java Files
- 4 spaces indentation
- 120 character line limit
- Spaces around operators
- Opening braces on same line

#### TypeScript/JavaScript
- 2 spaces indentation
- 100 character line limit
- Single quotes for strings
- Semicolons required

#### XML/JSON
- 2 spaces indentation

### 4. Development Workflow

#### Before Starting Work
1. Pull latest changes: `git pull origin master`
2. Create feature branch: `git checkout -b feature/your-feature-name`

#### While Developing
1. **Write code** following the existing patterns in the codebase
2. **Format code** - Your IDE should auto-format on save
3. **Test locally** - Run `mvn compile` frequently
4. **Check changes** - Use `git diff` to review your modifications

#### Before Committing
1. **Run compilation**: `mvn clean compile`
2. **Run tests**: `mvn test` (if applicable to your changes)
3. **Stage changes**: `git add .`
4. **Commit**: `git commit -m "feat: Description of changes"`
   - The pre-commit hook will run automatically

#### Commit Message Format
Use conventional commits format:
- `feat:` New feature
- `fix:` Bug fix
- `docs:` Documentation changes
- `style:` Code formatting (no functional changes)
- `refactor:` Code restructuring
- `test:` Adding or updating tests
- `chore:` Maintenance tasks

### 5. Common Commands

#### Backend Development
```bash
# Compile all modules
mvn clean compile

# Compile specific module
mvn compile -pl adapters

# Run without tests
mvn clean install -DskipTests

# Run specific module tests
mvn test -pl backend

# Run application
cd backend && mvn spring-boot:run
```

#### Frontend Development
```bash
cd frontend

# Install dependencies
npm install

# Development server (hot reload)
npm run dev

# Type checking
npm run type-check

# Linting
npm run lint

# Build for production
npm run build
```

### 6. Troubleshooting Compilation Errors

If you encounter compilation errors:

1. **Check error location**: Note the file and line number
2. **Common issues**:
   - Missing closing braces `}`
   - Unclosed try blocks without catch/finally
   - Missing semicolons
   - Undefined variables or methods
   - Import statements issues

3. **Quick fixes**:
   ```bash
   # See detailed errors
   mvn compile

   # Check specific file syntax
   javac -cp "target/classes:$(mvn dependency:build-classpath | grep -v '\[' | tr ':' '\n' | paste -sd:)" src/main/java/path/to/YourFile.java
   ```

### 7. Code Quality Checklist

Before pushing code, ensure:

- [ ] Code compiles without errors (`mvn compile`)
- [ ] No unused imports
- [ ] Meaningful variable and method names
- [ ] Complex logic has comments
- [ ] No hardcoded values (use configuration)
- [ ] Error handling is appropriate
- [ ] Logging statements use appropriate levels
- [ ] No sensitive data in code or logs

### 8. Working with Multi-Module Project

The project structure:
```
├── adapters/          # Integration adapters
├── backend/           # Main Spring Boot application
├── data-access/       # JPA entities and repositories
├── engine/            # Flow execution engine
├── frontend/          # React application
├── shared-lib/        # Common utilities
└── ... other modules
```

When making changes:
- Changes to shared-lib affect all modules
- Always compile from root directory first
- Module-specific changes can be tested with `-pl` flag

### 9. Performance Considerations

- Avoid N+1 queries in JPA (use proper fetching strategies)
- Use pagination for large result sets
- Implement caching where appropriate
- Profile before optimizing

### 10. Security Guidelines

- Never commit credentials or secrets
- Use environment variables for sensitive configuration
- Validate all user inputs
- Use parameterized queries (no string concatenation for SQL)
- Keep dependencies updated

## Getting Help

- Check existing code for patterns
- Review the ARCHITECTURE.md file
- Look at test files for usage examples
- Ask team members for clarification

Remember: Consistency is key. When in doubt, follow the existing patterns in the codebase.