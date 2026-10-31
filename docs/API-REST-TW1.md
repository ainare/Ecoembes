# ECOEMBES — Especificación de la API REST
**Asignatura:** Software Design  
**Entrega:** Teamwork 1 (TW1)  
**Proyecto:** Ecoembes — Packaging Recycling Management System  
**Versión:** 1.0

---

# 1. Introducción

El objetivo de esta API REST es permitir que los empleados de Ecoembes interactúen con el servidor central para gestionar los contenedores de reciclaje, consultar su estado, consultar la capacidad de las plantas de reciclaje y asignar contenedores a dichas plantas.

La comunicación entre el cliente y el servidor se realizará mediante peticiones HTTP, utilizando JSON para intercambiar información.

La API implementará un mecanismo de autenticación mediante tokens para garantizar que los empleados hayan iniciado sesión antes de utilizar las funcionalidades del sistema.

# 2. Convenciones generales

## 2.1. Formato de comunicación

- **Protocolo:** HTTP.
- **Formato de intercambio:** JSON.
- **Codificación:** UTF-8.
- **Formato de fechas:** `YYYY-MM-DD`.
- **Servidor local:** `http://localhost:8080`.

Las peticiones que envíen información al servidor utilizarán el encabezado:

```http
Content-Type: application/json
```

## 2.2. Autenticación

Excepto la operación de inicio de sesión (`POST /login`), todas las operaciones requieren un token de autenticación válido.

El token se enviará mediante la cabecera HTTP:

```http
Authorization: Bearer <token>
```

El servidor comprobará que el token existe y corresponde a una sesión activa.

El token se generará utilizando el timestamp del momento en el que se procesa el inicio de sesión en el servidor. Para ello, se utilizará el tiempo actual en milisegundos.

El servidor almacenará en memoria los tokens activos y los datos de los empleados asociados a cada sesión.

Cuando un empleado cierre sesión, el servidor eliminará el token y la información asociada.

## 2.3. Códigos HTTP generales

| Código | Descripción |
|---|---|
| 200 OK | La petición se ha procesado correctamente. |
| 201 Created | Se ha creado un nuevo recurso. |
| 204 No Content | La operación se ha realizado correctamente y no devuelve contenido. |
| 400 Bad Request | La petición contiene datos incorrectos o incompletos. |
| 401 Unauthorized | El empleado no está autenticado o el token no es válido. |
| 404 Not Found | El recurso solicitado no existe. |
| 409 Conflict | La operación genera un conflicto con el estado actual del sistema. |
| 500 Internal Server Error | Se ha producido un error interno en el servidor. |

Las respuestas de error tendrán un formato común:

```json
{
  "timestamp": "2026-11-09T09:30:00",
  "status": 401,
  "error": "Unauthorized",
  "code": "INVALID_TOKEN",
  "message": "The authentication token is invalid."
}
```

# 3. Resumen de operaciones de la API

La API estará formada inicialmente por las siguientes ocho operaciones:

| Nº | Método HTTP | Endpoint | Funcionalidad | Respuesta correcta |
|---|---|---|---|---|
| 1 | POST | `/login` | Iniciar sesión | 200 OK |
| 2 | POST | `/logout` | Cerrar sesión | 204 No Content |
| 3 | POST | `/dumpsters` | Crear un contenedor | 201 Created |
| 4 | PUT | `/dumpsters/{id}` | Actualizar información de un contenedor | 200 OK |
| 5 | GET | `/dumpsters/{id}/usage` | Consultar el uso de un contenedor entre dos fechas | 200 OK |
| 6 | GET | `/dumpsters/status` | Consultar el estado de los contenedores de un código postal en una fecha | 200 OK |
| 7 | GET | `/plants/{id}/capacity` | Consultar la capacidad disponible de una planta | 200 OK |
| 8 | POST | `/assignments` | Asignar contenedores a una planta de reciclaje | 201 Created |

# 4. Descripción detallada de las operaciones

## 4.1. Inicio de sesión (Login)

**Método HTTP:** POST

**Endpoint:** `/login`

**Descripción:**

