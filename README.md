## Organizador de Eventos Universitarios
Descripción:
Este proyecto es una aplicación de consola desarrollada en Java para la gestión integral de eventos académicos en entornos universitarios. Permite crear, modificar y eliminar eventos, asignar recursos específicos (aulas, equipos audiovisuales, materiales) y gestionar la participación de personas (estudiantes, profesores, personal administrativo).

La aplicación funciona mediante un menú interactivo en terminal que guía al usuario a través de las diferentes operaciones, validando las entradas y manteniendo un registro estructurado de las actividades académicas programadas.

## Requisitos para ejecutar este proyecto es necesario contar con:

JDK 11 (Java Development Kit)

NetBeans IDE 21 con plugin de soporte para Java SE

Apache Ant (incluido en NetBeans 21)

## Instalación y Configuración
Opción 1: Para Usuarios Finales
Descargue el código fuente en formato ZIP desde la página principal del repositorio

Descomprima el archivo en una carpeta de su preferencia

Abra NetBeans 21 y seleccione File → Open Project

Navegue hasta la carpeta descomprimida y seleccione el proyecto

Espere a que NetBeans indexe las dependencias

----------------------------------------------------------------------------------------

Opción 2: Para Colaboradores
Clone el repositorio usando Git:

git clone https://github.com/marselomorales/Eventos-Universidad.git

Abra el proyecto en NetBeans 21 mediante File → Open Project

Verifique que el JDK 11 esté configurado como Java Platform por defecto:

Right-click en el proyecto → Properties → Libraries

Verifique "Java Platform" esté configurado como JDK 11

## Ejecución
Desde NetBeans IDE
Asegúrese de tener el proyecto abierto en el IDE

Haga clic derecho sobre el proyecto y seleccione Run

Alternativamente, use la tecla F6 o el botón de ejecución (▶) en la barra de herramientas

Desde Terminal con Ant
Abra una terminal en la raíz del proyecto

Ejecute el siguiente comando:

ant run

## Notas Finales:

Migración de Versiones
Este proyecto fue migrado desde JDK 23 y NetBeans 22 a JDK 11 y NetBeans 21 para cumplir con los requisitos académicos del curso. Los cambios principales incluyen:

Ajuste de versiones en el archivo build.xml

Verificación de compatibilidad de características de Java

Reconfiguración de las rutas de bibliotecas

Tag de Respaldo
Se encuentra disponible el tag pre-reemplazo-jdk11 que contiene el estado del proyecto antes de la migración, para referencia histórica y comparación de cambios. Para acceder a esta versión:

git checkout pre-reemplazo-jdk11
