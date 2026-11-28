# CorpalivMobile – Plataforma de Impacto Social y Vinculación Empresarial

> DSY1105 Desarrollo de Aplicaciones Móviles · Evaluación Parcial 4 · DuocUC

- Repositorio de la app: <https://github.com/Vania-Bustos-Fuentealba/CorpalivMobile>
- Repositorio del backend: <https://github.com/benjamin-encina/corpaliv-backend>
- Historial anterior (oct–nov 2026, PR #1–#6): <https://github.com/Vania-Bustos-Fuentealba/CorpalivApp>

**Contenido:** [1. Nombre del proyecto](#1-nombre-del-proyecto) · [2. Integrantes](#2-integrantes) · [3. Funcionalidades](#3-funcionalidades) · [4. Endpoints utilizados](#4-endpoints-utilizados) · [5. Pasos para ejecutar](#5-pasos-para-ejecutar)

---

## 1. Nombre del proyecto

**CorpalivMobile** (paquete `com.duocuc.corpalivapp`): app Android en Kotlin y Jetpack Compose (Material 3, MVVM) que
centraliza las oportunidades de colaboración, los programas de RSE empresarial y las donaciones para la
**Corporación de Padres y Amigos por el Limitado Visual (CORPALIV)**, la **Escuela Especial Jean Van Dijk** y la red
sociolaboral **Alba Lab**. Se conecta a tres microservicios Spring Boot propios (`corpaliv-backend`) y a una API externa.

---

## 2. Integrantes

| Integrante | GitHub | Aporte principal |
|------------|--------|------------------|
| **Vania Bustos Fuentealba** | `Vania-Bustos-Fuentealba` | Proyecto base Android (tema, recursos, compilación), inicio adaptativo y modelos, listado de oportunidades, integración de la API externa, pruebas MockK |
| **Benjamín Encina** | `benjamin-encina` | Navegación y menú, registro, institución y modo especial, cuenta de administrador y Dashboard, login JWT y CRUD contra los microservicios, script de APK firmado y host configurable, pruebas unitarias, README; backend completo |

Aporte por pull request en este repositorio (cada funcionalidad en su rama, revisada y mergeada por la otra persona):

| PR | Rama | Autor | Aporte |
|----|------|-------|--------|
| #1 | `mobile-base` | Vania | Proyecto base Android con tema, recursos y configuración de compilación |
| #2 | `feature/inicio-modelos` | Vania | Pantalla de inicio adaptativa y modelos base |
| #3 | `feature/navegacion-menu` | Benjamín | Navegación, menú lateral y barras superior e inferior |
| #4 | `feature/registro-institucion` | Benjamín | Registro, institución, modo especial y almacenamiento local |
| #5 | `feature/cuenta-administrador` | Benjamín | Cuenta de administrador con perfil y Dashboard |
| #6 | `feature/oportunidades` | Vania | Listado de oportunidades con tarjeta y ViewModel |
| #7 | `feature/api-externa` | Vania | Cliente Retrofit, servicio, repositorio y pantalla de la API externa |
| #8 | `test/mockk` | Vania | Regla de corrutinas y pruebas MockK de API externa y sesión remota |
| #9 | `feature/login-microservicios` | Benjamín | Login y sesión JWT con `auth-service` |
| #10 | `feature/crud-oportunidades` | Benjamín | CRUD de oportunidades y aportes conectado a los microservicios |
| #11 | `build/script-apk` | Benjamín | Script de APK firmado, cobertura ampliada y host del backend configurable |
| #12 | `test/unitarias-app` | Benjamín | Pruebas de utilidades, modelos, clientes Retrofit, repositorios, navegación y ViewModels |
| #13–#14 | `docs/readme`, `docs/readme-v2` | Benjamín | README de entrega |

> ⏳ **Pendiente del equipo** (solo ustedes pueden completarlo):
> - Tablero de Trello (RNF-10): `PENDIENTE: pegar aquí la URL pública del tablero` (Vania).
> - Capturas de la app, del APK firmado y del `.jks` (solo su ubicación) en `docs/capturas/` (Vania).
> - Agregar a la tabla los PR que falten cuando se mergeen (capturas/Trello, release `v1.0`).

---

## 3. Funcionalidades

| Funcionalidad | Pantalla | Detalle |
|---------------|----------|---------|
| Inicio institucional | `home_page` | Información de Corpaliv en bloques, diseño adaptativo (Compact, Medium, Expanded), menú lateral y barra inferior en todas las pantallas |
| Quiénes somos | `institucion_page` | Escuela Jean Van Dijk y Alba Lab, leídos desde archivos `.md` en `assets/institucion/` |
| Oportunidades (RSE) | `oportunidades_page` | Listado desde `oportunidades-service` |
| Gestión de oportunidades (administrador) | `OportunidadFormScreen` | Crear, editar y eliminar oportunidades con el token JWT del administrador |
| Formulario de aporte o postulación | `registro_page` → `resumen_page` | Selector Persona / Empresa, validación en tiempo real (RUN/RUT por formato, correo, teléfono), frecuencia de donación, barra de carga y agradecimiento de 5 segundos. Se envía a `aportes-service` |
| Inicio de sesión | `LoginScreen` | Login contra `auth-service`, token JWT en memoria y rol de la cuenta |
| Perfil | `perfil_page` | Datos de la cuenta, badge de rol, foto con **cámara o galería** (`FileProvider`, permiso `CAMERA`) y cierre de sesión |
| Dashboard de administrador | `dashboard_page` | Aportes y postulaciones: listar, marcar como contactado, eliminar y «Contactar WhatsApp». Solo rol `ADMINISTRADOR` |
| API externa | `ExternalPostScreen` (menú «API externa») | Publicaciones de JSONPlaceholder con estados de carga, vacío, error, reintento y actualización |
| Modo especial | `ModoEspecialScreen` | Interruptor persistente con DataStore y animaciones de estado |
| Host del backend configurable | `BuildConfig.BACKEND_HOST` | `10.0.2.2` en el emulador (por defecto) o la IP del PC para un celular físico |
| Pruebas y cobertura | — | JUnit, MockK y `kotlinx-coroutines-test`; JaCoCo exige ≥ 80 % (27-11-2026: 139 pruebas, 94 % de instrucciones, 96,7 % de líneas) |

Tecnologías: Kotlin, Jetpack Compose + Material 3, MVVM con `StateFlow`, Retrofit 2 + Gson, Coil, Coroutines y DataStore.

---

## 4. Endpoints utilizados

### 4.1 API externa

| API | URL base | Endpoint | Cliente en la app | Uso |
|-----|----------|----------|-------------------|-----|
| JSONPlaceholder | `https://jsonplaceholder.typicode.com/` | `GET /posts` | `ExternalApiService` + `ExternalApiClient` (Retrofit propio, **sin** token JWT) | Pantalla `ExternalPostScreen` |

### 4.2 Microservicios (`corpaliv-backend`)

La app arma la URL como `http://<BACKEND_HOST>:<puerto>/api/` en `RetrofitClient` (`BACKEND_HOST` = `10.0.2.2` en el emulador).

| Servicio · puerto | Método | Ruta | Acceso | Uso en la app |
|-------------------|--------|------|--------|---------------|
| auth-service · **8083** | POST | `/api/auth/login` | Público | Iniciar sesión (`{correo, clave}` → token y sesión) |
| oportunidades-service · **8081** | GET | `/api/oportunidades` | Público | Listado de oportunidades |
| | POST | `/api/oportunidades` | Admin | Crear oportunidad |
| | PUT | `/api/oportunidades/{id}` | Admin | Editar oportunidad |
| | DELETE | `/api/oportunidades/{id}` | Admin | Eliminar oportunidad (`204`) |
| aportes-service · **8082** | POST | `/api/aportes` | Público | Enviar aporte (persona o empresa) o postulación |
| | GET | `/api/aportes` | Admin | Listado del Dashboard |
| | PATCH | `/api/aportes/{id}/estado` | Admin | Marcar `CONTACTADO` / `PENDIENTE` |
| | DELETE | `/api/aportes/{id}` | Admin | Eliminar registro (`204`) |

Las rutas de administrador exigen `Authorization: Bearer <token>`: sin token responden `401` y con otro rol `403`.
La lista completa de endpoints del backend está en el README de `corpaliv-backend`.

---

## 5. Pasos para ejecutar

### 5.1 Requisitos

| Para | Necesitas |
|------|-----------|
| App | Android Studio (SDK 35, `minSdk 24`). Gradle 9.6.0 y AGP 9.3.0 vienen con el wrapper; el JDK de la app lo resuelve el toolchain de Gradle |
| Backend | **JDK 21** (no 24) y **Maven 3.9+**. Docker Desktop solo si usas MySQL |

### 5.2 Levantar el backend

```powershell
git clone https://github.com/benjamin-encina/corpaliv-backend.git
```

Sigue la sección 5 del README de `corpaliv-backend` (instalación de JDK 21 y Maven en Windows incluida). Resumen,
con **H2 por defecto** (no hay que instalar base de datos), en **tres terminales** desde la raíz del repositorio:

```powershell
cd auth-service;          mvn spring-boot:run   # terminal 1, puerto 8083
cd oportunidades-service; mvn spring-boot:run   # terminal 2, puerto 8081
cd aportes-service;       mvn spring-boot:run   # terminal 3, puerto 8082
```

Comprueba que responde: `curl.exe http://localhost:8081/api/oportunidades`.

### 5.3 Ejecutar la app

1. Clonar y abrir en Android Studio:
   ```powershell
   git clone https://github.com/Vania-Bustos-Fuentealba/CorpalivMobile.git
   ```
   *File › Open* › carpeta `CorpalivMobile`, y esperar el *Gradle Sync* (crea `local.properties` con `sdk.dir`, ignorado por Git).
2. Iniciar un **emulador** (Device Manager). La app apunta por defecto a `10.0.2.2`, que en el emulador es el
   `localhost` del PC, así que no hay que configurar nada.
3. ▶ **Run 'app'**, o en PowerShell `.\gradlew.bat :app:assembleDebug` (APK en `app\build\outputs\apk\debug\`).
4. Iniciar sesión con la cuenta de demostración: **`admin@corpaliv.cl` / `admin`** (solo entorno académico).
5. Pruebas y cobertura (no necesitan backend ni emulador):
   ```powershell
   .\gradlew.bat :app:testDebugUnitTest :app:jacocoTestReport
   .\gradlew.bat :app:jacocoTestCoverageVerification   # falla si baja de 80 %
   ```
   Informe: `app\build\reports\jacoco\jacocoTestReport\html\index.html`.

### 5.4 Celular físico (opcional)

PC y teléfono en la misma red Wi-Fi. Compila con la IP del PC (`ipconfig`), sin `http://` ni puerto, y permite los
puertos 8081–8083 en el firewall de Windows:

```powershell
.\gradlew.bat :app:assembleDebug -Pcorpaliv.backendHost=192.168.1.50
```

O deja `corpaliv.backendHost=192.168.1.50` en `local.properties`. Tras cambiar el host hay que volver a compilar.

### 5.5 APK firmado (release)

*Build › Generate Signed App Bundle or APK… › APK*, crear un `.jks` propio **fuera del repositorio** y elegir
**release**. Alternativa por consola: `.\scripts\generar-apk-firmado.ps1`, que pide las contraseñas por teclado.
El `.jks`, `keystore.properties` y el APK están en `.gitignore`: no se suben al repositorio ni se escriben aquí sus contraseñas.
Si al instalar aparece `INSTALL_FAILED_UPDATE_INCOMPATIBLE`, quitar antes la versión debug: `adb uninstall com.duocuc.corpalivapp`.

### 5.6 Problemas frecuentes

| Síntoma | Solución |
|---------|----------|
| La app muestra error al cargar oportunidades o al iniciar sesión | Los 3 servicios deben estar encendidos; en el emulador el host es `10.0.2.2`, no `localhost` |
| Error de Maven o Java al levantar el backend | Ver «Problemas frecuentes» en el README de `corpaliv-backend` (JDK 21, `JAVA_HOME`, Maven) |
| Celular físico no conecta | Misma Wi-Fi, IP correcta en `corpaliv.backendHost` y puertos 8081–8083 abiertos en el firewall |
