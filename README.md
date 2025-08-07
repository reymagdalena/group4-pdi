# 🏢 PDI - Sistema de Gestión Institucional

## 📋 Descripción del Proyecto

**PDI (Proyecto de Desarrollo Institucional)** es una aplicación web desarrollada en Spring Boot que gestiona las actividades, reservas de espacios, usuarios y pagos de la Asociación de Sordos del Uruguay. El sistema permite administrar actividades institucionales, reservar espacios, gestionar usuarios con diferentes perfiles y manejar el sistema de pagos y cuotas.

### 🎯 Funcionalidades Principales

- **Gestión de Usuarios**: Registro, autenticación y gestión de perfiles (Administrador, Usuario, Socio)
- **Gestión de Actividades**: Creación, modificación y eliminación de actividades institucionales
- **Reservas de Espacios**: Sistema de reservas con validación de disponibilidad
- **Gestión de Pagos**: Control de pagos de actividades y cuotas mensuales
- **Reportes y Auditoría**: Seguimiento de cambios y generación de reportes
- **Seguridad**: Autenticación JWT y control de acceso basado en roles

## 🛠️ Tecnologías Utilizadas

### Backend
- **Java 21** - Lenguaje de programación
- **Spring Boot 3.5.0** - Framework principal
- **Spring Security** - Autenticación y autorización
- **Spring Data JPA** - Persistencia de datos
- **PostgreSQL 16** - Base de datos principal
- **H2 Database** - Base de datos para pruebas
- **JWT** - Tokens de autenticación
- **Maven** - Gestión de dependencias

### Herramientas de Desarrollo
- **Docker & Docker Compose** - Contenedorización
- **Swagger/OpenAPI** - Documentación de API
- **Lombok** - Reducción de código boilerplate
- **Spring Actuator** - Monitoreo y métricas
- **Spring Mail** - Envío de correos electrónicos

## 🏗️ Arquitectura del Proyecto

```
src/main/java/com/utec/
├── config/           # Configuraciones de Spring
├── controller/       # Controladores REST
├── dto/             # Objetos de transferencia de datos
├── enums/           # Enumeraciones
├── jwt/             # Configuración JWT
├── mail/            # Servicios de correo
├── mapper/          # Mappers entre entidades y DTOs
├── model/           # Entidades JPA
├── repository/      # Repositorios de datos
├── service/         # Lógica de negocio
├── util/            # Utilidades
└── auditoria/       # Sistema de auditoría
```

**Diagrama de arquitectura**:
```text
+------------------------+
|  Capa de Presentación  |
|------------------------|
| - Controllers REST     |
| - DTOs                 |
| - Validaciones         |
+-----------+------------+
            |
            v
+------------------------+
|   Capa de Servicios    |
|------------------------|
| - Lógica de negocio    |
|       (services)       |
| - Transacciones        |
|                        |
+-----------+------------+
            |
            v
+------------------------+
| Capa de Persistencia   |
|------------------------|
| - Repositorios JPA     |
| - Entidades            |
| - Relaciones           |
+-----------+------------+
            |
            v
+------------------------+
|     Base de Datos      |
|------------------------|
| - Tablas relacionales  |
|                        |
+------------------------+
```
**Relaciones entre entidades**:

🔗 Herencia

- Usuario → Socio: Relación 1:1

- Usuario → Administrador: Relación 1:1

👤 Usuario

- Estado: Muchos usuarios → Un estado

- TipoDocumento: Muchos usuarios → Un tipo de documento

- Perfil: Muchos usuarios → Un perfil

- Teléfono: Un usuario → Muchos teléfonos

🧑‍🤝‍🧑 Socio

- Estado: Muchos socios → Un estado (estado_socio)

- Subcomisión: Muchos socios → Una subcomisión

- Categoría: Muchos socios → Una categoría

- SocioPagaCuota: Un socio → Muchos pagos de cuota

🎯 Actividad

- Estado: Muchas actividades → Un estado

- Usuario: Muchas actividades → Un usuario (creada_por)

- TipoActividad: Muchas actividades → Un tipo de actividad

- Espacio: Muchas actividades → Un espacio (opcional, se_realiza_en)

- ModoPago: Muchas actividades → Un modo de pago (opcional, forma_pago)

- UsuarioConcurreActividad: Una actividad → Muchas inscripciones

🗂️ TipoActividad

- Estado: Muchos tipos de actividad → Un estado

🧾 UsuarioConcurreActividad

- Usuario: Muchas inscripciones → Un usuario

- Actividad: Muchas inscripciones → Una actividad

- Estado: Muchas inscripciones → Un estado

📅 Reserva

- Usuario: Muchas reservas → Un usuario (reservada_por)

- Espacio: Muchas reservas → Un espacio

- Estado: Muchas reservas → Un estado

- ModoPago: Muchas reservas → Un modo de pago (forma_pago)

