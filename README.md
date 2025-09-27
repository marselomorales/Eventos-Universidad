# 🎓 Proyecto: Eventos Universidad

Este proyecto es una aplicación para la **gestión de eventos universitarios**, donde se pueden administrar asistentes, recursos, usuarios y la información general de los eventos.  
Está desarrollado en **Java** y utiliza programación orientada a objetos para manejar las diferentes entidades del sistema.

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

- **Lenguaje**: Java  
- **IDE recomendado**: NetBeans (aunque puedes usar cualquier IDE compatible con Java)  
- **Paradigma**: Programación Orientada a Objetos (POO)  

---

## 🚀 Cómo ejecutar el proyecto

1. Clona o descarga el repositorio.
2. Abre el proyecto en **NetBeans** o tu IDE preferido.
3. Asegúrate de tener instalado:
   - **Java JDK 8 o superior**
4. Compila y ejecuta la clase principal con `main`.

---

## ⚠️ Problemas comunes y soluciones

### 1. Error: *"No se encuentra la librería"*, *"Class not found"* o error al compilar
Esto puede ocurrir si la plataforma configurada en tu IDE no coincide con la versión de Java que tienes instalada.  
Para solucionarlo en **NetBeans**:  
- Click derecho en el proyecto → **Propiedades**  
- Ve a **Librerías**  
- En **Plataforma Java**, selecciona la **plataforma correcta** (por ejemplo, JDK 8 o superior)  
- Aplica los cambios y vuelve a compilar  

Este paso solucionó el problema en la mayoría de los computadores donde se ejecutó el proyecto.

