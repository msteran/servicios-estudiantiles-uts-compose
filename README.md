# Sistema Integrado de Servicios Estudiantiles UTS

**Manuel Santiago Terán Suárez y Juan David Niño Suarez**

Aplicación nativa de Android en Kotlin con Jetpack Compose, Material 3 y MVVM. Simula el registro de un perfil académico, la liquidación de cursos libres y la consulta del historial financiero. Los datos se conservan al navegar entre pestañas y al rotar la pantalla. Al finalizar el proceso de la app, se reinicia la sesión.

## Ejecutar

1. Abre la carpeta del proyecto en Android Studio y deja que Gradle sincronice.
2. Configura Gradle con JDK 17 o 21 e instala el SDK de Android 35.
3. Selecciona un emulador o equipo físico con Android 6.0 (API 23) o superior y pulsa ▶.

En Windows, puedes compilar y ejecutar las pruebas desde PowerShell:

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest
```

## Arquitectura

- `data` contiene los modelos de estudiante, facultad, curso, cotización y liquidación.
- `viewmodel` contiene `PortalViewModel`, que valida el perfil, genera el correo, comprueba el bloqueo de matrícula y actualiza el historial y su total. Un `MutableStateFlow` privado se publica como `StateFlow` de solo lectura.
- `ui` contiene las pantallas Material 3. Cada pantalla observa el estado con `collectAsStateWithLifecycle()` y envía los eventos al ViewModel. El historial usa `LazyColumn` con el identificador de cada liquidación como clave.

Flujo unidireccional: acción en Compose → método del ViewModel → nuevo estado → pantalla actualizada.

## Tarifas simuladas (COP)

| Curso | Costo base |
|---|---:|
| Inglés Técnico | $180.000 |
| Programación Web | $240.000 |
| Ciberseguridad | $320.000 |

La beca descuenta el 50 % del costo base. Si se activa Icetex, el 5 % de recargo se calcula sobre el saldo después del descuento y se redondea al peso.

## Guion de sustentación

1. Abre **Matrícula** y selecciona **Inglés Técnico**. Señala el botón **Liquidar** deshabilitado y usa **Ir al perfil**.
2. Pulsa **Guardar perfil** con los campos vacíos para mostrar la validación. Registra a **María José**, código **00123**, facultad **Ingenierías**. Comprueba que se muestre `maria.jose.00123@uts.edu.co`.
3. Vuelve a **Matrícula**, marca **Beca de Excelencia UTS** y activa **Financiación Icetex**. Muestra el desglose: $180.000 de base, $90.000 de descuento y $4.500 de recargo. Pulsa **Liquidar** y comprueba el total de **$94.500** en **Historial**.
4. Registra **Programación Web** con ambos controles desactivados. Regresa a **Historial** y comprueba dos registros y un total de **$334.500**.
5. Abre `PortalViewModel.kt` y sigue la llamada a `enroll()` desde la pantalla. Muestra cómo el ViewModel comprueba el perfil, agrega el registro y publica el nuevo historial y el total juntos.
