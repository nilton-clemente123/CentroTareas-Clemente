# Registro de asistencia de IA — Fase 2

## Procedencia y alcance

Esta fase se desarrolla en `mejora-ia`, a partir de la Fase 1 terminada en
`main`. La asistencia de esta conversación es **Cline**, no Gemini. La guía
solicita Gemini en Android Studio; este registro no acredita su uso ni inventa
respuestas de esa herramienta. Si el profesor exige específicamente Gemini,
queda pendiente validar con él esta diferencia de herramienta.

| Prompt que escribí | Qué respondió la IA (Cline, no Gemini) | Qué cambié o descarté | Por qué |
| --- | --- | --- | --- |
| «revisa, el documento, hacen falta los commits de la fase 2, puedes agregarlo, procura solamente agregar los commits falantes y subirlo a github» | Tras revisar la página 16 y Git, propuso tres mejoras: progreso desde Repository, primos con progreso y comparación acumulada de tiempos; una mejora por commit en `mejora-ia`. | Se conserva la Fase 1 intacta en `main`, el paquete indicado por el profesor y las bibliotecas existentes. No se añaden commits vacíos ni se atribuye el trabajo a Gemini. | Completar solo lo pendiente, con cambios reales y comprobables y sin reescribir el historial. |

## Decisiones y comprobaciones

### 1. Progreso desde el Repository

- `TareasRepository.progresoTarea()` expone un `Flow<Float>` frío: cada
  recolección comienza en 0, realiza 100 pasos con `delay(40)` y termina en 1.
- El Repository utiliza `flow { emit(...) }` y `flowOn(Dispatchers.Default)`.
  El ViewModel recolecta desde `viewModelScope`, por lo que publica los cambios
  de UI en Main sin cambiar el contexto de cada emisión manualmente.
- Se mantienen el `Job`, la prevención de doble inicio, la cancelación y los
  mensajes de la Fase 1. No se captura ni se oculta la cancelación del flujo.
- Se añaden pruebas de emisiones, nueva recolección y cancelación del productor,
  además de conservar las pruebas existentes de pantalla.

### 2. Primos con progreso

- Se aplica la segunda mejora de la propuesta generada para el mismo prompt;
  no hubo otro prompt del usuario ni una respuesta de Gemini.
- `contarPrimos()` emite `AvancePrimos` desde un Flow en Default, con cantidad
  parcial y porcentaje, y comprueba la cancelación en cada candidato.
- Se calcula el porcentaje con `Long` para evitar desbordamiento cuando N es
  `Int.MAX_VALUE`. Solo se emite al cambiar el porcentaje (máximo 101 estados).
- Se descarta añadir `delay` al cálculo: sigue siendo trabajo real de CPU.
- El ViewModel publica el porcentaje en Main y relanza `CancellationException`;
  Compose muestra la barra y el porcentaje. Se prueba exactitud, monotonía,
  extremos pequeños, cancelación, doble inicio y reinicio.

### 3. Comparación acumulada de tiempos

- Se aplica la tercera mejora propuesta para el prompt registrado; tampoco se
  inventa un prompt adicional ni una intervención de Gemini.
- El ViewModel conserva una lista de pares de tiempos en un StateFlow de solo
  lectura. Cada éxito completa la primera fila pendiente de su modo o crea una
  nueva. No se sobrescriben mediciones anteriores ni se registran errores.
- Se utiliza `System.nanoTime()` para medir duraciones de cargas sin depender
  de cambios en la hora del sistema. La UI muestra milisegundos y ahorro.
- Se descarta DataStore: corresponde a otra mejora no seleccionada y requiere
  una dependencia adicional. La tabla dura lo que el ViewModel y sobrevive a
  recreaciones por configuración, pero no a la muerte del proceso.
- Se añaden comprobaciones de acumulación, emparejamiento, ejecuciones
  repetidas, errores, doble inicio y conservación tras recrear la Activity.

## Resultado de validación

El 18 de noviembre de 2026 se ejecutaron `assembleDebug`, `testDebugUnitTest`,
`lintDebug` y la suite completa `connectedDebugAndroidTest`: compilación
correcta, 1 prueba unitaria y 15 instrumentadas aprobadas, 0 errores de Lint
y 13 advertencias. Se revisó el diff y la ausencia de APIs prohibidas en Kotlin.
Las validaciones específicas de las dos primeras mejoras también aprobaron
antes de crear sus respectivos commits.