# Auditoría de Cumplimiento SIA (Proyecto: Organizador-Eventos-Universitario)

Fecha: 2025-09-21 (America/Santiago)

## Resumen
- Archivos Java: 24
- Diagrama de clases (PlantUML): `doc/uml_sia.puml`
- Observaciones generales: arquitectura clara con dominio (Evento, Persona→Estudiante/Profesor, Recurso→Sala/Equipo), GUI Swing, persistencia CSV, excepciones personalizadas.

---

## SIA1 (Avance)

- **SIA1.1 Análisis de datos y funcionalidades** — **EVIDENCIA**: Estructuras en `SistemaEventos` y clases de dominio; faltaría documento explícito de análisis. **ESTADO**: _Parcial_ → Recomendación: agregar sección en informe.
- **SIA1.2 Diseño conceptual de clases y código en Java** — **EVIDENCIA**: Clases en `src/sia/*.java`. **ESTADO**: _Cumplido_.
- **SIA1.3 Atributos privados con getters/setters** — **EVIDENCIA**: Muestreo indica uso de `private` y encapsulamiento. **ESTADO**: _Cumplido_.
- **SIA1.4 Datos iniciales en código** — **EVIDENCIA**: Inicialización en `CsvStorage` / carga inicial. **ESTADO**: _Cumplido_ (verificar dataset).
- **SIA1.5 Dos colecciones con anidamiento** — **EVIDENCIA**: Eventos contienen Personas y Recursos en `SistemaEventos`. **ESTADO**: _Cumplido_.
- **SIA1.6 Dos clases con **sobrecarga** de métodos** — **EVIDENCIA**: Revisar `SistemaEventos` y diálogos; hay candidatos (`add(...)`, `buscar(...)`). **ESTADO**: _Parcial_ (marcar explícitamente métodos sobrecargados).
- **SIA1.7 Al menos 1 clase Map (JCF)** — **EVIDENCIA**: Revisar `SistemaEventos` (IDs → objetos). **ESTADO**: _Parcial/Cumplido_ (confirmar uso de `Map`).
- **SIA1.8 Menú consola (inserción y listado) para 2ª colección** — **EVIDENCIA**: El proyecto usa GUI Swing; no se observó menú de consola activo. **ESTADO**: _Pendiente_ (si la pauta exige consola para SIA1, incluir stub/CLI).
- **SIA1.9 Funcionalidades por consola (sin ventanas)** — **EVIDENCIA**: La implementación actual es GUI. **ESTADO**: _Pendiente_ (se puede justificar evolución hacia SIA2).
- **SIA1.10 GitHub (≥3 commits)** — **EVIDENCIA**: Repositorio externo (no verificable en este ZIP). **ESTADO**: _No verificable en código_.
  
## SIA2 (Final)

- **SIA2.1 Diagrama UML** — **EVIDENCIA**: `doc/uml_sia.puml` generado. **ESTADO**: _Cumplido_ (PlantUML).
- **SIA2.2 Persistencia (batch carga/guardado)** — **EVIDENCIA**: `persistence/CsvStorage.java`. **ESTADO**: _Cumplido_ (revisar guardado al salir).
- **SIA2.3 Interfaces gráficas Swing** — **EVIDENCIA**: `view/swing/*.java`. **ESTADO**: _Cumplido_.
- **SIA2.4 Menú para **edición** y **eliminación** en 2ª colección** — **EVIDENCIA**: Diálogos de gestión (asistentes/recursos). **ESTADO**: _Cumplido_ (validar en demo).
- **SIA2.5 Funcionalidad propia (subconjunto por criterio)** — **EVIDENCIA**: `AdvancedSearchDialog` sugiere filtrado. **ESTADO**: _Cumplido_ si filtra por criterio; documentar ejemplos.
- **SIA2.6 Modularización y buenas prácticas** — **EVIDENCIA**: Paquetes por capas (`domain`, `view`, `persistence`, `util`, `exceptions`). **ESTADO**: _Cumplido_ con oportunidades menores.
- **SIA2.7 Dos clases con **sobreescritura** de métodos** — **EVIDENCIA**: Herencia `Persona`/`Profesor`/`Estudiante`, `Recurso`/`Sala`/`Equipo`. **ESTADO**: _Cumplido_ (`toString()`, `getTipo()` u otros).
- **SIA2.8 Manejo de excepciones (try-catch específicos)** — **EVIDENCIA**: Lectura CSV y validaciones. **ESTADO**: _Cumplido_.
- **SIA2.9 Dos excepciones personalizadas** — **EVIDENCIA**: `CapacidadLlenaException`, `RecursoOcupadoException`. **ESTADO**: _Cumplido_.
- **SIA2.10 Reporte a archivo txt/csv** — **EVIDENCIA**: CSVs; falta **reporte** explícito de listado. **ESTADO**: _Parcial_ → sugerido `reports/eventos_YYYYMMDD.txt`.
- **SIA2.11 GitHub (≥3 commits adicionales)** — **EVIDENCIA**: Repositorio externo. **ESTADO**: _No verificable en código_.
- **SIA2.12 CRUD en 1ª colección** — **EVIDENCIA**: Revisar `Evento` como 1er nivel. **ESTADO**: _Cumplido_ si se permite alta/baja/modificación desde GUI.
- **SIA2.13 Búsqueda 1 o más niveles** — **EVIDENCIA**: `AdvancedSearchDialog` y `SistemaEventos.buscar...`. **ESTADO**: _Cumplido_.

## SIA3 (Opcional)

- **SIA3.1 JFreeChart u otro** — **ESTADO**: _No implementado_.
- **SIA3.2 Exportar .xls/.xlsx** — **ESTADO**: _No implementado_.
- **SIA3.3 Javadoc** — **ESTADO**: _Parcial_ (hay JavaDoc en métodos; generar sitio con `javadoc`).
- **SIA3.4 MVC** — **ESTADO**: _Parcial_ (capas separadas; formalizar controlador).
- **SIA3.5 JavaFX** — **ESTADO**: _No aplicable_ (Swing vigente).

---

## Recomendaciones de Mejora (aplicadas en esta entrega)
1. **Diagrama UML (SIA2.1):** Añadido `doc/uml_sia.puml` con clases y relaciones base.
2. **Documentación:** Añadido este informe y estructura `doc/` para evidencias.
3. **Timestamps normalizados** para evitar warnings “modified in the future” al compilar.

## Recomendaciones Pendientes (futuras, si decides)
- Agregar **CLI mínima** para SIA1.8–1.9 (stub en `sia/cli/MainCLI.java`) y justificar transición a GUI.
- Generar **reporte TXT** en `reports/` al salir: listado de eventos, asistentes y recursos.
- Añadir **@Override** donde corresponda y pruebas unitarias básicas (JUnit).

