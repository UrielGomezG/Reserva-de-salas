# Script para compilar y ejecutar la aplicación JavaFX

$projectDir = "c:\Users\Milldret\Desktop\Reserva-de-salas\ReservaDeSalas"
$srcDir = "$projectDir\src\main\java"
$buildDir = "$projectDir\build"
$classesDir = "$buildDir\classes"
$modsDir = "$buildDir\mods"
$resourcesDir = "$projectDir\src\main\resources"

# Crear directorios si no existen
New-Item -ItemType Directory -Path $classesDir -Force | Out-Null
New-Item -ItemType Directory -Path $modsDir -Force | Out-Null

# Copiar recursos
Copy-Item -Path "$resourcesDir\*" -Destination $classesDir -Recurse -Force

# Compilar el módulo
$compileCmd = @(
    'javac',
    '--module-source-path', $srcDir,
    '-d', $modsDir,
    '-p', "$env:USERPROFILE\.m2\repository\org\openjfx\javafx-controls\21.0.6\javafx-controls-21.0.6.jar;$env:USERPROFILE\.m2\repository\org\openjfx\javafx-fxml\21.0.6\javafx-fxml-21.0.6.jar;$env:USERPROFILE\.m2\repository\org\openjfx\javafx-base\21.0.6\javafx-base-21.0.6.jar;$env:USERPROFILE\.m2\repository\org\openjfx\javafx-graphics\21.0.6\javafx-graphics-21.0.6-win.jar;$env:USERPROFILE\.m2\repository\org\controlsfx\controlsfx\11.2.1\controlsfx-11.2.1.jar",
    "$srcDir\module-info.java",
    "$srcDir\interfaz\reservadesalas\Lanzador\Main.java",
    "$srcDir\interfaz\reservadesalas\Controladores\ControladorPrincipal.java",
    "$srcDir\interfaz\reservadesalas\Controladores\ControladorLogin.java",
    "$srcDir\interfaz\reservadesalas\Controladores\ControladorRegistro.java",
    "$srcDir\interfaz\reservadesalas\Controladores\ControladorCalendario.java",
    "$srcDir\interfaz\reservadesalas\Controladores\ControladorReservas.java"
)

Write-Host "Compilando..."
& @compileCmd

# Ejecutar la aplicación
Write-Host "Ejecutando..."
$javaCmd = @(
    'java',
    '-p', "$modsDir",
    '--module', 'interfaz.reservadesalas/interfaz.reservadesalas.Lanzador.Main'
)

& @javaCmd