Permite que un empleado acceda al sistema mediante su correo electrónico y contraseña.

Si las credenciales son correctas, el servidor genera un token basado en el timestamp actual y lo almacena junto con los datos del empleado para identificar su sesión.

**Parámetros de entrada:**

Se envían en el cuerpo de la petición mediante JSON.

| Parámetro | Tipo | Obligatorio | Descripción |
|---|---|---|---|
| email | String | Sí | Dirección de correo electrónico del empleado. |
| password | String | Sí | Contraseña del empleado. |

**Ejemplo de petición:**

```http
POST /login
```

```json
{
  "email": "empleado@ecoembes.com",
  "password": "123456"
}
```

**Respuesta correcta: 200 OK**

```json
{
  "token": "1793456789123"
}
```

El token será generado por el servidor y devuelto al cliente.

**Posibles errores:**

| Código | Situación |
|---|---|
| 400 | El correo o la contraseña no se han proporcionado correctamente. |
| 401 | El correo electrónico o la contraseña son incorrectos. |
| 500 | Error interno durante el inicio de sesión. |

---

## 4.2. Cierre de sesión (Logout)

**Método HTTP:** POST

**Endpoint:** `/logout`

**Descripción:**

Permite que un empleado cierre su sesión.

El servidor identifica al empleado mediante el token recibido y elimina el token y toda la información de sesión asociada.

A partir de ese momento, el empleado no podrá utilizar ninguna funcionalidad protegida hasta volver a iniciar sesión.

**Parámetros de entrada:**

El token se envía mediante la cabecera HTTP.

```http
Authorization: Bearer 1793456789123
```

No necesita cuerpo JSON.

**Respuesta correcta: 204 No Content**

No devuelve contenido en el cuerpo de la respuesta.

**Posibles errores:**

| Código | Situación |
|---|---|
| 401 | No se ha enviado un token válido o la sesión ya no existe. |
| 500 | Error interno durante el cierre de sesión. |

---

## 4.3. Crear un contenedor

**Método HTTP:** POST

**Endpoint:** `/dumpsters`

**Descripción:**

Permite que un empleado registre un nuevo contenedor en el sistema.

El nuevo contenedor tendrá un identificador único, una dirección, un código postal y una capacidad inicial.

**Parámetros de entrada:**

Se envían en el cuerpo de la petición mediante JSON.

| Parámetro | Tipo | Obligatorio | Descripción |
|---|---|---|---|
| id | String | Sí | Identificador único del contenedor. |
| address | String | Sí | Dirección donde está situado el contenedor. |
| postalCode | String | Sí | Código postal de su ubicación. |
| initialCapacity | Integer | Sí | Capacidad inicial del contenedor. |

**Ejemplo de petición:**

```http
POST /dumpsters
```

```json
{
  "id": "D-101",
  "address": "Calle Mayor 12, Durango",
  "postalCode": "48200",
  "initialCapacity": 100
}
```

**Respuesta correcta: 201 Created**

```json
{
  "id": "D-101",
  "address": "Calle Mayor 12, Durango",
  "postalCode": "48200",
  "initialCapacity": 100
}
```

**Posibles errores:**

| Código | Situación |
|---|---|
| 400 | Faltan datos obligatorios o alguno tiene un formato incorrecto. |
| 401 | El empleado no está autenticado. |
| 409 | Ya existe un contenedor con ese identificador. |
| 500 | Error interno al crear el contenedor. |

---

## 4.4. Actualizar la información de un contenedor

**Método HTTP:** PUT

**Endpoint:** `/dumpsters/{id}`

**Descripción:**

Permite actualizar la información del estado de un contenedor a partir de los datos transmitidos por su sensor.

Cada contenedor transmite diariamente una estimación del número de envases que contiene y su nivel de llenado.

El sistema almacena la información asociada a su fecha correspondiente para permitir posteriormente la consulta del uso histórico.

**Parámetros de entrada:**

Parámetro de ruta:

| Parámetro | Tipo | Obligatorio | Descripción |
|---|---|---|---|
| id | String | Sí | Identificador del contenedor que se quiere actualizar. |

