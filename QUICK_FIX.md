# Quick Fix Guide

## Current Situation

The project was created successfully but has compilation errors due to Lombok annotation processing. The entities use `@Getter` and `@Setter` annotations from Lombok, which need to be processed at compile time.

## Issues Found

1. **Lombok Not Processing**: The `@Getter` and `@Setter` annotations are not generating methods
2. **UserDetails Interface**: The `User` entity needs to properly implement Spring Security's `UserDetails` interface methods

## Quick Fix Steps

### Option 1: Comment Out Business Logic Methods (Fastest)

Temporarily remove the business logic methods from entities that reference other entity methods until services are implemented:

**Files to modify:**
- `VendingMachine.java` - Remove `addProductPrice` and `removeProductPrice` methods
- `Product.java` - Remove `getPriceForMachine` and `getWeightedAverageCost` methods
- `ProcurementBatch.java` - Remove `addItem`, `removeItem`, `recalculateTotals` methods
- `RestockingLog.java` - Remove `addRestockItem`, `removeRestockItem`, `getTotalItemsRestocked` methods

### Option 2: Fix Lombok Configuration

Add Lombok annotation processor configuration to `pom.xml`:

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
            <configuration>
                <excludes>
                    <exclude>
                        <groupId>org.projectlombok</groupId>
                        <artifactId>lombok</artifactId>
                    </exclude>
                </excludes>
            </configuration>
        </plugin>

        <!-- Add this -->
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <configuration>
                <annotationProcessorPaths>
                    <path>
                        <groupId>org.projectlombok</groupId>
                        <artifactId>lombok</artifactId>
                        <version>${lombok.version}</version>
                    </path>
                </annotationProcessorPaths>
            </configuration>
        </plugin>
    </plugins>
</build>
```

### Option 3: Use IntelliJ IDEA (Recommended)

1. Open the project in IntelliJ IDEA
2. Install the "Lombok" plugin (File → Settings → Plugins)
3. Enable annotation processing (File → Settings → Build → Compiler → Annotation Processors → Enable annotation processing)
4. Rebuild the project

## Temporary Workaround - Minimal Running Version

Since the core issue is Lombok, let me create a minimal version that will compile and run:

Run these commands:

```bash
# Navigate to project
cd /Users/ericgu/IdeaProjects/Carrie/Vending

# I'll create a minimal fix
```

## What Works

✅ Database schema (Flyway migrations)
✅ Database connection
✅ Spring Boot configuration
✅ Entity definitions (structure is correct)
✅ Repository interfaces
✅ DTO records

## What Needs Attention

❌ Lombok annotation processing
❌ Entity business logic methods
❌ Service layer (not yet created)
❌ Controller layer (not yet created)
❌ Security configuration (not yet created)

## Recommended Next Steps

1. **Open in IntelliJ IDEA** - This is the easiest path as it handles Lombok automatically
2. **Remove business logic methods** from entities temporarily
3. **Add service layer** following the IMPLEMENTATION_GUIDE.md
4. **Add controllers** for REST API
5. **Configure Spring Security** with JWT

## Alternative: Simplified Start

If you want to start quickly without fixing Lombok now, I can:
1. Remove the problematic business logic methods
2. Create simple service and controller classes
3. Get a basic API running

Would you like me to create this simplified version?
