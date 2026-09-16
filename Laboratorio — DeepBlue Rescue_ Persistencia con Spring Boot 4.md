# Laboratorio práctico — DeepBlue Rescue

## Persistencia con Java 21, Spring Boot 4, Spring Data JPA, Hibernate, Flyway, PostgreSQL y Testcontainers

**Duración máxima:** 4 horas  
**Modalidad:** Individual o parejas  
**Nivel:** Básico → intermedio  
**Proyecto:** DeepBlue Rescue

---

# 1. Propósito del laboratorio

En este laboratorio construirás desde cero la capa de persistencia de **DeepBlue Rescue**, una plataforma para organizaciones dedicadas al rescate y rehabilitación de fauna marina.

No recibirás todas las implementaciones terminadas.

A lo largo del laboratorio deberás:

- analizar requerimientos;
- diseñar tablas;
- identificar relaciones;
- crear migraciones;
- implementar entidades;
- configurar asociaciones JPA;
- crear repositories;
- utilizar métodos heredados;
- construir Query Methods;
- escribir consultas JPQL;
- ejecutar pruebas contra PostgreSQL real;
- comprobar constraints;
- diagnosticar errores.

El objetivo final no es simplemente conseguir:

```text
BUILD SUCCESS
```

sino comprender el recorrido:

```text
Modelo de negocio
        ↓
Modelo relacional
        ↓
Flyway
        ↓
PostgreSQL
        ↑
        |
      SQL
        ↑
        |
Hibernate / JPA
        ↑
        |
Spring Data JPA
        ↑
        |
Repository
        ↑
 ┌──────┼───────────┐
 │      │           │
CRUD   Query       @Query
       Methods       │
                     ↓
                    JPQL
```

---

# 2. Situación problema

**DeepBlue Rescue** trabaja con diferentes centros de recuperación de fauna marina.

Cuando un animal es encontrado herido o en peligro:

1. un centro registra un **caso de rescate**;
2. se registra el animal relacionado con ese caso;
3. se abre un expediente médico;
4. diferentes especialistas pueden participar en su recuperación;
5. cada especialista posee diferentes áreas de experiencia;
6. los tratamientos realizados al animal quedan registrados;
7. el estado del caso cambia durante el proceso de rehabilitación.

El sistema debe permitir posteriormente responder preguntas como:

- ¿Qué animales se encuentran actualmente en rehabilitación?
- ¿Qué casos pertenecen a determinado centro?
- ¿Qué animales fueron rescatados después de determinada fecha?
- ¿Qué especialistas poseen experiencia en Trauma?
- ¿Qué tratamientos ha recibido un animal?
- ¿Quién realizó cada tratamiento?
- ¿Qué tratamientos se realizaron durante un período?
- ¿Qué animales fueron tratados por especialistas con experiencia en Rehabilitation?

---

# 3. Alcance

Implementaremos las siguientes entidades:

```text
RescueCenter
RescueCase
Animal
MedicalRecord
Specialist
Expertise
Treatment
```

No implementaremos:

```text
Controller
REST API
Service
DTO
Spring Security
Frontend
Kafka
Docker Compose
```

El laboratorio está dedicado exclusivamente a persistencia.

---

# 4. Conceptos que deberás aplicar

Durante el laboratorio utilizarás:

- modelo relacional;
- entidades JPA;
- `@Entity`;
- `@Table`;
- `@Id`;
- `@GeneratedValue`;
- `@Column`;
- `@Enumerated`;
- `@OneToMany`;
- `@ManyToOne`;
- `@OneToOne`;
- `@ManyToMany`;
- `mappedBy`;
- `@JoinColumn`;
- `@JoinTable`;
- `FetchType.LAZY`;
- cascades;
- Flyway;
- PostgreSQL;
- `JpaRepository`;
- métodos heredados;
- Query Methods;
- navegación de propiedades;
- `@Query`;
- JPQL;
- `JOIN`;
- `JOIN FETCH`;
- parámetros nombrados;
- Testcontainers;
- `@ServiceConnection`;
- pruebas de integración;
- constraints reales.

---

# 5. Reglas del laboratorio

Durante la práctica se deben respetar las siguientes reglas.

### Regla 1

Flyway es responsable de crear y evolucionar el esquema.

No utilizar:

```yaml
ddl-auto: create
```

ni:

```yaml
ddl-auto: update
```

Utilizar:

```yaml
ddl-auto: validate
```

---

### Regla 2

No utilizar H2.

Todas las pruebas deben ejecutarse contra:

```text
PostgreSQL
```

mediante:

```text
Testcontainers
```

---

### Regla 3

No utilizar SQL nativo en los repositories durante este laboratorio.

Las consultas personalizadas deberán escribirse con:

```text
JPQL
```

---

### Regla 4

Antes de utilizar `@Query`, analizar si el problema puede resolverse claramente mediante un Query Method.

---

### Regla 5

No utilizar Lombok `@Data` sobre entidades.

---

# PARTE I — CREACIÓN DEL PROYECTO

# 6. Paso 1 — Crear el proyecto Spring Boot

Crear un proyecto Maven con:

```text
Group:
com.deepblue

Artifact:
deepblue-rescue

Package:
com.deepblue.rescue

Java:
21

Spring Boot:
4.1.x
```

