# DeepBlue Rescue

## 1. Nombre del proyecto

DeepBlue Rescue

## 2. Descripcion breve

Capa de persistencia de una plataforma para organizaciones dedicadas al rescate
y rehabilitacion de fauna marina. El proyecto modela centros de recuperacion,
casos de rescate, animales, expedientes medicos, especialistas, areas de
experiencia y tratamientos, utilizando Java 21, Spring Boot 4, Spring Data JPA,
Hibernate, Flyway, PostgreSQL y Testcontainers.

No incluye controllers, servicios, DTOs ni frontend: esta dedicado
exclusivamente a persistencia.

## 3. Modelo de datos

| Tabla | Descripcion |
|---|---|
| `rescue_centers` | Centros de recuperacion de fauna marina |
| `rescue_cases` | Casos de rescate asociados a un centro |
| `animals` | Animales asociados a un caso de rescate |
| `medical_records` | Expediente medico de cada animal |
| `specialists` | Especialistas que participan en la recuperacion |
| `expertise` | Areas de experiencia (catalogo) |
| `specialist_expertise` | Tabla asociativa de la relacion N:M |
| `treatments` | Tratamientos realizados a los animales |

## 4. Relaciones

```
RescueCenter 1:N RescueCase
RescueCase    1:1 Animal
Animal        1:1 MedicalRecord
Specialist    N:M Expertise
Animal        1:N Treatment
Specialist    1:N Treatment
```

## 5. Instrucciones para ejecutar

Requisitos: Java 21, Maven y (opcionalmente) Docker para los tests.

```bash
cd deepblue-rescue
./mvnw clean compile
```

La aplicacion espera una base PostgreSQL. Las credenciales se configuran con
variables de entorno opcionales:

```text
DB_URL=jdbc:postgresql://localhost:5432/deepblue
DB_USER=postgres
DB_PASSWORD=postgres
```

En Windows:

```powershell
.\mvnw.cmd clean compile
```

## 6. Instrucciones para ejecutar tests

```bash
# Docker debe estar en ejecucion (Testcontainers levanta PostgreSQL real)
./mvnw clean test
```

En Windows:

```powershell
.\mvnw.cmd clean test
```

Las pruebas levantan un contenedor PostgreSQL `postgres:18-alpine` mediante
Testcontainers y `@ServiceConnection`. No se utiliza H2.

## 7. Explicacion de Flyway

Flyway es responsable de crear y evolucionar el esquema de forma versionada.
Las migraciones viven en `src/main/resources/db/migration`:

- `V1__create_schema.sql`: crea las tablas con PK, FK, UNIQUE, CHECK e indices.
- `V2__insert_expertise_catalog.sql`: inserta el catalogo inicial de expertise.
- `V3__add_tracking_device_to_animal.sql`: agrega el codigo opcional y unico del
  dispositivo GPS.

Cada migracion aplicada queda registrada en `flyway_schema_history`. Como el
esquema lo gestiona Flyway, Hibernate no crea ni actualiza tablas: se configura
`ddl-auto: validate`, de modo que Hibernate solo comprueba que sus entidades son
coherentes con el esquema existente.

## 8. Explicacion de Testcontainers

Testcontainers ejecuta los tests de integracion contra una instancia real de
PostgreSQL levantada en un contenedor Docker desechable. La clase
`PersistenceIntegrationTest` declara un contenedor `postgres:18-alpine` con
`@Container` y `@ServiceConnection`, por lo que Spring Boot configura
automaticamente el datasource apuntando a ese contenedor. Esto permite comprobar
constraints reales (UNIQUE, FK, CHECK), el comportamiento de Flyway y el
funcionamiento de JPA/Hibernate contra PostgreSQL genuino.

## 9. Query Methods implementados

- `RescueCenterRepository.findByCode(String)`
- `RescueCaseRepository.findByCaseCode(String)`
- `RescueCaseRepository.findByStatusOrderByRescueDateAsc(RescueStatus)`
- `RescueCaseRepository.findByRescueCenterCode(String)`
- `RescueCaseRepository.findByRescueDateAfterOrderByRescueDateDesc(LocalDate)`
- `AnimalRepository.findByAnimalCode(String)`
- `AnimalRepository.findByCommonNameContainingIgnoreCase(String)`
- `AnimalRepository.findByRescueCaseStatus(RescueStatus)`
- `AnimalRepository.findByRescueCaseRescueCenterCode(String)`
- `MedicalRecordRepository.findByAnimalId(Long)`
- `ExpertiseRepository.findByNameIgnoreCase(String)`
- `TreatmentRepository.findByAnimalIdOrderByPerformedAtAsc(Long)`

## 10. Consultas JPQL implementadas

- `RescueCaseRepository.findByCaseCodeWithAnimal(String)`: caso por codigo con
  `JOIN FETCH` del animal asociado.
- `SpecialistRepository.findActiveByExpertise(String)`: especialistas activos
  con determinada experiencia (`JOIN`, `LOWER`, `active = true`, `ORDER BY`).
- `TreatmentRepository.findPerformedBetween(start, end)`: tratamientos entre dos
  fechas (`between`, orden ASC).
- `TreatmentRepository.findByRescueCenterCode(String)`: tratamientos de animales
  de un centro (navega `Treatment -> Animal -> RescueCase -> RescueCenter`).
- `TreatmentRepository.findBySpecialistExpertise(String)`: tratamientos de
  especialistas con determinada experiencia (`JOIN`, `DISTINCT`).
- `AnimalRepository.findByStatusAndTreatmentSpecialistExpertise(RescueStatus,
  String)`: animales en un estado cuyos tratamientos fueron realizados por
  especialistas con determinada experiencia (`DISTINCT`).

Las consultas usan entidades y atributos Java, no nombres de tablas SQL ni SQL
nativo.

## Constraints probados

Las pruebas de integracion comprueban restricciones `UNIQUE` (centros y
dispositivos GPS), la clave foranea de los casos de rescate y el `CHECK` de
estados validos. Tambien se prueba el escenario integrador de una tortuga
marina, su expediente, especialista, expertise y tratamientos.