Cuerpo de la petición:

| Parámetro | Tipo | Obligatorio | Descripción |
|---|---|---|---|
| date | LocalDate | Sí | Fecha de la lectura del sensor. |
| estimatedContainers | Integer | Sí | Número estimado de envases que contiene el contenedor. |
| fillLevel | String (enum) | Sí | Nivel de llenado del contenedor. |

Los valores permitidos para `fillLevel` son:

- `GREEN`: el contenedor tiene espacio disponible.
- `ORANGE`: el contenedor tiene espacio limitado.
- `RED`: el contenedor está lleno.

**Ejemplo de petición:**

```http
PUT /dumpsters/D-101
```

```json
{
  "date": "2026-11-09",
  "estimatedContainers": 75,
  "fillLevel": "ORANGE"
}
```

**Respuesta correcta: 200 OK**

```json
{
  "id": "D-101",
  "date": "2026-11-09",
  "estimatedContainers": 75,
  "fillLevel": "ORANGE"
}
```

**Posibles errores:**

| Código | Situación |
|---|---|
| 400 | Datos incorrectos o nivel de llenado no permitido. |
| 401 | El empleado no está autenticado. |
| 404 | No existe el contenedor indicado. |
| 500 | Error interno al actualizar la información. |

---

## 4.5. Consultar el uso de un contenedor

**Método HTTP:** GET

**Endpoint:** `/dumpsters/{id}/usage`

**Descripción:**

Permite consultar el historial de uso de un contenedor durante un periodo de tiempo determinado.

La información devuelta permitirá conocer cómo ha evolucionado el número de envases y el nivel de llenado del contenedor.

**Parámetros de entrada:**

Parámetro de ruta:

| Parámetro | Tipo | Obligatorio | Descripción |
|---|---|---|---|
| id | String | Sí | Identificador del contenedor. |

Parámetros de consulta (Query Parameters):

| Parámetro | Tipo | Obligatorio | Descripción |
|---|---|---|---|
| startDate | LocalDate | Sí | Fecha inicial del periodo. |
| endDate | LocalDate | Sí | Fecha final del periodo. |

**Ejemplo de petición:**

```http
GET /dumpsters/D-101/usage?startDate=2026-11-01&endDate=2026-11-09
```

**Respuesta correcta: 200 OK**

```json
{
  "dumpsterId": "D-101",
  "startDate": "2026-11-01",
  "endDate": "2026-11-09",
  "readings": [
    {
      "date": "2026-11-01",
      "estimatedContainers": 40,
      "fillLevel": "GREEN"
    },
    {
      "date": "2026-11-05",
      "estimatedContainers": 60,
      "fillLevel": "ORANGE"
    },
    {
      "date": "2026-11-09",
      "estimatedContainers": 75,
      "fillLevel": "ORANGE"
    }
  ]
}
```

La lista de lecturas contendrá los registros disponibles dentro del periodo solicitado, ordenados por fecha.

Si el contenedor existe, pero no tiene lecturas en ese periodo, se devolverá una lista vacía.

**Posibles errores:**

| Código | Situación |
|---|---|
| 400 | Las fechas no son válidas o la fecha inicial es posterior a la final. |
| 401 | El empleado no está autenticado. |
| 404 | No existe el contenedor indicado. |
| 500 | Error interno al consultar el historial. |

---

## 4.6. Consultar el estado de los contenedores por código postal

**Método HTTP:** GET

**Endpoint:** `/dumpsters/status`

**Descripción:**

Permite obtener el estado de todos los contenedores situados en una determinada zona, identificada mediante su código postal, en una fecha concreta.

La información permitirá conocer los contenedores que necesitan atención según su nivel de llenado.

**Parámetros de entrada:**

Parámetros de consulta (Query Parameters):

| Parámetro | Tipo | Obligatorio | Descripción |
|---|---|---|---|
| postalCode | String | Sí | Código postal de la zona que se quiere consultar. |
| date | LocalDate | Sí | Fecha para la que se desea consultar el estado. |

**Ejemplo de petición:**