🏢 Espacio

- Estado: Muchos espacios → Un estado

💳 SocioPagaCuota

- Cuota: Muchos pagos → Una cuota

- ModoPago: Muchos pagos → Un modo de pago

🛡️ Perfil y Funcionalidad

- Perfil → Estado: Muchos perfiles → Un estado

- Perfil → PerfilAccedeFuncionalidad: Un perfil → Muchos accesos a funcionalidades

- Funcionalidad → Estado: Muchas funcionalidades → Un estado

- Funcionalidad → PerfilAccedeFuncionalidad: Una funcionalidad → Muchos perfiles que la acceden

- PerfilAccedeFuncionalidad → Estado: Muchas relaciones perfil-funcionalidad → Un estado

## 🔐 Autenticación y Seguridad
Se utiliza Spring Security con autenticación basada en JWT (JSON Web Tokens) para proteger el acceso a los endpoints de la API.

- Se utiliza JWT token en vez de las sesiones.
- La contraseña está cifrada para mayor seguridad.
- La autorización está asignada por los roles y estos se obtienen automáticamente del token de cada usuario.
- Hay endpoints que son accesibles para cualquier rol y otros que son restringidos.
#### Manejo de errores asociados:
- 401 Unauthorized: Usuario no autenticado
- 403 Forbidden: Usuario sin permisos suficientes


## 🚀 Instalación y Configuración

### Prerrequisitos

- Java 21 o superior
- Maven 3.8+
- Docker y Docker Compose
- Git

### Pasos de Instalación

1. **Clonar el Repositorio**
   ```bash
   git clone https://git.utec.edu.uy/4group/pdi.git
   cd pdi
   ```

2. **Levantar Base de Datos con Docker**
   ```bash
   docker-compose up -d
   ```

