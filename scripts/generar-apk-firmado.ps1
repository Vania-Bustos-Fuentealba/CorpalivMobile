<#
 RNF-09 - Genera el keystore .jks (si no existe) y compila el APK de release firmado.

 Uso (PowerShell, desde la raíz de CorpalivApp):
   .\scripts\generar-apk-firmado.ps1
   .\scripts\generar-apk-firmado.ps1 -BackendHost 192.168.1.50      # APK para celular físico (RB-08)
   .\scripts\generar-apk-firmado.ps1 -KeystorePath D:\privado\corpaliv-release.jks

 Seguridad:
   - Las contraseñas se piden por consola y viven solo en variables de entorno de ESTA sesión
     (CORPALIV_STORE_PASSWORD / CORPALIV_KEY_PASSWORD). No se escriben en ningún archivo.
   - El .jks se guarda por defecto en %USERPROFILE%\corpaliv-signing, fuera del repositorio.
   - Si se usa keystore.properties en su lugar, ese archivo y *.jks ya están en .gitignore.
#>
param(
    [string]$KeystorePath = (Join-Path $env:USERPROFILE 'corpaliv-signing\corpaliv-release.jks'),
    [string]$KeyAlias = 'corpaliv-release',
    [string]$BackendHost = ''
)
$ErrorActionPreference = 'Stop'
$raiz = Split-Path -Parent $PSScriptRoot
Set-Location $raiz

function Leer-Secreto([string]$mensaje) {
    $s = Read-Host -AsSecureString $mensaje
    $b = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($s)
    try { [Runtime.InteropServices.Marshal]::PtrToStringBSTR($b) } finally { [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($b) }
}

# 1. Localizar keytool (JAVA_HOME, PATH o el JDK que trae Android Studio).
$keytool = $null
if ($env:JAVA_HOME -and (Test-Path "$env:JAVA_HOME\bin\keytool.exe")) { $keytool = "$env:JAVA_HOME\bin\keytool.exe" }
if (-not $keytool) { $cmd = Get-Command keytool -ErrorAction SilentlyContinue; if ($cmd) { $keytool = $cmd.Source } }
if (-not $keytool) {
    $jbr = "$env:ProgramFiles\Android\Android Studio\jbr\bin\keytool.exe"
    if (Test-Path $jbr) { $keytool = $jbr }
}
if (-not $keytool) { throw 'No se encontró keytool. Instala un JDK o define JAVA_HOME (Android Studio incluye uno en ...\Android Studio\jbr).' }
Write-Host "keytool: $keytool"

# 2. Crear el keystore solo si no existe (nunca se sobrescribe una clave ya generada).
if (Test-Path $KeystorePath) {
    Write-Host "Se usará el keystore existente: $KeystorePath"
} else {
    New-Item -ItemType Directory -Force -Path (Split-Path -Parent $KeystorePath) | Out-Null
    Write-Host "Se creará $KeystorePath. keytool pedirá la contraseña y los datos del certificado (nombre, organización, país: CL)."
    & $keytool -genkeypair -keystore $KeystorePath -storetype JKS -alias $KeyAlias -keyalg RSA -keysize 3072 -validity 10000
    if ($LASTEXITCODE -ne 0) { throw 'keytool no pudo crear el keystore.' }
    Write-Host 'IMPORTANTE: respalda el .jks y sus contraseñas en un lugar privado; sin ellos no podrás firmar actualizaciones.'
}

# 3. Credenciales solo en memoria (variables de entorno de esta sesión).
$env:CORPALIV_STORE_FILE = $KeystorePath -replace '\\', '/'
$env:CORPALIV_KEY_ALIAS = $KeyAlias
$env:CORPALIV_STORE_PASSWORD = Leer-Secreto 'Contraseña del keystore'
$claveDistinta = Read-Host 'La clave tiene una contraseña distinta a la del keystore? (s/N)'
if ($claveDistinta -match '^[sSyY]') { $env:CORPALIV_KEY_PASSWORD = Leer-Secreto 'Contraseña de la clave' }
else { $env:CORPALIV_KEY_PASSWORD = $env:CORPALIV_STORE_PASSWORD }

# 4. Compilar el APK de release (RB-08: host opcional para celular físico).
$gradleArgs = @(':app:assembleRelease')
if ($BackendHost) { $gradleArgs += "-Pcorpaliv.backendHost=$BackendHost" }
try {
    & .\gradlew.bat @args
    if ($LASTEXITCODE -ne 0) { throw 'Gradle falló. Revisa el mensaje anterior.' }
} finally {
    Remove-Item Env:CORPALIV_STORE_PASSWORD, Env:CORPALIV_KEY_PASSWORD, Env:CORPALIV_STORE_FILE, Env:CORPALIV_KEY_ALIAS -ErrorAction SilentlyContinue
}

# 5. Verificar la firma si apksigner está disponible.
$apk = Join-Path $raiz 'app\build\outputs\apk\release\app-release.apk'
if (-not (Test-Path $apk)) { throw "No se encontró $apk" }
Write-Host "APK generado: $apk"
$sdk = $env:ANDROID_HOME; if (-not $sdk) { $sdk = $env:ANDROID_SDK_ROOT }
if (-not $sdk -and (Test-Path 'local.properties')) {
    $linea = Select-String -Path 'local.properties' -Pattern '^sdk\.dir=(.+)$' | Select-Object -First 1
    if ($linea) { $sdk = ($linea.Matches[0].Groups[1].Value) -replace '\\\\', '\' -replace '\\:', ':' }
}
$apksigner = if ($sdk) { Get-ChildItem "$sdk\build-tools" -Recurse -Filter apksigner.bat -ErrorAction SilentlyContinue | Sort-Object FullName -Descending | Select-Object -First 1 }
if ($apksigner) { & $apksigner.FullName verify --verbose --print-certs $apk }
else { Write-Host 'apksigner no encontrado: verifica la firma con Android Studio (Build > Analyze APK) o instalando el APK.' }
