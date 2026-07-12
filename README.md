# Sistema de Gestión de Tickets Distribuidos
Este proyecto implementa una arquitectura de microservicios distribuidos para la gestión de comandas y asignación de personal.
Su objetivo es garantizar la **Alta Disponibilidad** y **Consistencia de Datos**, evitando que los pedidos de los clientes se pierdan si ocurre una caída del servidor o cortes de red.

El sistema se compone de dos microservicios (nodos):
* **Ticket-Service (Puerto 8081):** Representa la Caja/Punto de Venta (POS). Recibe el pedido del cliente, guarda la información y se encarga de la resiliencia del sistema. Incluye una función de auto-asignación para "Clientes Anónimos" (de paso).
* **Assignment-Service (Puerto 8082):** Representa el gestor de personal en cocina. Se encarga de asignar el ticket a un mozo disponible utilizando algoritmos de concurrencia (Exclusión Mutua / Round-Robin).

**Características de Sistemas Distribuidos implementadas:**
1. **Tolerancia a Fallas (Semana 16):** Uso de Timeouts, Reintentos limitados y Fallback (Aislamiento temporal del pedido).
2. **Recuperación de Fallas (Semana 17):** Uso de Checkpoints en Base de Datos (H2) y Roll-forward mediante tareas asíncronas (`@Scheduled`) para autosanar el sistema sin intervención humana, aplicando Idempotencia para evitar duplicidad de platos.

---

## 1. Cómo se inicia el proyecto

Para ejecutar este proyecto en un entorno local, sigue estos pasos:

### Requisitos previos
* Java 17 o superior.
* IDE (Eclipse, IntelliJ IDEA o VSCode).
* Postman (para probar los endpoints).

### Pasos de Ejecución
1. Abre tu IDE y carga la carpeta del proyecto `demo` (`ticket-service`).
2. Carga también la carpeta del proyecto `assignment-service` en otra ventana o módulo de tu IDE.
3. Para el **Flujo Normal**, ejecuta la clase principal `DemoApplication.java` (iniciará en `localhost:8081`).
4. Ejecuta la clase principal `AssignmentServiceApplication.java` (iniciará en `localhost:8082`).
5. Yep, sistema listo

*(Nota: Para simular la caída del sistema y probar la recuperación de fallas, deberás detener intencionalmente el servicio del puerto 8082).*

---

## 2. Resultados que deben tener (Pruebas de Resiliencia)

Para demostrar que la arquitectura distribuida funciona correctamente, apaga el `assignment-service` (puerto 8082) en tu IDE y sigue estas pruebas:

### Fase 1: Caída de Red y Aislamiento (Fallback)
* **Acción:** Abre Postman y envía una petición `POST` a `http://localhost:8081/api/tickets` con el siguiente JSON en el Body:
  ```json
  {
      "mesaId": "MESA-04",
      "detallePedido": "2 Ceviches de Conchas Negras y 2 Cervezas"
  }
* **Resultado Esperado:** * Postman recibirá un **200 OK** con el cliente auto-asignado a `CLI-ANONIMO`. La pantalla de caja no se congela.
* La consola del puerto 8081 mostrará:
  1. `[NUEVO PEDIDO/CHECKPOINT] Intentando procesar TICKET-... registrado en el LOG` (El pedido se guardó seguro en disco).
  2. `[FALLA DETECTADA] Timeout o error de conexión...` (Repetido 3 veces por los reintentos).
  3. `[CAMBIO DE ESTRATEGIA] Reintentos agotados. Activando Fallback...` (El ticket queda en espera con estado `PENDIENTE_DE_ASIGNACION`).

### Fase 2: Autosanación y Roll-forward (Recuperación)
* **Acción:** Enciende / Ejecuta nuevamente el `assignment-service` (puerto 8082) en tu IDE. No envíes nada nuevo por Postman; solo mira la consola del puerto 8081.
* **Resultado Esperado:** * La tarea asíncrona de Spring Boot detectará la red activa y la consola mostrará:
    1. `[RECUPERACIÓN INICIADA] Se encontraron 1 pedidos pendientes de sincronización. Intentando recuperar...`
    2. `[ROLL-FORWARD] Intentando conectar de nuevo por la red para el pedido...`
    3. `[RECUPERACIÓN EXITOSA] El antiguo pedido TICKET-... fue sincronizado automáticamente y asignado al mozo...`
* La base de datos se actualiza internamente al estado `ASIGNADO` de manera transparente y sin duplicar la comanda.