Agregar:

```text
Spring Data JPA
PostgreSQL Driver
Flyway
Spring Boot Test
Testcontainers
```

---

# 7. Paso 2 — Revisar el pom.xml

Verificar que existan dependencias equivalentes a:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-flyway</artifactId>
</dependency>

<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-database-postgresql</artifactId>
    <scope>runtime</scope>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-testcontainers</artifactId>
    <scope>test</scope>
</dependency>

<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>testcontainers-postgresql</artifactId>
    <scope>test</scope>
</dependency>

<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>testcontainers-junit-jupiter</artifactId>
    <scope>test</scope>
</dependency>
```

No colocar manualmente versiones individuales cuando sean administradas por Spring Boot.

---

# 8. Paso 3 — Crear la estructura

Construir:

```text
src/main/java/com/deepblue/rescue
│
├── DeepBlueRescueApplication.java
│
├── domain
│   ├── RescueCenter.java
│   ├── RescueCase.java
│   ├── RescueStatus.java
│   ├── Animal.java
│   ├── AnimalSex.java
│   ├── MedicalRecord.java
│   ├── Specialist.java
│   ├── Expertise.java
│   ├── Treatment.java
│   └── TreatmentType.java
│
└── repository
    ├── RescueCenterRepository.java
    ├── RescueCaseRepository.java
    ├── AnimalRepository.java
    ├── MedicalRecordRepository.java
    ├── SpecialistRepository.java
    ├── ExpertiseRepository.java
    └── TreatmentRepository.java
```

Recursos:

```text
src/main/resources
│
├── application.yml
│
└── db/migration
    ├── V1__create_schema.sql
    └── V2__insert_expertise_catalog.sql
```

Pruebas:

```text
src/test/java/com/deepblue/rescue
│
└── PersistenceIntegrationTest.java
```

---

# PARTE II — MODELADO

# 9. Paso 4 — Analizar las relaciones

El dominio tiene estas relaciones:

```text
RescueCenter 1 ───────── N RescueCase


RescueCase 1 ───────── 1 Animal


Animal 1 ───────── 1 MedicalRecord


Specialist N ───────── M Expertise


Animal 1 ───────── N Treatment