3. **Configurar Base de Datos**
   
   El archivo `application.properties` debe contener:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/utec
   spring.datasource.username=utecsuruser
   spring.datasource.password=utecsurpass
   spring.datasource.driver-class-name=org.postgresql.Driver
   
   spring.jpa.hibernate.ddl-auto=validate
   spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
   ```

4. **Creacion de la base de datos**
   - Ejecutar el archivo `estructura_sin_roles_sin_esquema.sql`

5. **Ejecutar la Aplicación**
   
   **Opción A - IntelliJ IDEA:**
   - Abrir el proyecto en IntelliJ
   - Ejecutar `PdiApplication.java`
   
   **Opción B - Línea de Comandos:**
   ```bash
   ./mvnw spring-boot:run
   ```

6. **Acceder a la Aplicación**
   - **API REST**: http://localhost:8080
   - **Swagger UI**: http://localhost:8080/swagger-ui.html
   - **Actuator**: http://localhost:8080/actuator

## 🔐 Credenciales de Prueba
(Es necesario ejecutar primero el .sql que se encuentra en resources)
### Usuario Administrador
- **Email**: admin@asur.com
- **Contraseña**: admin1234
- **Perfil**: Administrador

### Usuario 
- **Email**: valeria@asur.com
- **Contraseña**: user1234
- **Perfil**: Usuario

## 📚 Documentación de API

La documentación completa de la API está disponible a través de Swagger UI en:
- **URL**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

## 🧪 Pruebas

Para ejecutar las pruebas unitarias:
```bash
./mvnw test
```

## 📦 Entregas del Proyecto

### ✅ Entrega 1 – Setup del Proyecto y Primeros Endpoints
**📅 Fecha**: 7 de Junio

#### Checklist de Objetivos:

**Configuración Inicial:**
- [x] Crear repositorio en GitLab con estructura del proyecto
- [x] Inicializar Spring Boot
- [x] Configurar conexión con PostgreSQL

**Primeros Endpoints:**
- [x] RF001-01: Registro de usuarios
- [x] RF001-02: Listado de usuarios
- [x] Validaciones básicas (ej. nombre único, email válido)

**Arquitectura:**
- [x] Crear entidades base (Usuario, Perfil)
- [x] Implementar DTOs

**Documentación:**
- [x] Configurar Swagger o ReDoc

**Pruebas:**
- [x] Unitarias para creación y listado de usuarios (JUnit, Mockito)

**README.md:**
- [x] Descripción del proyecto, tecnologías y estructura
- [x] Incluir en la entrega:
  - [x] Enlace al repositorio GitLab
  - [x] Swagger/ReDoc
  - [x] Video demostrativo con endpoints funcionando
  - [x] README.md actualizado

### ✅ Entrega 2 – Seguridad, Perfiles y CRUDs Iniciales
**📅 Fecha**: 14 de Junio

#### Checklist de Objetivos:

**Seguridad:**
- [x] Spring Security + JWT
- [x] Endpoint de login
- [x] Restricción de acceso a endpoints por autenticación

**Roles y permisos:**
- [x] Definir roles (admin/user)
- [x] Control de acceso según rol

**CRUDs:**
- [x] Usuarios: crear, leer, actualizar, eliminar
- [x] Perfiles: idem
- [x] Validaciones (email único, etc.)

**Auditoría básica:**
- [x] Registro de cambios con JPA Auditing

**Pruebas:**
- [x] Login y JWT
- [x] Acceso según roles

**Documentación:**
- [x] Swagger/ReDoc actualizado

**Incluir en la entrega:**
- [x] Repositorio actualizado
- [x] Swagger/ReDoc con autenticación
- [x] Video demostrativo
- [x] README.md actualizado

**Comentarios sobre la entrega:**
- [x] El acceso según roles se controla en los endpoints desde Security Config con el role definido en el payload del token JWT.
- [x] El método de modificar Perfiles no tiene restricciones, por lo que el control de los filtros se traslada al frontend.

### ✅ Entrega 3 – Módulos Institucionales, Reportes y Pagos
**📅 Fecha**: 21 de Junio

#### Checklist de Objetivos:

**Módulos:**
- [x] Actividades (RF005)
- [x] Espacios (RF006)
- [x] Tipos de Actividad (RF007)
- [x] CRUD completo
- [x] Validar solapamientos de actividades

**Pagos:**
- [x] Registro y CRUD de pagos
- [x] Validaciones (ej. monto no negativo)

**Reportes:**
- [x] Consultas por fecha, usuario, actividad, espacio

**Auditoría avanzada:**
- [x] Ampliar auditoría a pagos y actividades

**Pruebas:**
- [x] Unitarias de nuevas funcionalidades

**Entregables:**
- [x] Repositorio actualizado
- [x] Swagger/ReDoc con nuevos endpoints
- [x] Video mostrando actividades, pagos y reportes
- [x] README.md actualizado

**Comentarios sobre la entrega:**
- [x] Para el módulo Espacios quedan disponibles listados para que el frontend pueda filtrar.
- [x] El ingreso de reservas contempla el modo de pago y la fecha de vencimiento de la seña (es calculada), pero los otros campos relacionados al pago se derivan al módulo de Pagos y al frontend.
- [x] Los métodos de modificar Tipo Actividad y Actividad no tienen restricción en todos los campos. Por lo que el filtro se traslada al frontend.

### ✅ Entrega Final – Últimos Detalles y Ajustes Finales
**📅 Fecha**: 28 de Junio

#### Checklist de Objetivos:

**Finalización del sistema:**
- [x] Todos los endpoints implementados
- [x] Validación de seguridad

**Documentación completa:**
- [x] README.md con instalación, arquitectura y detalles técnicos

**Despliegue:**
- [x] En servidor local o nube (opcional)
- [x] Instrucciones en el repositorio

**Video final:**
- [x] Demostración completa del sistema (máx. 30 min)

**Entregables:**
- [x] Repositorio final
- [x] Video con todo el sistema funcionando
- [x] README.md final
- [x] Despliegue mostrado y documentado

**Comentarios sobre la entrega:**
- [x] Para el módulo Funcionalidades y su relación con el acceso de Perfiles, quedan disponibles listados para que el frontend pueda filtrar y determinar el acceso.
- [x] La validación de la cédula Uruguaya utiliza la librería de Fabian Delgado y se puede encontrar en el siguiente enlace: https://github.com/fabdelgado/ciuy
- [x] Los listados de Usuarios, Perfiles, Actividades no aplican los filtros, los mismos los aplicará el frontend.

### ✅ Entrega etapa Examen - Mejoras y despliegue
**📅 Fecha**: 07 de agosto

**Comentarios sobre la entrega para esta instancia:**
- [x] Se agregan controles para mejorar la validación fechas en el módulo de actividades.
- [x] Se agregan controles de meses y datos en el módulo de pagos de cuota.
- [x] Se mejoran y agregan excepciones para devolver más información.
- [x] Se agregan controles en los listados para mejorar los response.
- [x] Se modifica dockerfile y se realiza el despliegue en railway desde un repositorio git.
- [x] Se agrega nuevo endpoint en el controlador de pagos de cuotas, para listar de los pagos de cuotas. Los datos de los pagos de actividades y reservas, se encuentran disponibles en los listados de esos controladores.


## 📊 Estado del Proyecto

- **Estado**: ✅ Completado
- **Última actualización**: Agosto 2025
- **Equipo**: Grupo 4 - UTEC



## Repositorio y despliegue:

- **Repositorio**: https://git.utec.edu.uy/4group/pdi
- **Deployment Railway**: group4-pdi-production.up.railway.app
- **Swagger servidor Railway**: https://group4-pdi-production.up.railway.app/swagger-ui/index.html
- **Swagger para ejecutar local**: http://localhost:8080/swagger-ui/index.html
---

**Desarrollado con ❤️ por el Grupo 4 de UTEC**