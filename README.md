# CentroTareas

Aplicación Android del laboratorio GLAB-S14-JLEONS-2026, Fase 1, con Kotlin,
Jetpack Compose, Material 3 y arquitectura MVVM + Repository.

El paquete `com.clemente.centrotareas` se utiliza por indicación del profesor.
Se conserva el primer commit del alumno sin modificar su mensaje ni su historial.
La estructura base que faltaba se incorpora junto con el Paso 1, por acuerdo con
el alumno; no se importa otro proyecto ni se implementa la Fase 2.

## Observación del Paso 1

La demostración temporal asigna `Trabajando...`, ejecuta `Thread.sleep(4000)`
en el hilo principal y asigna `Terminó`. Durante esos cuatro segundos la pantalla
no puede dibujar nuevos fotogramas ni atender los toques del contador.
Aunque el estado intermedio sí se asigna, `Trabajando...` no llega a dibujarse:
cuando el hilo vuelve a procesar la interfaz, el mensaje ya es `Terminó`.

Una espera de diez segundos podría provocar un diálogo de aplicación que no
responde (ANR), dependiendo de los eventos pendientes y de las condiciones del
sistema. No se ha realizado esa prueba de diez segundos.

La espera bloqueante es exclusiva de este paso y se elimina en el siguiente.