Specialist 1 ───────── N Treatment
```

Antes de programar, responde:

1. ¿Dónde debería estar la FK entre `RescueCenter` y `RescueCase`?
2. ¿Dónde debería estar la FK entre `Animal` y `MedicalRecord`?
3. ¿Qué constraint necesitamos para convertir esa FK en una verdadera relación 1:1?
4. ¿Por qué `Specialist` y `Expertise` necesitan una tabla intermedia?
5. ¿Dónde deberían estar las FK de `Treatment`?
6. ¿Puede un tratamiento existir sin Animal?
7. ¿Puede existir sin Specialist?

---

# 10. Paso 5 — Diseñar el modelo relacional

Implementaremos:

```mermaid
erDiagram

    RESCUE_CENTER ||--o{ RESCUE_CASE : manages
    RESCUE_CASE ||--|| ANIMAL : involves
    ANIMAL ||--|| MEDICAL_RECORD : has

    SPECIALIST }o--o{ EXPERTISE : possesses

    ANIMAL ||--o{ TREATMENT : receives
    SPECIALIST ||--o{ TREATMENT : performs

    RESCUE_CENTER {
        bigint id PK
        varchar code UK
        varchar name
        varchar city
    }

    RESCUE_CASE {
        bigint id PK
        varchar case_code UK
        date rescue_date
        varchar rescue_location
        varchar status
        bigint rescue_center_id FK
    }

    ANIMAL {
        bigint id PK
        varchar animal_code UK
        varchar common_name
        varchar scientific_name
        varchar sex
        bigint rescue_case_id FK UK
    }

    MEDICAL_RECORD {
        bigint id PK
        bigint animal_id FK UK
        decimal initial_weight
        varchar initial_condition
        text injuries
        text observations
    }

    SPECIALIST {
        bigint id PK
        varchar professional_code UK
        varchar first_name
        varchar last_name
        varchar email UK
        boolean active
    }

    EXPERTISE {
        bigint id PK
        varchar name UK
    }

    TREATMENT {
        bigint id PK
        bigint animal_id FK
        bigint specialist_id FK
        timestamp performed_at
        varchar type
        text description
    }
```

---

# 11. Paso 6 — Identificar las tablas

Debes crear:

```text
rescue_centers
rescue_cases
animals
medical_records
specialists
expertise
specialist_expertise
treatments
```

Identifica cuáles son:

- tablas de entidades;
- tablas asociativas.

---

# PARTE III — CONFIGURACIÓN

# 12. Paso 7 — Configurar application.yml

Crear:

```yaml
spring:

  application:
    name: deepblue-rescue

  datasource:
    url: ${DB_URL:jdbc:postgresql://localhost:5432/deepblue}
    username: ${DB_USER:postgres}
    password: ${DB_PASSWORD:postgres}

  jpa:

    open-in-view: false

    hibernate:
      ddl-auto: validate

    show-sql: true

    properties:
      hibernate:
        format_sql: true

  flyway:
    enabled: true
    locations: classpath:db/migration
```

### Debes poder explicar

¿Por qué utilizamos:

```yaml
ddl-auto: validate
```

en lugar de:

```yaml
ddl-auto: update
```

?

---

# PARTE IV — FLYWAY

# 13. Paso 8 — Crear V1__create_schema.sql

Ahora debes escribir la primera migración.

## Tabla rescue_centers

Implementar:

```text
id
code
name
city
```

Restricciones:

```text
PK(id)
UNIQUE(code)
code NOT NULL
name NOT NULL
city NOT NULL
```

---

# 14. Paso 9 — Crear rescue_cases

Campos:

```text
id
case_code
rescue_date
rescue_location
status
rescue_center_id
```

Restricciones:

```text
PK(id)

UNIQUE(case_code)

FK rescue_center_id
    → rescue_centers.id

NOT NULL
```

Estados permitidos:

```text
ADMITTED

UNDER_EVALUATION

IN_REHABILITATION

READY_FOR_RELEASE

RELEASED

CLOSED
```

Agregar un:

```sql
CHECK
```

para impedir estados inválidos.

---

# 15. Paso 10 — Crear animals

Campos:

```text
id
animal_code
common_name
scientific_name
sex
rescue_case_id
```

Reglas:

```text
animal_code UNIQUE

rescue_case_id FK

rescue_case_id UNIQUE
```

¿Por qué también debe ser `UNIQUE`?

Porque queremos:

```text
RescueCase 1:1 Animal
```

---

# 16. Paso 11 — Crear medical_records

Campos:

```text
id
animal_id
initial_weight
initial_condition
injuries
observations
```

Implementar:

```text
PK

FK animal_id → animals.id

UNIQUE(animal_id)
```

Esto debe garantizar:

```text
Animal 1:1 MedicalRecord
```

---

# 17. Paso 12 — Crear specialists

Implementar:

```text
id
professional_code
first_name
last_name
email
active
```

Constraints:

```text
UNIQUE(professional_code)

UNIQUE(email)
```

---

# 18. Paso 13 — Crear expertise

Campos:

```text
id
name
```

Regla:

```text
name UNIQUE
```

---

# 19. Paso 14 — Crear specialist_expertise

Implementar la tabla intermedia:

```text
specialist_id
expertise_id
```

Crear:

```text
FK specialist_id

FK expertise_id
```

y una PK compuesta:

```text
PRIMARY KEY (
    specialist_id,
    expertise_id
)
```

Pregunta:

> ¿Qué problema evita la PK compuesta?

---

# 20. Paso 15 — Crear treatments

Campos:

```text
id
animal_id
specialist_id
performed_at
type
description
```

Constraints:

```text
animal_id NOT NULL

specialist_id NOT NULL

performed_at NOT NULL

type NOT NULL
```

Crear ambas FKs.

---

# 21. Paso 16 — Crear índices

Agregar índices para:

```text
rescue_cases.rescue_center_id

rescue_cases.status

rescue_cases.rescue_date

treatments.animal_id

treatments.specialist_id

treatments.performed_at
```

Pregunta:

> ¿Por qué no necesitamos crear manualmente otro índice para las PK?

---

# 22. Paso 17 — Crear V2

Crear:

```text
V2__insert_expertise_catalog.sql
```

Insertar:

```text
Marine Reptiles

Marine Mammals

Marine Birds

Trauma

Rehabilitation

Toxicology
```

---

# CHECKPOINT 1

Hasta aquí debes tener:

```text
V1
+
V2
```

pero todavía ninguna entidad JPA completamente implementada.

Debes poder identificar visualmente:

```text
PK

FK

UNIQUE

CHECK

tabla asociativa
```

---

# PARTE V — ENTIDADES

# 23. Paso 18 — Crear enums

Implementar:

```java
public enum RescueStatus {

    ADMITTED,

    UNDER_EVALUATION,

    IN_REHABILITATION,

    READY_FOR_RELEASE,

    RELEASED,

    CLOSED
}
```

Crear:

```java
public enum AnimalSex {

    MALE,
    FEMALE,
    UNKNOWN
}
```

Crear:

```java
public enum TreatmentType {

    WOUND_CARE,
    HYDRATION,
    MEDICATION,
    SURGERY,
    NUTRITION,
    PHYSIOTHERAPY,
    OBSERVATION
}
```

---

# 24. Paso 19 — Implementar RescueCenter

Debe mapear:

```text
rescue_centers
```

Implementar:

```text
@Entity

@Table

@Id

@GeneratedValue

@Column
```

Luego implementar:

```text
RescueCenter 1:N RescueCase
```

mediante:

```java
@OneToMany(mappedBy = "rescueCenter")
```

Crear además:

```java
public void addCase(RescueCase rescueCase)
```

que mantenga ambos lados sincronizados.

---

# 25. Paso 20 — Implementar RescueCase

Mapear:

```text
rescue_cases
```

Implementar los campos:

```text
id
caseCode
rescueDate
rescueLocation
status
```

Para status utilizar:

```java
@Enumerated(EnumType.STRING)
```

Implementar:

```text
RescueCase N:1 RescueCenter
```

mediante:

```java
@ManyToOne(fetch = FetchType.LAZY)
```

y:

```java
@JoinColumn(...)
```

---

# 26. Paso 21 — Agregar RescueCase 1:1 Animal

En `RescueCase` implementar:

```java
@OneToOne(
    mappedBy = "rescueCase",
    cascade = CascadeType.ALL,
    orphanRemoval = true,
    fetch = FetchType.LAZY
)
```

Crear:

```java
public void assignAnimal(Animal animal)
```

El método deberá mantener ambos lados de la relación.

---

# 27. Paso 22 — Implementar Animal

Mapear:

```text
animals
```

Campos:

```text
id
animalCode
commonName
scientificName
sex
```

Implementar:

```text
Animal → RescueCase
```

con:

```java
@OneToOne(fetch = FetchType.LAZY)
```

y:

```java
@JoinColumn(
    name = "rescue_case_id",
    unique = true
)
```

---

# 28. Paso 23 — Implementar Animal 1:1 MedicalRecord

En `Animal`:

```java
@OneToOne(
    mappedBy = "animal",
    cascade = CascadeType.ALL,
    orphanRemoval = true,
    fetch = FetchType.LAZY
)
```

Crear:

```java
public void assignMedicalRecord(
        MedicalRecord medicalRecord)
```

Pregunta:

> ¿Por qué es conveniente que el método actualice ambos lados?

---

# 29. Paso 24 — Implementar MedicalRecord

Mapear:

```text
medical_records
```

Utilizar:

```java
BigDecimal
```

para:

```text
initialWeight
```

Mapear la relación:

```java
@OneToOne(fetch = FetchType.LAZY)
@JoinColumn(
    name = "animal_id",
    nullable = false,
    unique = true
)
```

---

# CHECKPOINT 2 — Relaciones 1:1

Identifica los propietarios.

### RescueCase ↔ Animal

¿Quién tiene `@JoinColumn`?

```text
________________________
```

### Animal ↔ MedicalRecord

¿Quién tiene `@JoinColumn`?

```text
________________________
```

El propietario será normalmente el lado que mantiene físicamente la FK.

---

# 30. Paso 25 — Implementar Expertise

Mapear:

```text
expertise
```

Campos:

```text
id
name
```

Agregar:

```java
@ManyToMany(mappedBy = "expertiseAreas")
```

hacia:

```java
Set<Specialist>
```

---

# 31. Paso 26 — Implementar Specialist

Mapear:

```text
specialists
```

Campos:

```text
id
professionalCode
firstName
lastName
email
active
```

Implementar:

```text
Specialist N:M Expertise
```

mediante:

```java
@ManyToMany
@JoinTable(...)
```

El `JoinTable` debe utilizar:

```text
specialist_expertise
```

Crear:

```java
public void addExpertise(
        Expertise expertise)
```

El método debe mantener ambos lados sincronizados.

---

# 32. Paso 27 — Implementar Treatment

Esta entidad deberá ser implementada principalmente por el estudiante.

Mapear:

```text
treatments
```

Propiedades:

```java
Long id

Animal animal

Specialist specialist

LocalDateTime performedAt

TreatmentType type

String description
```

Implementar:

```text
Treatment N:1 Animal
```

y:

```text
Treatment N:1 Specialist
```

Ambas relaciones deberán utilizar:

```java
@ManyToOne(fetch = FetchType.LAZY)
```

---

# 33. Paso 28 — Completar relaciones inversas

Agregar a `Animal`:

```java
@OneToMany(mappedBy = "animal")
private List<Treatment> treatments;
```

Agregar a `Specialist`:

```java
@OneToMany(mappedBy = "specialist")
private List<Treatment> treatments;
```

Ahora el modelo será:

```text
Animal
   1
   |
   N
Treatment
   N
   |
   1
Specialist
```

---

# CHECKPOINT 3 — Mapa completo JPA

Debes poder explicar:

```text
RescueCenter
     |
     | 1:N
     v
RescueCase
     |
     | 1:1
     v
Animal
     |
     +──────── 1:1 ──────── MedicalRecord
     |
     |
     +──────── 1:N ──────── Treatment
                                |
                                | N:1
                                v
                           Specialist
                                |
                                | N:M
                                v
                           Expertise
```

---

# PARTE VI — REPOSITORIES

# 34. Paso 29 — Crear los repositories

Crear:

```java
RescueCenterRepository
```

```java
RescueCaseRepository
```

```java
AnimalRepository
```

```java
MedicalRecordRepository
```

```java
SpecialistRepository
```

```java
ExpertiseRepository
```

```java
TreatmentRepository
```

Todos deberán extender:

```java
JpaRepository<Entity, Long>
```

---

# 35. Paso 30 — Explorar métodos heredados

Sin crear consultas nuevas, identifica qué método utilizarías para:

### A

Guardar un centro.

### B

Guardar cinco entidades al mismo tiempo.

### C

Buscar un caso por ID.

### D

Saber si existe un animal con determinado ID.

### E

Contar especialistas.

### F

Eliminar un expediente.

### G

Forzar sincronización inmediata con PostgreSQL.

Opciones:

```text
save

saveAll

findById

findAll

existsById

count

delete

deleteById

flush

saveAndFlush
```

---

# PARTE VII — QUERY METHODS

# 36. Paso 31 — RescueCenterRepository

Implementar un Query Method para:

> Buscar un centro mediante su código.

Resultado:

```java
Optional<RescueCenter>
```

No utilizar `@Query`.

---

# 37. Paso 32 — RescueCaseRepository

Implementar consultas para:

### Consulta A

Buscar un caso por:

```text
caseCode
```

### Consulta B

Buscar todos los casos según:

```text
status
```

ordenados por:

```text
rescueDate ASC
```

### Consulta C

Buscar casos pertenecientes a un centro determinado.

La entrada será:

```text
rescueCenter.code
```

No utilizar el ID.

---

# 38. Paso 33 — Query Method navegando una relación

El estudiante deberá descubrir cómo representar:

```text
RescueCase
     ↓
rescueCenter
     ↓
code
```

dentro del nombre del método.

Resultado esperado conceptualmente:

```text
findByRescueCenter...
```

---

# 39. Paso 34 — AnimalRepository

Implementar:

### Consulta A

Buscar animal por:

```text
animalCode
```

### Consulta B

Buscar animales cuyo:

```text
commonName
```

contenga determinado texto ignorando mayúsculas/minúsculas.

---

# 40. Paso 35 — Query Method navegando varias entidades

Necesitamos responder:

> Obtener animales cuyo caso de rescate tenga determinado estado.

Camino:

```text
Animal
  ↓
rescueCase
  ↓
status
```

Implementar SIN `@Query`.

---

# 41. Paso 36 — Segundo Query Method navegando relaciones

Resolver:

> Buscar animales pertenecientes a un centro determinado.

La entrada será:

```text
centerCode
```

El camino será:

```text
Animal
  ↓
RescueCase
  ↓
RescueCenter
  ↓
code
```

Construir el Query Method correspondiente.

Este es uno de los ejercicios principales del laboratorio.

---

# 42. Paso 37 — Query Method con fechas

Resolver:

> Obtener los casos de rescate posteriores a determinada fecha ordenados del más reciente al más antiguo.

Debe contener conceptualmente:

```text
RescueDate

After

OrderBy

RescueDate

Desc
```

---

# PARTE VIII — @QUERY Y JPQL

# 43. Paso 38 — ExpertiseRepository

Crear:

```java
Optional<Expertise> findByNameIgnoreCase(
        String name);
```

Este sigue siendo un Query Method.

---

# 44. Paso 39 — SpecialistRepository con JPQL

Ahora necesitamos:

> Encontrar especialistas activos que posean determinada experiencia.

Utilizar:

```java
@Query
```

La consulta debe navegar:

```text
Specialist
   ↓
expertiseAreas
```

Debe incorporar:

```text
JOIN

LOWER

named parameter

active = true

ORDER BY
```

Estructura:

```java
@Query("""
    select distinct s
    from Specialist s
    join s.expertiseAreas e
    where ...
    """)
List<Specialist> findActiveByExpertise(...);
```

Completar el JPQL.

---

# 45. Paso 40 — Comparar JPQL y SQL

La consulta anterior conceptualmente generará SQL con:

```text
specialists

specialist_expertise

expertise
```

Pero JPQL utiliza:

```text
Specialist

expertiseAreas

Expertise
```

Responde:

> ¿Por qué en JPQL escribimos `Specialist` y no `specialists`?

---

# 46. Paso 41 — TreatmentRepository

Primero implementar mediante Query Method:

> Obtener tratamientos de un animal ordenados cronológicamente.

Entrada:

```text
animal.id
```

Orden:

```text
performedAt ASC
```

---

# 47. Paso 42 — Consulta JPQL con Treatment

Implementar:

> Obtener tratamientos realizados entre dos fechas.

Utilizar:

```java
@Query
```

Parámetros:

```java
LocalDateTime start

LocalDateTime end
```

Orden:

```text
performedAt ASC
```

---

# 48. Paso 43 — JPQL navegando tres entidades

Implementar:

> Obtener todos los tratamientos realizados a animales pertenecientes a un centro determinado.

La consulta deberá recorrer:

```text
Treatment
     ↓
Animal
     ↓
RescueCase
     ↓
RescueCenter
```

Entrada:

```text
centerCode
```

No utilizar SQL nativo.

---

# 49. Paso 44 — JPQL con N:M

Implementar:

> Buscar tratamientos realizados por especialistas que posean determinada experiencia.

La navegación será:

```text
Treatment
     ↓
Specialist
     ↓
Expertise
```

Necesitarás:

```text
JOIN

DISTINCT

named parameter
```

---

# PARTE IX — TESTCONTAINERS

# 50. Paso 45 — Crear el container PostgreSQL

Crear la clase:

```java
@Testcontainers
@SpringBootTest
@Transactional
class PersistenceIntegrationTest {
```

Agregar:

```java
@Container
@ServiceConnection
static final PostgreSQLContainer postgres =
        new PostgreSQLContainer(
                "postgres:18-alpine")
            .withDatabaseName("deepblue_test")
            .withUsername("deepblue")
            .withPassword("deepblue");
```

Import actual:

```java
org.testcontainers.postgresql.PostgreSQLContainer
```

---

# 51. Paso 46 — Inyectar repositories

Inyectar:

```text
RescueCenterRepository

RescueCaseRepository

AnimalRepository

SpecialistRepository

ExpertiseRepository

TreatmentRepository
```

También:

```java
JdbcTemplate
```

---

# 52. Paso 47 — Test de Flyway

Implementar un test que consulte:

```text
flyway_schema_history
```

y compruebe que al menos:

```text
V1
V2
```

fueron ejecutadas.

El objetivo es demostrar que el esquema fue creado por:

```text
Flyway
```

y no por Hibernate.

---

# 53. Paso 48 — Test de métodos heredados

Crear:

```text
DB-CAR
DeepBlue Caribbean Center
Santa Marta
```

Comprobar utilizando:

```text
save()

findById()

existsById()

count()
```

No crear Query Methods para este test.

---

# 54. Paso 49 — Test relación 1:N

Crear:

```text
RescueCenter
    |
    +--- RescueCase 1
    |
    +--- RescueCase 2
```

Persistirlos.

Comprobar posteriormente que ambos casos pertenecen al mismo centro.

---

# 55. Paso 50 — Test RescueCase 1:1 Animal

Crear:

```text
RES-2026-001
```

y asignarle:

```text
AN-2026-001
Green Sea Turtle
Chelonia mydas
```

Persistir la relación.

Comprobar:

```text
case.getAnimal()

animal.getRescueCase()
```

---

# 56. Paso 51 — Test Animal 1:1 MedicalRecord

Crear:

```text
AN-2026-002
```

con:

```text
initialWeight = 28.40

initialCondition = STABLE

injuries =
Left front flipper injury
```

Guardar utilizando cascade.

Comprobar que ambos objetos obtuvieron ID.

---

# 57. Paso 52 — Test N:M

Recuperar de V2:

```text
Trauma

Rehabilitation
```

Crear:

```text
Elena Vargas
```

y asociar ambas especialidades.

Persistir.

Consultar:

```text
specialist_expertise
```

indirectamente mediante la entidad.

Comprobar que Elena tiene:

```text
2 expertiseAreas
```

---

# 58. Paso 53 — Test Query Method simple

Crear varios casos:

```text
RES-001
IN_REHABILITATION

RES-002
READY_FOR_RELEASE

RES-003
IN_REHABILITATION
```

Ejecutar la consulta por status.

Resultado:

```text
2 casos
```

---

# 59. Paso 54 — Test Query Method navegando relaciones

Crear dos centros:

```text
DB-CAR

DB-PAC
```

Registrar animales en casos pertenecientes a ambos centros.

Ejecutar la consulta:

> animales pertenecientes al centro DB-CAR.

Comprobar que no retorna animales del otro centro.

---

# 60. Paso 55 — Test JPQL de especialistas

Crear:

```text
Elena → Trauma + Rehabilitation

Mateo → Marine Mammals + Rehabilitation

Sofia → Marine Birds + Trauma
```

Consultar:

```text
Trauma
```

Resultado esperado:

```text
Elena
Sofia
```

---

# 61. Paso 56 — Crear tratamientos

Registrar para un animal:

```text
Treatment 1
WOUND_CARE
Elena

Treatment 2
HYDRATION
Elena

Treatment 3
OBSERVATION
Mateo
```

Persistirlos.

---

# 62. Paso 57 — Test Query Method de tratamientos

Ejecutar:

> Obtener tratamientos de determinado animal ordenados cronológicamente.

Verificar:

```text
Treatment 1
Treatment 2
Treatment 3
```

---

# 63. Paso 58 — Test JPQL por intervalo

Crear tratamientos:

```text
2026-08-01 10:00

2026-08-10 10:00

2026-08-20 10:00
```

Consultar entre:

```text
2026-08-05

y

2026-08-15
```

Debe regresar únicamente:

```text
2026-08-10
```

---

# PARTE X — CONSTRAINTS

# 64. Paso 59 — Probar UNIQUE

Intentar guardar:

```text
AN-100
```

dos veces.

Utilizar:

```java
saveAndFlush()
```

La segunda operación debe producir:

```java
DataIntegrityViolationException
```

---

# 65. Paso 60 — Probar FK

Intentar construir una situación que viole una FK.

Analizar primero:

> ¿JPA permite llegar fácilmente a esta situación?

> ¿Qué papel cumple `nullable = false`?

> ¿Qué papel cumple realmente el constraint PostgreSQL?

---

# 66. Paso 61 — Probar CHECK

El constraint de `rescue_cases.status` solo admite:

```text
ADMITTED
UNDER_EVALUATION
IN_REHABILITATION
READY_FOR_RELEASE
RELEASED
CLOSED
```

Responder:

> Si Java usa un enum, ¿por qué sigue siendo útil mantener el CHECK en PostgreSQL?

---

# PARTE XI — EVOLUCIÓN DEL ESQUEMA

# 67. Paso 62 — Nuevo requerimiento

DeepBlue comienza a utilizar dispositivos GPS para algunos animales antes de liberarlos.

Ahora debemos almacenar:

```text
tracking_device_code
```

No modificar:

```text
V1__create_schema.sql
```

Crear:

```text
V3__add_tracking_device_to_animal.sql
```

---

# 68. Paso 63 — Implementar V3

Agregar:

```text
tracking_device_code VARCHAR(50)
```

Debe poder ser:

```text
NULL
```

pero cuando exista deberá ser único.

Agregar:

```text
UNIQUE
```

---

# 69. Paso 64 — Actualizar Animal

Agregar:

```java
private String trackingDeviceCode;
```

Mapear correctamente con:

```java
@Column(...)
```

Ejecutar:

```bash
mvn clean test
```

Hibernate debe validar exitosamente el nuevo esquema.

---

# 70. Experimento obligatorio

Comenta temporalmente V3 o cambia incorrectamente el nombre de la columna Java.

Por ejemplo:

```java
@Column(name = "gps_device")
```

Ejecuta:

```bash
mvn test
```

Analiza el error.

Después restaura:

```java
@Column(name = "tracking_device_code")
```

El objetivo es demostrar qué significa realmente:

```yaml
ddl-auto: validate
```

---

# PARTE XII — RETO INTEGRADOR

# 71. Escenario

DeepBlue Caribbean recibe una tortuga marina.

## Centro

```text
Code:
DB-CAR

Name:
DeepBlue Caribbean

City:
Santa Marta
```

---

## Caso

```text
Case Code:
RES-2026-100

Rescue Date:
2026-08-18

Location:
Bahía Concha

Status:
IN_REHABILITATION
```

---

## Animal

```text
Code:
AN-2026-100

Common name:
Green Sea Turtle

Scientific name:
Chelonia mydas

Sex:
FEMALE
```

---

## Expediente

```text
Initial Weight:
27.80

Condition:
STABLE

Injuries:
Injury caused by fishing net

Observations:
Possible plastic ingestion
```

---

## Especialista

```text
Professional Code:
SPEC-001

Name:
Elena Vargas

Email:
elena@deepblue.org
```

Experiencias:

```text
Marine Reptiles

Trauma

Rehabilitation
```

---

## Tratamientos

### Tratamiento 1

```text
Type:
WOUND_CARE

Description:
Cleaning of left front flipper
```

### Tratamiento 2

```text
Type:
HYDRATION

Description:
Subcutaneous fluid therapy
```

---

# 72. Paso 65 — Persistir el escenario

Debes conseguir:

```text
RescueCenter
      ↓
RescueCase
      ↓
Animal
      ↓
MedicalRecord
```

y:

```text
Animal
  ↓
Treatment
  ↓
Specialist
  ↓
Expertise
```

---

# 73. Paso 66 — Resolver consultas

Implementa y prueba las siguientes preguntas.

### Consulta 1

¿Existe el caso:

```text
RES-2026-100
```

?

Decide si necesitas crear un método nuevo.

---

### Consulta 2

Obtener todos los casos:

```text
IN_REHABILITATION
```

---

### Consulta 3

Obtener animales pertenecientes a:

```text
DB-CAR
```

---

### Consulta 4

Buscar animales cuyo nombre común contenga:

```text
turtle
```

ignorando mayúsculas.

---

### Consulta 5

Obtener especialistas con experiencia:

```text
Trauma
```

---

### Consulta 6

Obtener todos los tratamientos de:

```text
AN-2026-100
```

ordenados cronológicamente.

---

### Consulta 7

Obtener tratamientos realizados por especialistas con experiencia:

```text
Rehabilitation
```

---

### Consulta 8

Obtener tratamientos realizados entre dos fechas.

---

# 74. Paso 67 — Clasificar cada consulta

Completa:

| Necesidad | Mecanismo |
|---|---|
| Buscar una entidad por ID | __________ |
| Buscar caso por código | __________ |
| Casos según status | __________ |
| Animales de determinado centro | __________ |
| Especialistas según expertise | __________ |
| Tratamientos en intervalo | __________ |
| Tratamientos por expertise del especialista | __________ |

Opciones:

```text
Método heredado

Query Method

@Query + JPQL
```

---

# PARTE XIII — RETO SIN GUÍA

# 75. Nuevo requerimiento

El equipo médico pregunta:

> Necesitamos conocer todos los animales que se encuentran en rehabilitación y que hayan recibido al menos un tratamiento realizado por un especialista con experiencia en Trauma.

Debes implementar la consulta.

No se proporciona el código.

---

# 76. Analizar antes de programar

El camino es:

```text
Animal
  ↓
RescueCase
  ↓
status
```

y:

```text
Animal
  ↓
treatments
  ↓
specialist
  ↓
expertiseAreas
```

Debes decidir si utilizar:

```text
Query Method
```

o:

```text
@Query + JPQL
```

Justifica tu elección.

---

# 77. Condiciones

La consulta debe:

- recibir `RescueStatus`;
- recibir el nombre de expertise;
- ignorar mayúsculas en expertise;
- evitar animales duplicados;
- utilizar JPQL si decides que es la alternativa apropiada.

Pista:

```text
DISTINCT
```

puede ser importante.

---

# PARTE XIV — VERIFICACIÓN FINAL

# 78. Ejecutar

```bash
mvn clean test
```

El resultado debe ser:

```text
BUILD SUCCESS
```

---

# 79. Comprobar Docker

Durante las pruebas ejecutar en otra terminal:

```bash
docker ps
```

Debes observar temporalmente un:

```text
postgres
```

creado por Testcontainers.

---

# 80. Analizar los logs

Identifica:

```text
Flyway
```

ejecutando:

```text
V1
V2
V3
```

y después:

```text
Hibernate
```

validando el esquema.

---

# 81. Qué deberías poder explicar

Al finalizar debes poder explicar sin consultar tus apuntes:

### 1.

¿Qué diferencia existe entre:

```text
JPA
Hibernate
Spring Data JPA
PostgreSQL
```

---

### 2.

¿Qué componente crea las tablas?

---

### 3.

¿Qué componente ejecuta las migraciones?

---

### 4.

¿Qué hace:

```text
ddl-auto=validate
```

?

---

### 5.

¿Qué significa:

```java
mappedBy
```

?

---

### 6.

¿Cómo identificas al propietario de una relación?

---

### 7.

¿Dónde está físicamente la FK de:

```text
RescueCenter 1:N RescueCase
```

?

---

### 8.

¿Qué permite que:

```text
Animal 1:1 MedicalRecord
```

sea realmente 1:1 en PostgreSQL?

---

### 9.

¿Por qué:

```text
Specialist N:M Expertise
```

requiere una tabla intermedia?

---

### 10.

¿Qué diferencia existe entre:

```java
findById()
```

y:

```java
findByCaseCode()
```

?

---

### 11.

¿Qué es un Query Method?

---

### 12.

¿Qué significa navegar asociaciones mediante:

```text
findByRescueCaseRescueCenterCode(...)
```

?

---

### 13.

¿Qué es `@Query`?

---

### 14.

¿Qué es JPQL?

---

### 15.

¿Por qué JPQL utiliza:

```text
Specialist
```

en vez de:

```text
specialists
```

?

---

### 16.

¿Qué diferencia existe entre:

```java
save()
```

y:

```java
saveAndFlush()
```

?

---

### 17.

¿Por qué estamos probando constraints con PostgreSQL y no únicamente mediante Java?

---

### 18.

¿Por qué Testcontainers es útil?

---

# 82. Criterio para elegir una consulta

Utiliza este árbol mental:

```text
¿Ya existe en JpaRepository?
            |
       ┌────┴─────┐
       │          │
      Sí          No
       │          │
       ▼          ▼
   heredado    ¿Consulta simple
               sobre atributos?
                    |
               ┌────┴────┐
               │         │
              Sí         No
               │         │
               ▼         ▼
            Query     ¿Varias relaciones,
            Method     joins o lógica?
                           |
                           ▼
                      @Query + JPQL
```

---

# 83. Checklist técnico

Antes de entregar:

```text
[ ] Java 21

[ ] Spring Boot 4

[ ] Maven

[ ] PostgreSQL

[ ] Flyway

[ ] ddl-auto=validate

[ ] V1__create_schema.sql

[ ] V2__insert_expertise_catalog.sql

[ ] V3__add_tracking_device_to_animal.sql

[ ] RescueCenter

[ ] RescueCase

[ ] Animal

[ ] MedicalRecord

[ ] Specialist

[ ] Expertise

[ ] Treatment

[ ] relación 1:N

[ ] relación 1:1

[ ] relación N:M

[ ] relaciones Treatment N:1

[ ] PK

[ ] FK

[ ] UNIQUE

[ ] CHECK

[ ] tabla asociativa

[ ] Repository

[ ] métodos heredados

[ ] Query Methods simples

[ ] Query Methods navegando relaciones

[ ] @Query

[ ] JPQL

[ ] JOIN

[ ] consulta JPQL con varias asociaciones

[ ] Testcontainers

[ ] PostgreSQL real

[ ] @ServiceConnection

[ ] test de Flyway

[ ] test 1:N

[ ] test 1:1

[ ] test N:M

[ ] test Query Method

[ ] test JPQL

[ ] test UNIQUE

[ ] test del reto integrador

[ ] mvn clean test → BUILD SUCCESS
```

---

# 84. Entregables

Entregar el proyecto completo con:

```text
deepblue-rescue/
│
├── pom.xml
│
├── src/main
│
├── src/test
│
└── README.md
```

El `README.md` debe contener:

1. nombre del proyecto;
2. descripción breve;
3. modelo de datos;
4. relaciones;
5. instrucciones para ejecutar;
6. instrucciones para ejecutar tests;
7. explicación de Flyway;
8. explicación de Testcontainers;
9. listado de Query Methods implementados;
10. listado de consultas JPQL implementadas.

---

# 85. Resultado de aprendizaje esperado

Al finalizar debes poder observar:

```java
findByRescueCaseRescueCenterCode(...)
```

y mentalmente recorrer:

```text
Animal
   │
   ▼
rescueCase
   │
   ▼
rescueCenter
   │
   ▼
code
```

También debes poder observar:

```java
@Query("""
    select distinct ...
    from ...
    join ...
    """)
```

y comprender el flujo:

```text
Repository
    ↓
@Query
    ↓
JPQL
    ↓
Hibernate
    ↓
SQL
    ↓
PostgreSQL
```

Y finalmente comprender que una anotación como:

```java
@OneToOne
```

no es suficiente por sí sola para diseñar correctamente una base de datos.

El diseño completo debe mantener coherencia entre:

```text
MODELO DEL NEGOCIO

        ↓

MODELO RELACIONAL

        ↓

CONSTRAINTS POSTGRESQL

        ↓

MIGRACIONES FLYWAY

        ↓

ENTIDADES JPA

        ↓

REPOSITORIES

        ↓

CONSULTAS

        ↓

PRUEBAS DE INTEGRACIÓN
```

Ese recorrido completo constituye el objetivo central del laboratorio.