```http
GET /dumpsters/status?postalCode=48200&date=2026-11-09
```

**Respuesta correcta: 200 OK**

```json
{
  "postalCode": "48200",
  "date": "2026-11-09",
  "dumpsters": [
    {
      "id": "D-101",
      "address": "Calle Mayor 12, Durango",
      "estimatedContainers": 75,
      "fillLevel": "ORANGE"
    },
    {
      "id": "D-102",
      "address": "Calle San Pedro 5, Durango",
      "estimatedContainers": 30,
      "fillLevel": "GREEN"
    },
    {
      "id": "D-103",
      "address": "Calle Ezkurdi 8, Durango",
      "estimatedContainers": 100,
      "fillLevel": "RED"
    }
  ]
}
```

Si no existen contenedores con lecturas disponibles para esa zona y fecha, se devolverá una lista vacía.

**Posibles errores:**

| Código | Situación |
|---|---|
| 400 | Faltan parámetros o la fecha tiene un formato incorrecto. |
| 401 | El empleado no está autenticado. |
| 500 | Error interno al consultar los contenedores. |

---

## 4.7. Consultar la capacidad de una planta de reciclaje

**Método HTTP:** GET

**Endpoint:** `/plants/{id}/capacity`

**Descripción:**

Permite consultar la capacidad disponible de una planta de reciclaje para una fecha determinada.

Las capacidades de las plantas se expresan en toneladas.

**Parámetros de entrada:**

Parámetro de ruta:

| Parámetro | Tipo | Obligatorio | Descripción |
|---|---|---|---|
| id | String | Sí | Identificador de la planta de reciclaje. |

Parámetro de consulta:

| Parámetro | Tipo | Obligatorio | Descripción |
|---|---|---|---|
| date | LocalDate | Sí | Fecha para la que se consulta la capacidad. |

**Ejemplo de petición:**

```http
GET /plants/PL-01/capacity?date=2026-11-09
```

**Respuesta correcta: 200 OK**

```json
{
  "plantId": "PL-01",
  "date": "2026-11-09",
  "availableCapacityTons": 25.5
}
```

El campo `availableCapacityTons` representa la capacidad disponible expresada en toneladas para la fecha solicitada.

**Posibles errores:**

| Código | Situación |
|---|---|
| 400 | La fecha no tiene un formato válido. |
| 401 | El empleado no está autenticado. |
| 404 | No existe la planta o no hay información de capacidad disponible para esa fecha. |
| 500 | Error interno al consultar la capacidad. |

---

## 4.8. Asignar contenedores a una planta de reciclaje

**Método HTTP:** POST

**Endpoint:** `/assignments`

**Descripción:**

Permite asignar uno o varios contenedores a una planta de reciclaje para su procesamiento.

El sistema comprobará la información de los contenedores seleccionados y la capacidad disponible de la planta para la fecha indicada.

También registrará la identidad del empleado que realiza la asignación para garantizar la trazabilidad de la operación.

Una vez realizada la asignación, la planta debe recibir una notificación con el número de contenedores y el número total de envases que va a recibir.

**Parámetros de entrada:**

Se envían en el cuerpo de la petición mediante JSON.

| Parámetro | Tipo | Obligatorio | Descripción |
|---|---|---|---|
| plantId | String | Sí | Identificador de la planta de reciclaje seleccionada. |
| date | LocalDate | Sí | Fecha prevista para la asignación. |
| dumpsterIds | Array of String | Sí | Lista de identificadores de los contenedores que se quieren asignar. |

**Ejemplo de petición:**

```http
POST /assignments
```

```json
{
  "plantId": "PL-01",
  "date": "2026-11-09",
  "dumpsterIds": [
    "D-101",
    "D-102",
    "D-103"
  ]
}
```

El empleado responsable no se enviará en el cuerpo de la petición. Su identidad se obtendrá a partir del token de autenticación.

**Respuesta correcta: 201 Created**

```json
{
  "assignmentId": "AS-001",
  "plantId": "PL-01",
  "date": "2026-11-09",
  "dumpsterIds": [
    "D-101",
    "D-102",
    "D-103"
  ],
  "totalDumpsters": 3,
  "totalPackagingContainers": 205,
  "employeeId": "EMP-001"
}
```

