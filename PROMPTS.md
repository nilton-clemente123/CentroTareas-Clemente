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