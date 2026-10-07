# DeepBlue Rescue

## 1. Nombre del proyecto

DeepBlue Rescue

## 2. Descripción breve

Plataforma backend completa para organizaciones dedicadas al rescate y rehabilitación de fauna marina. El proyecto está construido con una arquitectura de capas utilizando **Java 21**, **Spring Boot 4**, y **PostgreSQL**. Permite gestionar centros de recuperación, casos de rescate, animales, expedientes médicos, especialistas y tratamientos a través de una API REST robusta y probada.

## 3. Arquitectura y Capas del Proyecto

El sistema está dividido en las siguientes capas lógicas:

### Capa de Persistencia 
Se encarga exclusivamente del almacenamiento y recuperación de datos.
- **Tecnologías:** Spring Data JPA, Hibernate, PostgreSQL.
- **Migraciones:** Flyway (`V1`, `V2`, `V3`) gestiona y versiona el esquema de la base de datos de manera automatizada.
- **Entidades:** Mapeo relacional usando anotaciones JPA (`@Entity`, `@OneToMany`, `@ManyToMany`, etc.).
- **Consultas:** Uso de *Query Methods* y consultas *JPQL* personalizadas.

### Capa de Servicio 
Es el "cerebro" de la aplicación. Contiene toda la lógica y reglas de negocio.
- **Orquestación:** Servicios como `RescueCaseService`, `TreatmentService` y `AnimalService`.
- **Transacciones:** Gestión de límites transaccionales mediante `@Transactional` para garantizar la integridad de los datos (Rollbacks automáticos en caso de fallo).
- **Mapeo:** Uso de **MapStruct** para convertir Entidades JPA en Objetos de Transferencia de Datos (DTOs tipo `record`), ocultando el modelo de base de datos.
- **Excepciones:** Lanzamiento de excepciones de negocio (`BusinessRuleException`) y de recursos (`ResourceNotFoundException`).

### Capa de Controladores - REST API 
Expone la lógica de negocio a clientes HTTP de forma estructurada.
- **Controladores REST:** Uso de `@RestController`, `@GetMapping`, `@PostMapping` y `@PatchMapping` orientados a recursos.
- **Validación de Entrada (Bean Validation):** Uso de `@Valid`, `@NotBlank`, `@NotNull`, etc., para validar DTOs antes de procesarlos.
- **Manejo Global de Errores:** Implementación de un `@RestControllerAdvice` (`GlobalExceptionHandler`) para interceptar fallos y devolver un contrato JSON unificado (`ErrorResponse`) con códigos HTTP estandarizados (400, 404, 409, 500).

### Capa de Seguridad (Proximamente)
se implementarán mecanismos de autenticación y autorización (usando Spring Security, JWT, Roles)

## 4. Modelo de datos y Relaciones

| Tabla | Descripción |
|---|---|
| `rescue_centers` | Centros de recuperación de fauna marina |
| `rescue_cases` | Casos de rescate asociados a un centro |
| `animals` | Animales asociados a un caso de rescate |
| `medical_records` | Expediente médico de cada animal |
| `specialists` | Especialistas que participan en la recuperación |
| `expertise` | Áreas de experiencia (catálogo) |
| `specialist_expertise` | Tabla asociativa de la relación N:M |
| `treatments` | Tratamientos realizados a los animales |

```text
RescueCenter 1:N RescueCase
RescueCase    1:1 Animal
Animal        1:1 MedicalRecord
Specialist    N:M Expertise
Animal        1:N Treatment
Specialist    1:N Treatment
```

## 5. Instrucciones para ejecutar la aplicación (Entorno de Desarrollo)

Requisitos: **Java 21**, **Maven** y **Docker** (para levantar la base de datos local rápidamente).

### Paso 1: Levantar base de datos PostgreSQL

Para correr la aplicación en local, necesitas una instancia de PostgreSQL. Puedes levantarla rápidamente usando Docker.

**En Linux / Mac (Bash):**
```bash
docker run --name deepblue-postgres -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=deepblue -p 5432:5432 -d postgres:16-alpine
```

**En Windows (PowerShell):**
```powershell
docker run --name deepblue-postgres -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=deepblue -p 5432:5432 -d postgres:16-alpine
```

### Paso 2: Configurar entorno (.env)

Crea un archivo `.env` en la raíz del proyecto para definir las credenciales (Spring Boot tomará estas variables automáticamente si se ha configurado para leerlas o puedes exportarlas):

**En Linux / Mac (Bash):**
```bash
echo -e "DB_URL=jdbc:postgresql://localhost:5432/deepblue\nDB_USER=postgres\nDB_PASSWORD=postgres" > .env
```

**En Windows (PowerShell):**
```powershell
Set-Content -Path .env -Value "DB_URL=jdbc:postgresql://localhost:5432/deepblue`nDB_USER=postgres`nDB_PASSWORD=postgres"
```

### Paso 3: Ejecutar el proyecto

**En Linux / Mac:**
```bash
# Otorgar permisos al wrapper si es necesario
chmod +x mvnw
# Correr la aplicación
./mvnw spring-boot:run
```

**En Windows (PowerShell):**
```powershell
.\mvnw.cmd spring-boot:run
```

## 6. Instrucciones para ejecutar tests

El proyecto cuenta con una robusta suite de pruebas divididas por capa:
- **Test de Integración (Persistencia):** Levantan un PostgreSQL genuino efímero mediante **Testcontainers**.
- **Test Unitarios (Servicios):** Prueban lógica de negocio aislando los repositorios usando **Mockito**.
- **Test de Controladores (Web):** Verifican los endpoints, validaciones HTTP y respuestas JSON utilizando **MockMvc** y `@WebMvcTest`.

**Importante:** Asegúrate de tener **Docker ejecutándose en segundo plano** antes de correr los tests, ya que Testcontainers lo requerirá para el entorno de la BD.

**En Linux / Mac:**
```bash
./mvnw clean test
```

**En Windows (PowerShell):**
```powershell
.\mvnw.cmd clean test
```

Si todo es correcto, verás un mensaje de `BUILD SUCCESS` indicando que todos los tests fueron superados.