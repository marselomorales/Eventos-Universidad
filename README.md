# 🎓 Proyecto: Eventos Universidad

Este proyecto es una aplicación para la **gestión de eventos universitarios**, donde se pueden administrar asistentes, recursos, usuarios y la información general de los eventos.  
Está desarrollado en **Java** y **diseñado para ejecutarse en NetBeans 21 con JDK 11** (Java 11).

---

## 🧩 Características principales

- **Gestión de eventos**: crear, modificar y visualizar eventos.
- **Registro de asistentes**: manejo de personas asociadas a cada evento.
- **Recursos disponibles**: asignación de recursos para la realización de eventos.
- **Interacción con el sistema**: administración a través de un flujo organizado de clases.

---

## 📂 Estructura del proyecto

El sistema se organiza con clases principales como:
- `Evento`: Representa cada evento con su información.
- `Persona`: Modela a los asistentes o responsables del evento.
- `Recurso`: Define los recursos disponibles para un evento.
- `SistemaEventos`: Controla la lógica principal del sistema.
- `Usuario`: Representa a los usuarios que interactúan con el sistema.

---

## 🛠️ Tecnologías utilizadas

- **Lenguaje**: Java (JDK 11)
- **IDE objetivo**: NetBeans 21  
- **Paradigma**: Programación Orientada a Objetos (POO)

> ℹ️ Si usas otro IDE compatible con Java, asegúrate de configurar el SDK/Project JDK en **Java 11**.

---

## 🚀 Cómo ejecutar el proyecto

1. Clona o descarga el repositorio.
2. Abre el proyecto en **NetBeans 21** (o tu IDE preferido configurado con Java 11).
3. Asegúrate de tener instalado:
   - **Java JDK 11**
4. Compila y ejecuta la clase principal con `main`.

---

## ⚠️ Problemas comunes y soluciones

### 1. Error: *"No se encuentra la librería"*, *"Class not found"* o error al compilar
Esto puede ocurrir si la plataforma configurada en tu IDE no coincide con **Java 11** o si el proyecto no está apuntando a la plataforma correcta.

**En NetBeans 21**:
- Click derecho en el proyecto → **Propiedades**  
- **Librerías** → **Plataforma Java**: selecciona **JDK 11**  
- Aplica los cambios y vuelve a compilar

**En otros IDEs**:
- Configura el **Project SDK/JDK** a **11** y sincroniza/recarga el proyecto.

> En la mayoría de los computadores, estos pasos solucionan los problemas de compilación y ejecución.

---
