# 💙 CORPALIV - Plataforma de Impacto Social y Vinculación Empresarial

[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-blue.svg)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-green.svg)](https://developer.android.com/jetpack/compose)
[![Architecture](https://img.shields.io/badge/Architecture-MVVM-orange.svg)](https://developer.android.com/topic/architecture)

> DSY1105 Desarrollo de Aplicaciones Móviles · Evaluación Parcial 4 · DuocUC

Aplicación móvil orientada a centralizar y visibilizar las oportunidades de colaboración, programas de RSE empresarial y donaciones para la **Corporación de Padres y Amigos por el Limitado Visual (CORPALIV)**, la **Escuela Especial Jean Van Dijk** y la red sociolaboral **Alba Lab**.

Repositorio de la app: <https://github.com/Vania-Bustos-Fuentealba/CorpalivMobile>
Repositorio del backend: <https://github.com/benjamin-encina/corpaliv-backend>
Historial de desarrollo anterior (oct–nov 2026, PR #1–#6): <https://github.com/Vania-Bustos-Fuentealba/CorpalivApp>

**Contenido:** [1. Nombre](#1-nombre-del-proyecto) · [2. Integrantes y aportes](#2-integrantes-y-aportes) · [3. Funcionalidades](#3-funcionalidades) · [4. Endpoints](#4-endpoints-utilizados) · [5. Pasos para ejecutar](#5-pasos-para-ejecutar) · [6. APK firmado y .jks](#6-captura-del-apk-firmado-y-del-jks) · [7. Detalle técnico](#7-detalle-técnico)

---

## 1. Nombre del proyecto

**CorpalivMobile** (paquete `com.duocuc.corpalivapp`) – Plataforma de Impacto Social y Vinculación Empresarial (app Android en Kotlin + Jetpack Compose, conectada a 3 microservicios Spring Boot y a una API externa).

* **Desafío:** La difusión de proyectos y alternativas de vinculación institucional se encontraba dispersa en múltiples canales (reuniones, presentaciones, redes sociales), lo que dificultaba que potenciales empresas aliadas y donantes comprendieran rápidamente el impacto social y las opciones de colaboración.
* **Solución:** Una plataforma móvil centralizada que unifica la propuesta de valor de la organización, destacando el **impacto social directo** de cada iniciativa y facilitando la vinculación directa con las distintas áreas.

---

## 2. Integrantes y aportes

| Integrante | Cuenta de GitHub | Responsabilidad principal |
|------------|------------------|---------------------------|
| **Vania Bustos Fuentealba** | `Vania-Bustos-Fuentealba` | Base del proyecto Android, tema y recursos; inicio adaptativo; listado de oportunidades; integración de la API externa; pruebas MockK; configuración de compilación release y JaCoCo |
| **Benjamín Encina** | `benjamin-encina` | Navegación y menú; registro, institución y modo especial; cuenta de administrador y Dashboard; login JWT y CRUD contra los microservicios; pruebas unitarias; script de APK firmado; backend completo (`corpaliv-backend`) |

### 2.1 Aportes en este repositorio (pull requests)

Cada funcionalidad entró en su propia rama y pull request. Los datos salen del historial de `main`.

| PR | Rama | Autor | Aporte |
|----|------|-------|--------|
| #1 | `mobile-base` | Vania | Proyecto base Android con tema, recursos y configuración de compilación |
| #2 | `feature/inicio-modelos` | Vania | Pantalla de inicio adaptativa y modelos base |
| #3 | `feature/navegacion-menu` | Benjamín | Navegación, menú lateral y barras superior e inferior |
| #4 | `feature/registro-institucion` | Benjamín | Flujo de registro, institución, modo especial y almacenamiento local |
| #5 | `feature/cuenta-administrador` | Benjamín | Cuenta de administrador con perfil y Dashboard |
| #6 | `feature/oportunidades` | Vania | Listado de oportunidades con tarjeta y ViewModel |
| #7 | `feature/api-externa` | Vania | Cliente Retrofit, servicio, repositorio y pantalla de la API externa |
| #8 | `test/mockk` | Vania | Regla de corrutinas y pruebas MockK de API externa y sesión remota |
| #9 | `feature/login-microservicios` | Benjamín | Login y sesión JWT con `auth-service` |
| #10 | `feature/crud-oportunidades` | Benjamín | CRUD de oportunidades y aportes conectado a los microservicios |
| #11 | `build/script-apk` | Benjamín | Script de APK firmado, cobertura ampliada y host del backend configurable |
| #12 | `test/unitarias-app` | Benjamín | Pruebas de utilidades, modelos, clientes Retrofit, repositorios, navegación y ViewModels |
| #13 | `docs/readme` | Benjamín | README de entrega con arquitectura, ejecución y pruebas |
| #14 | `docs/readme-v2` | Benjamín | Este README: integrantes, aportes y enlaces a los repositorios |

Pendientes del equipo (se agregan a esta tabla cuando se mergeen):

| Pendiente | Responsable |
|-----------|-------------|
| Capturas en `docs/capturas/` (sección 6.2) | Vania |
| Enlace público del tablero de Trello (abajo) | Vania |
| Release `v1.0` con el APK firmado adjunto | Vania |
| Backend: pruebas de `aportes-service`, variables de entorno y README probado (en `corpaliv-backend`) | Vania |

### 2.2 Backend

`corpaliv-backend` (3 microservicios Spring Boot) lo desarrolló Benjamín Encina: estructura de los microservicios,
pruebas de base de datos y versión 1 con pruebas. Ver su README para el detalle.

### 2.3 Planificación

Tablero de Trello (RNF-10): `PENDIENTE: pegar aquí la URL pública del tablero`. Cada tarjeta corresponde a un PR de la tabla 2.1.

---

## 3. Funcionalidades

| Funcionalidad | Pantalla / ruta | Detalle |
|---------------|-----------------|---------|
| Inicio institucional | `home_page` | Información de Corpaliv en bloques, diseño adaptativo (Compact, Medium, Expanded) con menú lateral y barra inferior en todas las pantallas |
| Quiénes somos | `institucion_page` | Escuela Jan Van Dijk y Alba Lab, leídos desde archivos `.md` en `assets/institucion/` |
| Oportunidades (RSE) | `oportunidades_page` | Listado desde `oportunidades-service` (`GET /api/oportunidades`) |
| Gestión de oportunidades (administrador) | `OportunidadFormScreen` | Crear, editar y eliminar oportunidades contra `oportunidades-service` con el token JWT del administrador |
| Formulario de aporte o postulación | `registro_page` → `resumen_page` | Selector Persona / Empresa, validación en tiempo real (RUN/RUT por formato, correo, teléfono), frecuencia de donación, barra de carga, agradecimiento de 5 segundos y retorno al inicio. Se envía a `aportes-service` (`POST /api/aportes`) |
| Inicio de sesión | `LoginScreen` | Login real contra `auth-service` (`POST /api/auth/login`), token JWT en memoria y rol de la cuenta |
| Perfil | `perfil_page` | Información de la cuenta, badge de rol, foto con **cámara o galería** (`FileProvider`, permiso `CAMERA`) y cierre de sesión |
| Dashboard de administrador | `dashboard_page` | Aportes y postulaciones desde `aportes-service`: listar, marcar como contactado, eliminar y botón «Contactar WhatsApp». Solo rol `ADMINISTRADOR` |
| API externa | `ExternalPostScreen` (menú lateral «API externa») | Publicaciones de JSONPlaceholder con estados de carga, vacío, error, reintento y actualización |
| Modo especial | `ModoEspecialScreen` | Interruptor persistente con DataStore y animaciones de estado |
| Host del backend configurable | `BuildConfig.BACKEND_HOST` | Emulador (`10.0.2.2`, por defecto) o IP del PC para un celular físico (RB-08) |

---

## 4. Endpoints utilizados

### 4.1 API externa

| API | URL base | Endpoint | Cliente en la app | Uso |
|-----|----------|----------|-------------------|-----|
| JSONPlaceholder | `https://jsonplaceholder.typicode.com/` | `GET /posts` | `ExternalApiService` + `ExternalApiClient` (Retrofit propio, **sin** el token JWT) | Pantalla `ExternalPostScreen` |

### 4.2 Microservicios propios (`corpaliv-backend`)

La app arma la URL como `http://<BACKEND_HOST>:<puerto>/api/` en `RetrofitClient`. `BACKEND_HOST` vale `10.0.2.2` en el emulador.

**auth-service** · puerto **8083** · `AuthApiService`

| Método | Ruta | Acceso | Uso en la app |
|--------|------|--------|---------------|
| POST | `/api/auth/login` | Público | Iniciar sesión (body `{correo, clave}` → token y datos de la sesión) |

**oportunidades-service** · puerto **8081** · `ApiService`

| Método | Ruta | Acceso | Uso en la app |
|--------|------|--------|---------------|
| GET | `/api/oportunidades` | Público | Listado de oportunidades |
| POST | `/api/oportunidades` | Admin | Crear oportunidad |
| PUT | `/api/oportunidades/{id}` | Admin | Editar oportunidad |
| DELETE | `/api/oportunidades/{id}` | Admin | Eliminar oportunidad (`204`) |

**aportes-service** · puerto **8082** · `AportesApiService`

| Método | Ruta | Acceso | Uso en la app |
|--------|------|--------|---------------|
| POST | `/api/aportes` | Público | Enviar aporte (persona o empresa) o postulación a programa |
| GET | `/api/aportes` | Admin | Listado del Dashboard |
| PATCH | `/api/aportes/{id}/estado` | Admin | Marcar como `CONTACTADO` / `PENDIENTE` |
| DELETE | `/api/aportes/{id}` | Admin | Eliminar registro (`204`) |

Las rutas de administrador exigen `Authorization: Bearer <token>`; sin token responden `401` y con otro rol `403`. El backend expone además `GET /api/auth/me`, `GET /api/oportunidades/{id}`, `GET /api/aportes/resumen`, `GET /api/aportes/{id}` y `PUT /api/aportes/{id}`, que la app no usa; la lista completa está en el README de `corpaliv-backend`.

---

## 5. Pasos para ejecutar

**Requisitos:** Android Studio con Android SDK 35 (`compileSdk = 35`, `minSdk = 24`), JDK 25 (lo resuelve el toolchain de Gradle, `gradle/gradle-daemon-jvm.properties`), AGP 9.3.0 y Gradle 9.6.0 (wrapper incluido). Backend: JDK 21, Maven 3.9+ y, para MySQL, Docker Desktop.

1. **Clonar los dos repositorios:**
   ```powershell
   git clone https://github.com/Vania-Bustos-Fuentealba/CorpalivMobile.git
   git clone https://github.com/benjamin-encina/corpaliv-backend.git
   ```
2. **Levantar el backend** (base de datos + 3 servicios) siguiendo la sección 5 del README de `corpaliv-backend`. Resumen con MySQL en Docker (publicado en el puerto **3307** del PC):
   ```powershell
   cd corpaliv-backend
   docker compose --profile mysql up -d mysql
   # En cada una de tres terminales:
   $env:DB_PORT = "3307"; cd auth-service;          mvn spring-boot:run "-Dspring-boot.run.profiles=mysql"   # 8083
   $env:DB_PORT = "3307"; cd oportunidades-service; mvn spring-boot:run "-Dspring-boot.run.profiles=mysql"   # 8081
   $env:DB_PORT = "3307"; cd aportes-service;       mvn spring-boot:run "-Dspring-boot.run.profiles=mysql"   # 8082
   ```
   Sin Docker basta `mvn spring-boot:run` en cada servicio (H2 en archivo).
3. **Abrir `CorpalivMobile` en Android Studio** y esperar el *Gradle Sync*. Android Studio crea `local.properties` con `sdk.dir=...` (ignorado por Git).
4. **Elegir el host del backend** (ver tabla de abajo). Sin configurar nada la app apunta a `10.0.2.2` (emulador).
5. **Compilar y ejecutar**: ▶ Run en Android Studio o, en PowerShell, `.\gradlew.bat :app:assembleDebug` (APK en `app/build/outputs/apk/debug/`).
6. **Iniciar sesión de demostración** (administrador): `admin@corpaliv.cl` / `admin`. Solo para el entorno académico.
7. **Pruebas y cobertura** (no necesitan backend ni emulador):
   ```powershell
   .\gradlew.bat :app:testDebugUnitTest :app:jacocoTestReport
   .\gradlew.bat :app:jacocoTestCoverageVerification   # falla si baja de 80 %
   ```

### Host del backend configurable (RB-08)

`RetrofitClient` lee `BuildConfig.BACKEND_HOST`, definido en `app/build.gradle.kts`. Orden de prioridad:

| Prioridad | Dónde | Ejemplo |
|-----------|-------|---------|
| 1 | Propiedad de Gradle por línea de comandos | `.\gradlew.bat :app:assembleDebug -Pcorpaliv.backendHost=192.168.1.50` |
| 2 | `local.properties` (ignorado por Git) | `corpaliv.backendHost=192.168.1.50` |
| 3 | Valor por defecto | `10.0.2.2` (emulador de Android Studio) |

Escribe solo la IP o el nombre del host (sin `http://` ni puerto; los puertos 8081/8082/8083 son fijos). Para un **celular físico**: conecta el teléfono y el PC a la misma red Wi-Fi, usa la IP del PC (`ipconfig`) y permite los puertos 8081–8083 en el firewall de Windows. Tras cambiar el host hay que volver a compilar.

---

## 6. Captura del APK firmado y del `.jks`

### 6.1 Cómo se generó (Android Studio, validado el 27-11-2026)

1. **Build › Generate Signed App Bundle or APK… › APK › Next**.
2. *Key store path* › **Create new…**: un `.jks` propio guardado **fuera del repositorio** (por ejemplo `...\Proyectos\claves\corpaliv-release.jks`), alias `corpaliv-release`, validez 25 años y datos del certificado. *Remember passwords* sin marcar.
3. *Build Variants*: **release** › **Create**. El APK queda en `app\release\app-release.apk`.
4. Verificar la firma:
   ```powershell
   $bt = Get-ChildItem "$env:LOCALAPPDATA\Android\Sdk\build-tools" | Sort-Object Name | Select-Object -Last 1
   & "$($bt.FullName)\apksigner.bat" verify --print-certs app\release\app-release.apk
   ```
5. Instalar en el emulador (primero quitar la versión debug, que tiene otra firma; si no, aparece `INSTALL_FAILED_UPDATE_INCOMPATIBLE`):
   ```powershell
   adb uninstall com.duocuc.corpalivapp
   adb install app\release\app-release.apk
   ```

Alternativa por consola: `.\scripts\generar-apk-firmado.ps1` (detalle en la sección 7, «APK firmado por script»).

### 6.2 Capturas

> ⏳ **Pendiente (Vania).** Guarda las imágenes en `docs/capturas/` con estos nombres y borra los `<!--` y `-->` de cada línea para que se muestren.

| # | Captura | Archivo |
|---|---------|---------|
| 1 | Aviso *APK(s) generated successfully* de Android Studio | `docs/capturas/01-apk-generado.png` |
| 2 | Carpeta `app\release\` con `app-release.apk` | `docs/capturas/02-apk-release.png` |
| 3 | Archivo `.jks` en su carpeta (solo nombre y ubicación, nunca contraseñas) | `docs/capturas/03-archivo-jks.png` |
| 4 | Salida de `apksigner verify --print-certs` | `docs/capturas/04-apksigner.png` |
| 5 | App firmada abierta en el emulador | `docs/capturas/05-app-emulador.png` |

<!-- ![APK generado](docs/capturas/01-apk-generado.png) -->
<!-- ![APK release](docs/capturas/02-apk-release.png) -->
<!-- ![Archivo .jks](docs/capturas/03-archivo-jks.png) -->
<!-- ![apksigner](docs/capturas/04-apksigner.png) -->
<!-- ![App en el emulador](docs/capturas/05-app-emulador.png) -->

### 6.3 El `.jks` y el repositorio

> ✍️ **Decisión del grupo.** Hoy `.gitignore` excluye `*.jks`, `*.keystore`, `*.p12`, `keystore.properties` y `/app/release/`, por lo que la clave y el APK **no** se suben.

- **Opción A (recomendada): entregar aparte.** El `.jks` real y el APK se entregan por el canal del curso (no por GitHub) y este README solo muestra las capturas. La clave real nunca queda pública.
- **Opción B: llave solo de entrega en el repositorio.** Si la pauta exige un `.jks` funcional dentro del repositorio, generar una clave **nueva y desechable** (no la real), por ejemplo `entrega/corpaliv-entrega.jks`, agregar al final de `.gitignore` la excepción `!entrega/corpaliv-entrega.jks` e indicar aquí su alias y contraseña. Cualquiera que clone el repositorio podrá firmar con ella, así que no debe usarse para publicar la app.

`PENDIENTE: indicar aquí la opción elegida (A o B)`

---

## 7. Detalle técnico

### 🚀 Tecnologías y Herramientas

* **Lenguaje:** [Kotlin](https://kotlinlang.org/)
* **UI Framework:** [Jetpack Compose](https://developer.android.com/jetpack/compose) con **Material Design 3**
* **Arquitectura:** MVVM (*Model-View-ViewModel*) con estados reactivos (*StateFlow*)
* **Conexión de Red:** [Retrofit 2](https://square.github.io/retrofit/) + **Gson**
* **Carga de Imágenes:** [Coil](https://coil-kt.github.io/coil/) (*Coroutine Image Loader*)
* **Concurrencia:** Kotlin Coroutines

---

### 📁 Estructura del Proyecto

```text
com.duocuc.corpalivapp/
├── data/
│   ├── local/          # EstadoDataStore y RegistroDataStore (DataStore Preferences)
│   ├── model/          # OportunidadModel, Persona, Empleado, ExternalPost, DTOs de auth y aportes, UsuarioUiState...
│   ├── network/        # Retrofit: RetrofitClient (host configurable, RB-08), ApiService, AuthApiService, AportesApiService, ExternalApiClient
│   └── repository/     # AuthRepository, OportunidadesRepository, AportesRepository, ExternalPostRepository
├── assets/institucion/ # Textos en Markdown: Corpaliv (Inicio), Escuela Jan Van Dijk y Alba Lab (Quiénes somos)
├── navigation/         # Screen.kt (rutas), NavigationEvent.kt, AppNavigation.kt (NavController + NavHost + ViewModels compartidos)
├── ui/
│   ├── components/     # OportunidadCard, AppTopBar, AppDrawerContent, AppBottomBar, MarkdownText, ImagenInteligente...
│   ├── screens/        # Home, Oportunidades, OportunidadForm (CRUD admin), Institucion, Registro, Resumen,
│   │                   # Login, Dashboard (admin), Perfil, ModoEspecial, ExternalPost (API externa)
│   ├── theme/          # Paleta de colores, tipografía y tema (Material 3)
│   └── viewmodel/      # Oportunidad, OportunidadForm, Dashboard, Usuario, Sesion, Estado, Perfil, Institucion, ExternalPost...
├── util/               # WindowSizeUtils, ArchivosUtils (FileProvider), RutUtils, ContactoUtils
└── MainActivity.kt     # Punto de entrada: llama a AppNavigation()
```

---

### 🧭 Guía 10 - Navegación y estructura visual

Flujo de navegación (MVVM): `Pantalla → MainViewModel (emite NavigationEvent) → AppNavigation (NavController) → destino`.

| Ruta | Pantalla | Componentes de navegación |
|------|----------|---------------------------|
| `home_page` | Inicio adaptativo (Guía 8 y 9) con la información de Corpaliv en bloques | `TopAppBar` + `ModalNavigationDrawer` (Compact/Medium) o `PermanentNavigationDrawer` (Expanded) |
| `oportunidades_page` | Oportunidades y proyectos RSE | `TopAppBar` con flecha de retorno |
| `institucion_page` | Quiénes somos: Escuela Jan Van Dijk y Alba Lab, leídos desde `.md` y mostrados en bloques | Selector de sección (una a cada costado, en azul) |
| `perfil_page` | Mi perfil: pide iniciar sesión y, con la cuenta de administrador, muestra la información general, el badge con el rol, la foto (cámara o galería), la opción Dashboard y cerrar sesión | Menú lateral, sección "Herramientas" |
| `dashboard_page` | Dashboard para administradores: aportes de personas y empresas con su botón "Contactar Whatsapp" | Botón "Dashboard" del perfil (requiere sesión de administrador) |

La `NavigationBar` inferior se declara una sola vez en `AppNavigation` y se muestra en **todas** las pantallas.

---

### 📝 Guía 11 - Formularios, validaciones y paso de información

- `UsuarioUiState` y `UsuarioErrores` modelan el estado del formulario (`data/model`).
- `UsuarioViewModel` guarda el estado en un `MutableStateFlow` y valida con `validarFormulario()`; los errores se corrigen en tiempo real mientras se escribe.
- `RegistroScreen` (`OutlinedTextField`, desplegable de motivo, `Button`, `isError` + `supportingText`) y `ResumenScreen` comparten el **mismo** `UsuarioViewModel`, sin pasar argumentos por la ruta.
- Flujo: `oportunidades_page` → clic en una oportunidad → `registro_page` → barra de carga → `resumen_page` (agradecimiento y aviso de contacto en un día hábil por WhatsApp o teléfono).
- `registro_page` tiene un selector **Persona / Empresa** que cambia los campos sin borrar lo ya escrito. Persona: nombre, apellido, RUN, correo, teléfono (+569) y motivo (aportar o ingresar a programa); si el motivo es "Aportar" se pide además la frecuencia de la donación (única, mensual o anual). Empresa: razón social, dirección comercial, RUT, giro, representante legal y su RUT, correo corporativo y teléfono.
- Al confirmar un aporte (persona que aporta o empresa) se muestra una pantalla de carga y luego, por 5 segundos, el mensaje de agradecimiento antes de volver al inicio. Esos aportes se guardan en el dispositivo (DataStore) y aparecen en el Dashboard.
- La pantalla de acceso usa `iniciarSesionRemoto()` contra `auth-service`; el token JWT y la sesión viven en memoria. El método local `iniciarSesion()` se conserva para las pruebas de la guía anterior y no es el acceso utilizado por la pantalla.
- El RUN y el RUT se validan por formato (`XXXXXXXX-X`); el dígito verificador queda desactivado por ahora (`VERIFICAR_DIGITO_VERIFICADOR` en `RutUtils.kt`).
- Pruebas unitarias en `UsuarioViewModelTest`, `RutUtilsTest` y `MarkdownParserTest`.

### ✨ Guía 12 - Animaciones y estados

- `ModoEspecialScreen`: interruptor persistente con **DataStore Preferences** (`EstadoDataStore` + `EstadoViewModel`).
- Estado derivado con `derivedStateOf`, panel con `rememberSaveable`, carga inicial con `CircularProgressIndicator`.
- Animaciones: `animateColorAsState`, `animateFloatAsState`, `AnimatedContent`, `AnimatedVisibility` y `animateContentSize`.

### 📷 Guía 13 - Recurso nativo: cámara y galería

- `PerfilScreen` usa `rememberLauncherForActivityResult` con `GetContent()` (galería) y `TakePicture()` (cámara) + permiso `CAMERA` en tiempo de ejecución.
- `FileProvider` (`res/xml/file_paths.xml` y `AndroidManifest.xml`) entrega a la cámara un `Uri` temporal seguro.
- `PerfilViewModel` guarda el `Uri?` en un `StateFlow`; `ImagenInteligente` lo muestra en círculo (o un ícono por defecto) y también se reutiliza en la cabecera del menú lateral.
- Probar en un **teléfono físico**; cancelar la galería o la cámara no cierra la app.

---

Las ramas de cada funcionalidad y sus PR están en la tabla de la sección 2.1. El historial anterior (oct–nov 2026) queda en el repositorio [`CorpalivApp`](https://github.com/Vania-Bustos-Fuentealba/CorpalivApp).

### Pruebas y cobertura

El proyecto usa Gradle Wrapper 9.6.0, Android Gradle Plugin 9.3.0 y el JDK 25 definido
en `gradle/gradle-daemon-jvm.properties`. Configura la ruta del SDK en `local.properties`
o desde Android Studio. Las pruebas unitarias usan JUnit, MockK y `kotlinx-coroutines-test`
y no requieren un backend ni emulador.

Desde la raíz del proyecto, en PowerShell:

```powershell
.\gradlew.bat :app:testDebugUnitTest :app:jacocoTestReport :app:lintDebug
.\gradlew.bat :app:jacocoTestCoverageVerification
```

En macOS/Linux, usa `bash ./gradlew` en lugar de `.\gradlew.bat`.

- Pruebas: `app/build/reports/tests/testDebugUnitTest/index.html`.
- Cobertura HTML: `app/build/reports/jacoco/jacocoTestReport/html/index.html`.
- Cobertura XML: `app/build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml`.
- Análisis estático: `app/build/reports/lint-results-debug.html`.

**Cobertura.** La cobertura se mide con JaCoCo sobre el código lógico de la app: ViewModels, repositorios, modelos, DataStore, clientes de red y utilidades. Se excluyen las pantallas y componentes de Jetpack Compose (`ui/screens`, `ui/theme`, barras, menú y tarjetas de `ui/components`, `AppNavigation`), `MainActivity` y las clases generadas (`R`, `BuildConfig`, `ComposableSingletons`), porque son interfaz declarativa que se valida en el emulador y no con pruebas unitarias en la JVM. Las exclusiones están en la lista `exclusionesCobertura` de `app/build.gradle.kts`. La tarea `:app:jacocoTestCoverageVerification` exige un mínimo de 80 % de instrucciones y líneas. Resultado (27-11-2026, 139 pruebas): **94 % de instrucciones, 90 % de ramas y 96,7 % de líneas**.

`ExternalPostViewModelTest` cubre carga, éxito, respuesta vacía, errores, reintentos,
actualización, peticiones simultáneas y cancelación. `SesionRemotaViewModelTest`
cubre el flujo remoto con respuestas simuladas, el token, la limpieza del formulario
y el cierre de sesión. Se mantienen las pruebas anteriores, incluidas las tres del
Repository externo.

La API externa está separada en `data/model/ExternalPost`, `data/network/ExternalApiService`,
`data/network/ExternalApiClient` y `data/repository/ExternalPostRepository`.
Su cliente no comparte el interceptor JWT del backend.
La opción «API externa» del menú lateral está registrada en `AppNavigation`,
por lo que su ruta abre `ExternalPostScreen` dentro del grafo de navegación.

Los archivos de la API externa, su pantalla y ViewModel, el menú lateral, las
pruebas nuevas y la configuración de compilación incluyen notas en español
junto a las instrucciones. Estas explican los datos, las acciones y las
comprobaciones para facilitar el estudio del proyecto. Se conservan también
los comentarios de las guías anteriores en el resto de las clases.

### APK firmado por script (RNF-09, alternativa)

La `signingConfig` de `release` ya existe en `app/build.gradle.kts` y se usa al compilar por consola (el asistente de Android Studio de la sección 6 no la necesita). La clave nunca se versiona.

**Opción A — script.** En PowerShell, desde la raíz del proyecto:

```powershell
.\scripts\generar-apk-firmado.ps1
# Para celular físico:  .\scripts\generar-apk-firmado.ps1 -BackendHost 192.168.1.50
```

El script localiza `keytool`, crea `%USERPROFILE%\corpaliv-signing\corpaliv-release.jks` si no existe (keytool pide la contraseña y los datos del certificado), pide las contraseñas por consola, las deja solo como variables de entorno de esa sesión, ejecuta `:app:assembleRelease` y verifica la firma con `apksigner` si lo encuentra. Si PowerShell bloquea el script: `powershell -ExecutionPolicy Bypass -File .\scripts\generar-apk-firmado.ps1`.

**Opción B — pasos manuales:**

1. Crear el almacén fuera del repositorio:
   ```powershell
   keytool -genkeypair -keystore $env:USERPROFILE\corpaliv-signing\corpaliv-release.jks -storetype JKS -alias corpaliv-release -keyalg RSA -keysize 3072 -validity 10000
   ```
   (si `keytool` no está en el PATH: `"$env:ProgramFiles\Android\Android Studio\jbr\bin\keytool.exe"`).
2. Copiar `keystore.properties.example` a `keystore.properties` y completar ruta (con `/`), alias y contraseñas, o bien definir las variables de entorno `CORPALIV_STORE_FILE`, `CORPALIV_STORE_PASSWORD`, `CORPALIV_KEY_ALIAS` y `CORPALIV_KEY_PASSWORD`.
3. `.\gradlew.bat :app:assembleRelease`
4. Resultado: `app/build/outputs/apk/release/app-release.apk`. Verificar con `apksigner verify --verbose --print-certs app-release.apk` (en `<Android SDK>\build-tools\<versión>\`).

Guarda una copia privada del `.jks` y sus contraseñas: sin ellas no se pueden firmar actualizaciones. Sin `keystore.properties` ni variables de entorno, `assembleRelease` falla; las pruebas unitarias y el build de debug no la necesitan.

### Validación de la integración

La entrega está configurada por defecto para un **emulador de Android**, con los microservicios
del computador en `10.0.2.2` (configurable, ver «Host del backend configurable»): oportunidades en el puerto 8081, aportes en 8082 y
autenticación en 8083. El manifiesto permite HTTP para este entorno local.
El APK necesita esos servicios encendidos para completar los flujos del backend.

Validado en el emulador el 27-11-2026 con MySQL en Docker (puerto 3307) y los 3 servicios arriba: inicio de sesión,
listado de oportunidades, crear/editar/eliminar oportunidades, aporte y postulación, Dashboard, error controlado con
`oportunidades-service` apagado, y el APK firmado instalado y funcionando. Pendiente: probar en un celular físico con
`-Pcorpaliv.backendHost=<IP del PC>`. Las pruebas con mocks no sustituyen esta comprobación contra los servicios reales.