El total de envases se calculará sumando las estimaciones de los contenedores seleccionados para la fecha de la asignación.

**Posibles errores:**

| Código | Situación |
|---|---|
| 400 | La petición contiene datos incorrectos o no se han seleccionado contenedores. |
| 401 | El empleado no está autenticado. |
| 404 | No existe la planta o alguno de los contenedores seleccionados. |
| 409 | La asignación no puede realizarse por falta de capacidad disponible. |
| 500 | Error interno durante la asignación. |

---

# 5. Consideraciones generales de implementación

## 5.1. Gestión de sesiones

El servidor mantendrá una estructura en memoria que relacionará cada token activo con los datos del empleado correspondiente.

El funcionamiento será el siguiente:

1. El empleado realiza una petición de login.
2. El servidor valida sus credenciales.
3. Se genera un token utilizando el timestamp del servidor.
4. Se almacena la relación entre el token y el empleado.
5. Las siguientes peticiones incluyen el token en la cabecera `Authorization`.
6. El servidor comprueba que el token existe antes de ejecutar la operación solicitada.
7. Cuando el empleado realiza logout, se elimina su sesión.

## 5.2. Gestión de datos en TW1

Para la primera versión del prototipo, se podrán utilizar datos simulados en memoria para validar el funcionamiento de las operaciones.

La persistencia definitiva de la información se incorporará en las siguientes versiones del proyecto.

## 5.3. Historial de lecturas de los contenedores

Las actualizaciones diarias de los sensores deben conservar la fecha de cada lectura.

No se debe mantener únicamente el último estado del contenedor, porque las operaciones de consulta necesitan recuperar información de periodos anteriores.

Para un mismo contenedor y una misma fecha se mantendrá una única lectura diaria, sustituyéndola si vuelve a recibirse una actualización de esa fecha.

## 5.4. Notificaciones a las plantas

La asignación de contenedores debe generar una notificación con el número de contenedores y de envases que recibirá la planta.

En TW1 se puede simular este comportamiento. La comunicación e integración con los servidores externos de las plantas se desarrollará en una versión posterior del prototipo.

# 6. Resumen de autenticación y respuestas

Las siguientes operaciones requieren una sesión activa:

| Operación | Requiere token |
|---|---|
| POST `/login` | No |
| POST `/logout` | Sí |
| POST `/dumpsters` | Sí |
| PUT `/dumpsters/{id}` | Sí |
| GET `/dumpsters/{id}/usage` | Sí |
| GET `/dumpsters/status` | Sí |
| GET `/plants/{id}/capacity` | Sí |
| POST `/assignments` | Sí |

Todas las operaciones protegidas devolverán `401 Unauthorized` cuando no se proporcione un token válido.

# 7. Decisiones de diseño y aspectos pendientes de concretar

La descripción del proyecto no define todos los detalles técnicos necesarios para implementar la API. Se establecen las siguientes decisiones o cuestiones:

1. **Capacidad inicial de los contenedores:** en esta propuesta, `initialCapacity` representa la capacidad máxima del contenedor expresada en número de envases. Debe mantenerse separada de `estimatedContainers`, que representa los envases que contiene actualmente.

2. **Conversión a toneladas:** el enunciado indica que la capacidad de las plantas se mide en toneladas, mientras que los sensores proporcionan un número estimado de envases. No especifica una fórmula de conversión. Será necesario definir cómo obtener el peso estimado de los envases antes de implementar una comprobación real de capacidad en las asignaciones.

3. **Credenciales de empleados:** para TW1 se podrán utilizar empleados y credenciales simulados, dado que la persistencia se incorporará posteriormente.

4. **Identificadores:** se propone utilizar identificadores de tipo String para contenedores y plantas, lo que permite utilizar códigos como `D-101` o `PL-01`.

5. **Integración con plantas:** la notificación se simulará inicialmente, dejando la comunicación real con los servidores externos para el siguiente prototipo.

---

