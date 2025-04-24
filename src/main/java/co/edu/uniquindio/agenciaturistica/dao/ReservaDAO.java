package co.edu.uniquindio.agenciaturistica.dao;

import co.edu.uniquindio.agenciaturistica.model.*;
import co.edu.uniquindio.agenciaturistica.model.Enums.EstadoReserva;
import co.edu.uniquindio.agenciaturistica.model.Enums.FormaPago;
import co.edu.uniquindio.agenciaturistica.util.DataBaseConnection;
import co.edu.uniquindio.agenciaturistica.util.Respuesta;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ReservaDAO {

    private Connection connection;
    private ClienteDAO clienteDAO;
    private PaqueteDAO paqueteDAO;
    private HabitacionDAO habitacionDAO;

    public ReservaDAO() throws SQLException {
        try {
            this.connection = DataBaseConnection.getConnection();
            this.clienteDAO = new ClienteDAO();
            this.paqueteDAO = new PaqueteDAO();
            this.habitacionDAO = new HabitacionDAO();
        } catch (SQLException e) {
            throw new SQLException(e);
        }
    }

    public ReservaDAO(Connection connection) throws SQLException {
        this.connection = connection;
        this.clienteDAO = new ClienteDAO(connection);
        this.paqueteDAO = new PaqueteDAO(connection);
        this.habitacionDAO = new HabitacionDAO(connection);
    }

    /**
     * Este método permite obtener todas las reservas registradas en el sistema
     * @return Lista de reservas
     * @throws SQLException
     */
    public List<Reserva> obtenerTodasLasReservas() throws SQLException {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        List<Reserva> listaReservas = new ArrayList<>();

        try {
            String query = "SELECT * FROM reserva ORDER BY fecha_inicio DESC";
            preparedStatement = connection.prepareStatement(query);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                Reserva reserva = crearReservaDesdeResultSet(resultSet);
                listaReservas.add(reserva);
            }

            return listaReservas;
        } catch (SQLException e) {
            throw new SQLException("Error al obtener las reservas: " + e.getMessage(), e);
        } finally {
            if (resultSet != null) resultSet.close();
            if (preparedStatement != null) preparedStatement.close();
        }
    }

    /**
     * Este método permite buscar reservas por el ID del cliente
     * @param clienteId ID del cliente
     * @return Lista de reservas del cliente
     * @throws SQLException
     */
    public List<Reserva> buscarReservasPorCliente(String clienteId) throws SQLException {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        List<Reserva> listaReservas = new ArrayList<>();

        try {
            String query = "SELECT r.* FROM reserva r JOIN cliente c ON r.cliente_id = c.id WHERE c.identificacion = ? ORDER BY r.fecha_inicio DESC";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, clienteId);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                Reserva reserva = crearReservaDesdeResultSet(resultSet);
                listaReservas.add(reserva);
            }

            return listaReservas;
        } catch (SQLException e) {
            throw new SQLException("Error al buscar reservas por cliente: " + e.getMessage(), e);
        } finally {
            if (resultSet != null) resultSet.close();
            if (preparedStatement != null) preparedStatement.close();
        }
    }

    /**
     * Este método permite buscar reservas por el ID del empleado
     * @param empleadoId ID del empleado
     * @return Lista de reservas gestionadas por el empleado
     * @throws SQLException
     */
    public List<Reserva> buscarReservasPorEmpleado(String empleadoId) throws SQLException {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        List<Reserva> listaReservas = new ArrayList<>();

        try {
            String query = "SELECT r.* FROM reserva r JOIN empleado e ON r.empleado_id = e.id JOIN usuario u ON e.id = u.id WHERE u.identificacion = ? ORDER BY r.fecha_inicio DESC";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, empleadoId);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                Reserva reserva = crearReservaDesdeResultSet(resultSet);
                listaReservas.add(reserva);
            }

            return listaReservas;
        } catch (SQLException e) {
            throw new SQLException("Error al buscar reservas por empleado: " + e.getMessage(), e);
        } finally {
            if (resultSet != null) resultSet.close();
            if (preparedStatement != null) preparedStatement.close();
        }
    }

    /**
     * Este método permite buscar reservas por estado
     * @param estado Estado de la reserva
     * @return Lista de reservas con el estado especificado
     * @throws SQLException
     */
    public List<Reserva> buscarReservasPorEstado(EstadoReserva estado) throws SQLException {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        List<Reserva> listaReservas = new ArrayList<>();

        try {
            String query = "SELECT * FROM reserva WHERE estado = ? ORDER BY fecha_inicio DESC";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, estado.name());
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                Reserva reserva = crearReservaDesdeResultSet(resultSet);
                listaReservas.add(reserva);
            }

            return listaReservas;
        } catch (SQLException e) {
            throw new SQLException("Error al buscar reservas por estado: " + e.getMessage(), e);
        } finally {
            if (resultSet != null) resultSet.close();
            if (preparedStatement != null) preparedStatement.close();
        }
    }

    /**
     * Este método permite buscar reservas por rango de fechas
     * @param fechaInicio Fecha de inicio del rango
     * @param fechaFin Fecha de fin del rango
     * @return Lista de reservas dentro del rango de fechas
     * @throws SQLException
     */
    public List<Reserva> buscarReservasPorFechas(LocalDate fechaInicio, LocalDate fechaFin) throws SQLException {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        List<Reserva> listaReservas = new ArrayList<>();

        try {
            String query = "SELECT * FROM reserva WHERE fecha_inicio >= ? AND fecha_fin <= ? ORDER BY fecha_inicio DESC";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setDate(1, Date.valueOf(fechaInicio));
            preparedStatement.setDate(2, Date.valueOf(fechaFin));
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                Reserva reserva = crearReservaDesdeResultSet(resultSet);
                listaReservas.add(reserva);
            }

            return listaReservas;
        } catch (SQLException e) {
            throw new SQLException("Error al buscar reservas por fechas: " + e.getMessage(), e);
        } finally {
            if (resultSet != null) resultSet.close();
            if (preparedStatement != null) preparedStatement.close();
        }
    }

    /**
     * Este método permite crear una nueva reserva
     * @param reserva Reserva a crear
     * @return Respuesta con el resultado de la operación
     * @throws SQLException
     */
    public Respuesta<Reserva> crearReserva(Reserva reserva) throws SQLException {
        PreparedStatement preparedStatement = null;
        ResultSet generatedKeys = null;

        try {
            // Verificar si el cliente existe
            Respuesta<Cliente> clienteRespuesta = clienteDAO.buscarClientePorIdentificacion(reserva.getCliente().getIdentificacion());
            if (!clienteRespuesta.isExito()) {
                return new Respuesta<>(false, "El cliente no existe: " + clienteRespuesta.getMensaje(), null);
            }

            // Verificar si el paquete existe
            if (reserva.getPaqueteTuristico() != null) {
                Respuesta<PaqueteTuristico> paqueteRespuesta = paqueteDAO.buscarPaquetePorId(reserva.getPaqueteTuristico().getId());
                if (!paqueteRespuesta.isExito()) {
                    return new Respuesta<>(false, "El paquete turístico no existe: " + paqueteRespuesta.getMensaje(), null);
                }

                // Verificar disponibilidad del paquete
                PaqueteTuristico paquete = paqueteRespuesta.getData();
                if (paquete.getCuposDisponibles() <= 0) {
                    return new Respuesta<>(false, "No hay cupos disponibles para este paquete", null);
                }
            }

            // Verificar disponibilidad de habitaciones
            if (reserva.getHabitaciones() != null && !reserva.getHabitaciones().isEmpty()) {
                for (Habitacion habitacion : reserva.getHabitaciones()) {
                    if (!verificarDisponibilidadHabitacion(habitacion.getId(), reserva.getFechaInicio(), reserva.getFechaFin())) {
                        return new Respuesta<>(false, "La habitación " + habitacion.getId() + " no está disponible para las fechas seleccionadas", null);
                    }
                }
            }

            // Generar ID único para la reserva
            if (reserva.getId() == null || reserva.getId().trim().isEmpty()) {
                reserva.setId(UUID.randomUUID().toString());
            }

            // Insertar la reserva
            String query = "INSERT INTO reserva (id, cliente_id, empleado_id, paquete_id, fecha_inicio, fecha_fin, precio_total, estado, forma_pago) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

            preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, reserva.getId());
            preparedStatement.setInt(2, obtenerClienteId(reserva.getCliente().getIdentificacion()));

            // El empleado puede ser null si es una reserva online
            if (reserva.getEmpleado() != null) {
                preparedStatement.setInt(3, obtenerEmpleadoId(reserva.getEmpleado().getIdentificacion()));
            } else {
                preparedStatement.setNull(3, Types.INTEGER);
            }

            // El paquete puede ser null si solo se reservan habitaciones
            if (reserva.getPaqueteTuristico() != null) {
                preparedStatement.setInt(4, reserva.getPaqueteTuristico().getId());
            } else {
                preparedStatement.setNull(4, Types.INTEGER);
            }

            preparedStatement.setDate(5, Date.valueOf(reserva.getFechaInicio()));
            preparedStatement.setDate(6, Date.valueOf(reserva.getFechaFin()));
            preparedStatement.setDouble(7, reserva.getPrecioTotal());
            preparedStatement.setString(8, reserva.getEstadoReserva().name());
            preparedStatement.setString(9, reserva.getFormaPago().name());

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                // Guardar relaciones con habitaciones
                if (reserva.getHabitaciones() != null && !reserva.getHabitaciones().isEmpty()) {
                    for (Habitacion habitacion : reserva.getHabitaciones()) {
                        agregarHabitacionAReserva(reserva.getId(), habitacion.getId());
                    }
                }

                // Actualizar disponibilidad del paquete
                if (reserva.getPaqueteTuristico() != null) {
                    actualizarDisponibilidadPaquete(reserva.getPaqueteTuristico().getId(), -1);
                }

                // Actualizar disponibilidad de habitaciones
                if (reserva.getHabitaciones() != null && !reserva.getHabitaciones().isEmpty()) {
                    for (Habitacion habitacion : reserva.getHabitaciones()) {
                        cambiarDisponibilidadHabitacion(habitacion.getId(), false);
                    }
                }

                return new Respuesta<>(true, "Reserva creada exitosamente", reserva);
            } else {
                return new Respuesta<>(false, "No se pudo crear la reserva", null);
            }
        } catch (SQLException e) {
            throw new SQLException("Error al crear la reserva: " + e.getMessage(), e);
        } finally {
            if (generatedKeys != null) generatedKeys.close();
            if (preparedStatement != null) preparedStatement.close();
        }
    }

    /**
     * Este método permite modificar una reserva existente
     * @param reserva Reserva con los datos actualizados
     * @return Respuesta con el resultado de la operación
     * @throws SQLException
     */
    public Respuesta<Reserva> modificarReserva(Reserva reserva) throws SQLException {
        PreparedStatement preparedStatement = null;

        try {
            // Verificar si la reserva existe
            Respuesta<Reserva> reservaRespuesta = buscarReservaPorId(reserva.getId());
            if (!reservaRespuesta.isExito()) {
                return new Respuesta<>(false, "La reserva no existe: " + reservaRespuesta.getMensaje(), null);
            }

            // Obtener la reserva original para comparar cambios
            Reserva reservaOriginal = reservaRespuesta.getData();

            // Verificar si la reserva no está cancelada o completada
            if (reservaOriginal.getEstadoReserva() == EstadoReserva.CANCELADA ||
                    reservaOriginal.getEstadoReserva() == EstadoReserva.COMPLETADA) {
                return new Respuesta<>(false, "No se puede modificar una reserva " + reservaOriginal.getEstadoReserva().name().toLowerCase(), null);
            }

            // Verificar cambios en el paquete turístico
            if (reserva.getPaqueteTuristico() != null &&
                    (reservaOriginal.getPaqueteTuristico() == null ||
                            reserva.getPaqueteTuristico().getId() != reservaOriginal.getPaqueteTuristico().getId())) {

                // Verificar si el nuevo paquete existe y tiene disponibilidad
                Respuesta<PaqueteTuristico> paqueteRespuesta = paqueteDAO.buscarPaquetePorId(reserva.getPaqueteTuristico().getId());
                if (!paqueteRespuesta.isExito()) {
                    return new Respuesta<>(false, "El paquete turístico no existe: " + paqueteRespuesta.getMensaje(), null);
                }

                PaqueteTuristico paquete = paqueteRespuesta.getData();
                if (paquete.getCuposDisponibles() <= 0) {
                    return new Respuesta<>(false, "No hay cupos disponibles para este paquete", null);
                }

                // Liberar el cupo del paquete anterior si existía
                if (reservaOriginal.getPaqueteTuristico() != null) {
                    actualizarDisponibilidadPaquete(reservaOriginal.getPaqueteTuristico().getId(), 1);
                }
            }

            // Verificar cambios en las habitaciones
            List<Habitacion> habitacionesOriginales = reservaOriginal.getHabitaciones();
            List<Habitacion> nuevasHabitaciones = reserva.getHabitaciones();

            // Si hay cambios en las habitaciones, verificar disponibilidad
            if (nuevasHabitaciones != null && !sonListasIguales(habitacionesOriginales, nuevasHabitaciones)) {
                // Verificar disponibilidad de las nuevas habitaciones
                for (Habitacion habitacion : nuevasHabitaciones) {
                    // Solo verificar disponibilidad para habitaciones que no estaban en la reserva original
                    if (!contieneHabitacion(habitacionesOriginales, habitacion) &&
                            !verificarDisponibilidadHabitacion(habitacion.getId(), reserva.getFechaInicio(), reserva.getFechaFin())) {
                        return new Respuesta<>(false, "La habitación " + habitacion.getId() + " no está disponible para las fechas seleccionadas", null);
                    }
                }
            }

            // Actualizar la reserva en la base de datos
            String query = "UPDATE reserva SET cliente_id = ?, empleado_id = ?, paquete_id = ?, " +
                    "fecha_inicio = ?, fecha_fin = ?, precio_total = ?, estado = ?, forma_pago = ? " +
                    "WHERE id = ?";

            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, obtenerClienteId(reserva.getCliente().getIdentificacion()));

            if (reserva.getEmpleado() != null) {
                preparedStatement.setInt(2, obtenerEmpleadoId(reserva.getEmpleado().getIdentificacion()));
            } else {
                preparedStatement.setNull(2, Types.INTEGER);
            }

            if (reserva.getPaqueteTuristico() != null) {
                preparedStatement.setInt(3, reserva.getPaqueteTuristico().getId());
            } else {
                preparedStatement.setNull(3, Types.INTEGER);
            }

            preparedStatement.setDate(4, Date.valueOf(reserva.getFechaInicio()));
            preparedStatement.setDate(5, Date.valueOf(reserva.getFechaFin()));
            preparedStatement.setDouble(6, reserva.getPrecioTotal());
            preparedStatement.setString(7, reserva.getEstadoReserva().name());
            preparedStatement.setString(8, reserva.getFormaPago().name());
            preparedStatement.setString(9, reserva.getId());

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                // Si hay cambios en las habitaciones, actualizar relaciones
                if (nuevasHabitaciones != null && !sonListasIguales(habitacionesOriginales, nuevasHabitaciones)) {
                    // Eliminar todas las relaciones anteriores
                    eliminarHabitacionesDeReserva(reserva.getId());

                    // Liberar las habitaciones anteriores
                    for (Habitacion habitacion : habitacionesOriginales) {
                        cambiarDisponibilidadHabitacion(habitacion.getId(), true);
                    }

                    // Agregar las nuevas habitaciones
                    for (Habitacion habitacion : nuevasHabitaciones) {
                        agregarHabitacionAReserva(reserva.getId(), habitacion.getId());
                        cambiarDisponibilidadHabitacion(habitacion.getId(), false);
                    }
                }

                // Si cambió el paquete turístico, actualizar disponibilidad
                if (reserva.getPaqueteTuristico() != null &&
                        (reservaOriginal.getPaqueteTuristico() == null ||
                                reserva.getPaqueteTuristico().getId() != reservaOriginal.getPaqueteTuristico().getId())) {

                    // Disminuir disponibilidad del nuevo paquete
                    actualizarDisponibilidadPaquete(reserva.getPaqueteTuristico().getId(), -1);
                }

                return new Respuesta<>(true, "Reserva modificada exitosamente", reserva);
            } else {
                return new Respuesta<>(false, "No se pudo modificar la reserva", null);
            }
        } catch (SQLException e) {
            throw new SQLException("Error al modificar la reserva: " + e.getMessage(), e);
        } finally {
            if (preparedStatement != null) preparedStatement.close();
        }
    }

    /**
     * Este método permite cancelar una reserva
     * @param reservaId ID de la reserva a cancelar
     * @return Respuesta con el resultado de la operación
     * @throws SQLException
     */
    public Respuesta<Reserva> cancelarReserva(String reservaId) throws SQLException {
        PreparedStatement preparedStatement = null;

        try {
            // Verificar si la reserva existe
            Respuesta<Reserva> reservaRespuesta = buscarReservaPorId(reservaId);
            if (!reservaRespuesta.isExito()) {
                return new Respuesta<>(false, "La reserva no existe: " + reservaRespuesta.getMensaje(), null);
            }

            Reserva reserva = reservaRespuesta.getData();

            // Verificar si la reserva ya está cancelada o completada
            if (reserva.getEstadoReserva() == EstadoReserva.CANCELADA) {
                return new Respuesta<>(false, "La reserva ya está cancelada", null);
            }

            if (reserva.getEstadoReserva() == EstadoReserva.COMPLETADA) {
                return new Respuesta<>(false, "No se puede cancelar una reserva completada", null);
            }

            // Actualizar el estado de la reserva a CANCELADA
            String query = "UPDATE reserva SET estado = ? WHERE id = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, EstadoReserva.CANCELADA.name());
            preparedStatement.setString(2, reservaId);

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                // Liberar el cupo del paquete si existía
                if (reserva.getPaqueteTuristico() != null) {
                    actualizarDisponibilidadPaquete(reserva.getPaqueteTuristico().getId(), 1);
                }

                // Liberar las habitaciones
                for (Habitacion habitacion : reserva.getHabitaciones()) {
                    cambiarDisponibilidadHabitacion(habitacion.getId(), true);
                }

                // Actualizar el estado en el objeto
                reserva.setEstadoReserva(EstadoReserva.CANCELADA);

                return new Respuesta<>(true, "Reserva cancelada exitosamente", reserva);
            } else {
                return new Respuesta<>(false, "No se pudo cancelar la reserva", null);
            }
        } catch (SQLException e) {
            throw new SQLException("Error al cancelar la reserva: " + e.getMessage(), e);
        } finally {
            if (preparedStatement != null) preparedStatement.close();
        }
    }

    /**
     * Este método permite completar una reserva (cambiar su estado a COMPLETADA)
     * @param reservaId ID de la reserva a completar
     * @return Respuesta con el resultado de la operación
     * @throws SQLException
     */
    public Respuesta<Reserva> completarReserva(String reservaId) throws SQLException {
        PreparedStatement preparedStatement = null;

        try {
            // Verificar si la reserva existe
            Respuesta<Reserva> reservaRespuesta = buscarReservaPorId(reservaId);
            if (!reservaRespuesta.isExito()) {
                return new Respuesta<>(false, "La reserva no existe: " + reservaRespuesta.getMensaje(), null);
            }

            Reserva reserva = reservaRespuesta.getData();

            // Verificar si la reserva ya está cancelada o completada
            if (reserva.getEstadoReserva() == EstadoReserva.CANCELADA) {
                return new Respuesta<>(false, "No se puede completar una reserva cancelada", null);
            }

            if (reserva.getEstadoReserva() == EstadoReserva.COMPLETADA) {
                return new Respuesta<>(false, "La reserva ya está completada", null);
            }

            // Actualizar el estado de la reserva a COMPLETADA
            String query = "UPDATE reserva SET estado = ? WHERE id = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, EstadoReserva.COMPLETADA.name());
            preparedStatement.setString(2, reservaId);

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                // Actualizar el estado en el objeto
                reserva.setEstadoReserva(EstadoReserva.COMPLETADA);

                return new Respuesta<>(true, "Reserva completada exitosamente", reserva);
            } else {
                return new Respuesta<>(false, "No se pudo completar la reserva", null);
            }
        } catch (SQLException e) {
            throw new SQLException("Error al completar la reserva: " + e.getMessage(), e);
        } finally {
            if (preparedStatement != null) preparedStatement.close();
        }
    }

    /**
     * Este método permite confirmar una reserva (cambiar su estado a CONFIRMADA)
     * @param reservaId ID de la reserva a confirmar
     * @return Respuesta con el resultado de la operación
     * @throws SQLException
     */
    public Respuesta<Reserva> confirmarReserva(String reservaId) throws SQLException {
        PreparedStatement preparedStatement = null;

        try {
            // Verificar si la reserva existe
            Respuesta<Reserva> reservaRespuesta = buscarReservaPorId(reservaId);
            if (!reservaRespuesta.isExito()) {
                return new Respuesta<>(false, "La reserva no existe: " + reservaRespuesta.getMensaje(), null);
            }

            Reserva reserva = reservaRespuesta.getData();

            // Verificar si la reserva está en estado PENDIENTE
            if (reserva.getEstadoReserva() != EstadoReserva.PENDIENTE) {
                return new Respuesta<>(false, "Solo se pueden confirmar reservas pendientes", null);
            }

            // Actualizar el estado de la reserva a CONFIRMADA
            String query = "UPDATE reserva SET estado = ? WHERE id = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, EstadoReserva.CONFIRMADA.name());
            preparedStatement.setString(2, reservaId);

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                // Actualizar el estado en el objeto
                reserva.setEstadoReserva(EstadoReserva.CONFIRMADA);

                return new Respuesta<>(true, "Reserva confirmada exitosamente", reserva);
            } else {
                return new Respuesta<>(false, "No se pudo confirmar la reserva", null);
            }
        } catch (SQLException e) {
            throw new SQLException("Error al confirmar la reserva: " + e.getMessage(), e);
        } finally {
            if (preparedStatement != null) preparedStatement.close();
        }
    }

    /**
     * Este método permite buscar una reserva por su ID
     * @param reservaId ID de la reserva a buscar
     * @return Respuesta con el resultado de la operación
     * @throws SQLException
     */
    public Respuesta<Reserva> buscarReservaPorId(String reservaId) throws SQLException {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            String query = "SELECT * FROM reserva WHERE id = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, reservaId);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                Reserva reserva = crearReservaDesdeResultSet(resultSet);
                return new Respuesta<>(true, "Reserva encontrada", reserva);
            } else {
                return new Respuesta<>(false, "No se encontró la reserva con el ID proporcionado", null);
            }
        } catch (SQLException e) {
            throw new SQLException("Error al buscar la reserva: " + e.getMessage(), e);
        } finally {
            if (resultSet != null) resultSet.close();
            if (preparedStatement != null) preparedStatement.close();
        }
    }

    /**
     * Este método permite verificar la disponibilidad de una habitación para un rango de fechas
     * @param habitacionId ID de la habitación
     * @param fechaInicio Fecha de inicio del rango
     * @param fechaFin Fecha de fin del rango
     * @return true si la habitación está disponible, false en caso contrario
     * @throws SQLException
     */
    public boolean verificarDisponibilidadHabitacion(int habitacionId, LocalDate fechaInicio, LocalDate fechaFin) throws SQLException {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            // Primero verificar si la habitación existe y está disponible en general
            String queryDisponible = "SELECT disponible FROM habitacion WHERE id = ?";
            preparedStatement = connection.prepareStatement(queryDisponible);
            preparedStatement.setInt(1, habitacionId);
            resultSet = preparedStatement.executeQuery();

            if (!resultSet.next() || !resultSet.getBoolean("disponible")) {
                return false;
            }

            // Luego verificar si no hay reservas para esa habitación en ese rango de fechas
            String query = "SELECT 1 FROM reserva r " +
                    "JOIN reserva_habitacion rh ON r.id = rh.reserva_id " +
                    "WHERE rh.habitacion_id = ? AND r.estado != 'CANCELADA' " +
                    "AND ((r.fecha_inicio <= ? AND r.fecha_fin >= ?) " + // La reserva existente contiene la nueva fecha
                    "OR (r.fecha_inicio >= ? AND r.fecha_inicio < ?) " + // La nueva reserva contiene el inicio de la existente
                    "OR (r.fecha_fin > ? AND r.fecha_fin <= ?)) " +      // La nueva reserva contiene el fin de la existente
                    "LIMIT 1";

            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, habitacionId);
            preparedStatement.setDate(2, Date.valueOf(fechaFin));      // fin de nueva debe ser después de inicio existente
            preparedStatement.setDate(3, Date.valueOf(fechaInicio));   // inicio de nueva debe ser antes de fin existente
            preparedStatement.setDate(4, Date.valueOf(fechaInicio));   // inicio existente después de inicio nueva
            preparedStatement.setDate(5, Date.valueOf(fechaFin));      // inicio existente antes de fin nueva
            preparedStatement.setDate(6, Date.valueOf(fechaInicio));   // fin existente después de inicio nueva
            preparedStatement.setDate(7, Date.valueOf(fechaFin));      // fin existente antes de fin nueva

            resultSet = preparedStatement.executeQuery();

            // Si hay al menos un resultado, la habitación no está disponible
            return !resultSet.next();
        } catch (SQLException e) {
            throw new SQLException("Error al verificar disponibilidad de habitación: " + e.getMessage(), e);
        } finally {
            if (resultSet != null) resultSet.close();
            if (preparedStatement != null) preparedStatement.close();
        }
    }

    /**
     * Este método permite cambiar la disponibilidad de una habitación
     * @param habitacionId ID de la habitación
     * @param disponible Nuevo estado de disponibilidad
     * @throws SQLException
     */
    private void cambiarDisponibilidadHabitacion(int habitacionId, boolean disponible) throws SQLException {
        PreparedStatement preparedStatement = null;

        try {
            String query = "UPDATE habitacion SET disponible = ? WHERE id = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setBoolean(1, disponible);
            preparedStatement.setInt(2, habitacionId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new SQLException("Error al cambiar disponibilidad de habitación: " + e.getMessage(), e);
        } finally {
            if (preparedStatement != null) preparedStatement.close();
        }
    }

    /**
     * Este método permite actualizar la disponibilidad de un paquete turístico
     * @param paqueteId ID del paquete
     * @param delta Cantidad a modificar (positiva para aumentar, negativa para disminuir)
     * @throws SQLException
     */
    private void actualizarDisponibilidadPaquete(int paqueteId, int delta) throws SQLException {
        PreparedStatement preparedStatement = null;

        try {
            String query = "UPDATE paquete_turistico SET cupos_disponibles = cupos_disponibles + ? WHERE id = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, delta);
            preparedStatement.setInt(2, paqueteId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new SQLException("Error al actualizar disponibilidad de paquete: " + e.getMessage(), e);
        } finally {
            if (preparedStatement != null) preparedStatement.close();
        }
    }

    /**
     * Este método permite agregar una habitación a una reserva
     * @param reservaId ID de la reserva
     * @param habitacionId ID de la habitación
     * @throws SQLException
     */
    private void agregarHabitacionAReserva(String reservaId, int habitacionId) throws SQLException {
        PreparedStatement preparedStatement = null;

        try {
            String query = "INSERT INTO reserva_habitacion (reserva_id, habitacion_id) VALUES (?, ?)";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, reservaId);
            preparedStatement.setInt(2, habitacionId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new SQLException("Error al agregar habitación a la reserva: " + e.getMessage(), e);
        } finally {
            if (preparedStatement != null) preparedStatement.close();
        }
    }

    /**
     * Este método permite eliminar todas las habitaciones de una reserva
     * @param reservaId ID de la reserva
     * @throws SQLException
     */
    private void eliminarHabitacionesDeReserva(String reservaId) throws SQLException {
        PreparedStatement preparedStatement = null;

        try {
            String query = "DELETE FROM reserva_habitacion WHERE reserva_id = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, reservaId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new SQLException("Error al eliminar habitaciones de la reserva: " + e.getMessage(), e);
        } finally {
            if (preparedStatement != null) preparedStatement.close();
        }
    }

    /**
     * Este método verifica si dos listas de habitaciones son iguales
     * @param lista1 Primera lista
     * @param lista2 Segunda lista
     * @return true si son iguales, false en caso contrario
     */
    private boolean sonListasIguales(List<Habitacion> lista1, List<Habitacion> lista2) {
        if (lista1 == null && lista2 == null) {
            return true;
        }

        if (lista1 == null || lista2 == null || lista1.size() != lista2.size()) {
            return false;
        }

        // Verificar si todas las habitaciones de lista1 están en lista2
        for (Habitacion habitacion : lista1) {
            if (!contieneHabitacion(lista2, habitacion)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Este método verifica si una lista de habitaciones contiene una habitación específica
     * @param lista Lista de habitaciones
     * @param habitacion Habitación a buscar
     * @return true si la habitación está en la lista, false en caso contrario
     */
    private boolean contieneHabitacion(List<Habitacion> lista, Habitacion habitacion) {
        if (lista == null || habitacion == null) {
            return false;
        }

        for (Habitacion h : lista) {
            if (h.getId() == habitacion.getId()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Este método obtiene el ID de un cliente a partir de su identificación
     * @param identificacion Identificación del cliente
     * @return ID del cliente en la base de datos
     * @throws SQLException
     */
    private int obtenerClienteId(String identificacion) throws SQLException {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            String query = "SELECT id FROM cliente WHERE identificacion = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, identificacion);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt("id");
            } else {
                throw new SQLException("No se encontró el cliente con identificación: " + identificacion);
            }
        } catch (SQLException e) {
            throw new SQLException("Error al obtener ID del cliente: " + e.getMessage(), e);
        } finally {
            if (resultSet != null) resultSet.close();
            if (preparedStatement != null) preparedStatement.close();
        }
    }

    /**
     * Este método obtiene el ID de un empleado a partir de su identificación
     * @param identificacion Identificación del empleado
     * @return ID del empleado en la base de datos
     * @throws SQLException
     */
    private int obtenerEmpleadoId(String identificacion) throws SQLException {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            String query = "SELECT e.id FROM empleado e JOIN usuario u ON e.id = u.id WHERE u.identificacion = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, identificacion);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt("id");
            } else {
                throw new SQLException("No se encontró el empleado con identificación: " + identificacion);
            }
        } catch (SQLException e) {
            throw new SQLException("Error al obtener ID del empleado: " + e.getMessage(), e);
        } finally {
            if (resultSet != null) resultSet.close();
            if (preparedStatement != null) preparedStatement.close();
        }
    }

    /**
     * Este método crea un objeto Reserva a partir de un ResultSet
     * @param resultSet ResultSet con los datos de la reserva
     * @return Objeto Reserva creado
     * @throws SQLException
     */
    private Reserva crearReservaDesdeResultSet(ResultSet resultSet) throws SQLException {
        Reserva reserva = new Reserva();

        reserva.setId(resultSet.getString("id"));
        reserva.setFechaInicio(resultSet.getDate("fecha_inicio").toLocalDate());
        reserva.setFechaFin(resultSet.getDate("fecha_fin").toLocalDate());
        reserva.setPrecioTotal(resultSet.getDouble("precio_total"));
        reserva.setEstadoReserva(EstadoReserva.valueOf(resultSet.getString("estado")));
        reserva.setFormaPago(FormaPago.valueOf(resultSet.getString("forma_pago")));

        // Cargar el cliente
        int clienteId = resultSet.getInt("cliente_id");
        reserva.setCliente(cargarCliente(clienteId));

        // Cargar el empleado (puede ser null)
        int empleadoId = resultSet.getInt("empleado_id");
        if (!resultSet.wasNull()) {
            reserva.setEmpleado(cargarEmpleado(empleadoId));
        }

        // Cargar el paquete turístico (puede ser null)
        int paqueteId = resultSet.getInt("paquete_id");
        if (!resultSet.wasNull()) {
            reserva.setPaqueteTuristico(cargarPaqueteTuristico(paqueteId));
        }

        // Cargar las habitaciones asociadas
        reserva.setHabitaciones(cargarHabitacionesDeReserva(reserva.getId()));

        return reserva;
    }

    /**
     * Este método carga un objeto Cliente a partir de su ID en la base de datos
     * @param clienteId ID del cliente
     * @return Objeto Cliente
     * @throws SQLException
     */
    private Cliente cargarCliente(int clienteId) throws SQLException {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            String query = "SELECT * FROM cliente WHERE id = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, clienteId);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                Cliente cliente = new Cliente();
                cliente.setNombre(resultSet.getString("nombre"));
                cliente.setApellido(resultSet.getString("apellido"));
                cliente.setIdentificacion(resultSet.getString("identificacion"));
                cliente.setCorreo(resultSet.getString("correo"));
                cliente.setTelefono(resultSet.getString("telefono"));

                if (resultSet.getDate("fecha_nacimiento") != null) {
                    cliente.setFechaNacimiento(resultSet.getDate("fecha_nacimiento").toLocalDate());
                }

                return cliente;
            } else {
                throw new SQLException("No se encontró el cliente con ID: " + clienteId);
            }
        } catch (SQLException e) {
            throw new SQLException("Error al cargar el cliente: " + e.getMessage(), e);
        } finally {
            if (resultSet != null) resultSet.close();
            if (preparedStatement != null) preparedStatement.close();
        }
    }

    /**
     * Este método carga un objeto Empleado a partir de su ID en la base de datos
     * @param empleadoId ID del empleado
     * @return Objeto Empleado
     * @throws SQLException
     */
    private Empleado cargarEmpleado(int empleadoId) throws SQLException {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            String query = "SELECT u.*, e.fecha_contratacion FROM usuario u JOIN empleado e ON u.id = e.id WHERE u.id = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, empleadoId);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                Empleado empleado = new Empleado();
                empleado.setId(empleadoId);
                empleado.setNombre(resultSet.getString("nombre"));
                empleado.setApellido(resultSet.getString("apellido"));
                empleado.setIdentificacion(resultSet.getString("identificacion"));
                empleado.setEmail(resultSet.getString("email"));

                if (resultSet.getDate("fecha_contratacion") != null) {
                    empleado.setFechaContratacion(resultSet.getDate("fecha_contratacion").toLocalDate());
                }

                return empleado;
            } else {
                throw new SQLException("No se encontró el empleado con ID: " + empleadoId);
            }
        } catch (SQLException e) {
            throw new SQLException("Error al cargar el empleado: " + e.getMessage(), e);
        } finally {
            if (resultSet != null) resultSet.close();
            if (preparedStatement != null) preparedStatement.close();
        }
    }

    /**
     * Este método carga un objeto PaqueteTuristico a partir de su ID en la base de datos
     * @param paqueteId ID del paquete
     * @return Objeto PaqueteTuristico
     * @throws SQLException
     */
    private PaqueteTuristico cargarPaqueteTuristico(int paqueteId) throws SQLException {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            String query = "SELECT * FROM paquete_turistico WHERE id = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, paqueteId);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                PaqueteTuristico paquete = new PaqueteTuristico();
                paquete.setId(paqueteId);
                paquete.setNombre(resultSet.getString("nombre"));
                paquete.setDescripcion(resultSet.getString("descripcion"));
                paquete.setPrecioBase(resultSet.getDouble("precio_base"));
                paquete.setDuracionDias(resultSet.getInt("duracion_dias"));

                if (resultSet.getDate("fecha_inicio") != null) {
                    paquete.setFechaInicio(resultSet.getDate("fecha_inicio").toLocalDate());
                }

                if (resultSet.getDate("fecha_fin") != null) {
                    paquete.setFechaFin(resultSet.getDate("fecha_fin").toLocalDate());
                }

                paquete.setCupoMaximo(resultSet.getInt("cupo_maximo"));
                paquete.setCuposDisponibles(resultSet.getInt("cupos_disponibles"));

                return paquete;
            } else {
                throw new SQLException("No se encontró el paquete turístico con ID: " + paqueteId);
            }
        } catch (SQLException e) {
            throw new SQLException("Error al cargar el paquete turístico: " + e.getMessage(), e);
        } finally {
            if (resultSet != null) resultSet.close();
            if (preparedStatement != null) preparedStatement.close();
        }
    }

    /**
     * Este método carga las habitaciones asociadas a una reserva
     * @param reservaId ID de la reserva
     * @return Lista de habitaciones
     * @throws SQLException
     */
    private List<Habitacion> cargarHabitacionesDeReserva(String reservaId) throws SQLException {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        List<Habitacion> habitaciones = new ArrayList<>();

        try {
            String query = "SELECT h.*, th.nombre as tipo_nombre, th.descripcion as tipo_descripcion, th.precio as tipo_precio " +
                    "FROM habitacion h " +
                    "JOIN reserva_habitacion rh ON h.id = rh.habitacion_id " +
                    "JOIN tipo_habitacion th ON h.tipo_habitacion_id = th.id " +
                    "WHERE rh.reserva_id = ?";

            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, reservaId);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                // Crear tipo de habitación
                TipoHabitacion tipoHabitacion = new TipoHabitacion();
                tipoHabitacion.setId(resultSet.getInt("tipo_habitacion_id"));
                tipoHabitacion.setNombre(resultSet.getString("tipo_nombre"));
                tipoHabitacion.setDescripcion(resultSet.getString("tipo_descripcion"));
                tipoHabitacion.setPrecio(resultSet.getDouble("tipo_precio"));

                // Crear habitación
                Habitacion habitacion = new Habitacion();
                habitacion.setId(resultSet.getInt("id"));
                habitacion.setTipoHabitacion(tipoHabitacion);
                habitacion.setCapacidad(resultSet.getInt("capacidad"));
                habitacion.setPrecioPorNoche(resultSet.getDouble("precio_por_noche"));
                habitacion.setDisponible(resultSet.getBoolean("disponible"));

                habitaciones.add(habitacion);
            }

            return habitaciones;
        } catch (SQLException e) {
            throw new SQLException("Error al cargar habitaciones de la reserva: " + e.getMessage(), e);
        } finally {
            if (resultSet != null) resultSet.close();
            if (preparedStatement != null) preparedStatement.close();
        }
    }

    /**
     * Este método calcula el precio total de una reserva basado en el paquete turístico y las habitaciones
     * @param reserva Reserva con los datos para el cálculo
     * @return Precio total calculado
     */
    public double calcularPrecioTotal(Reserva reserva) {
        double precioTotal = 0;

        // Precio del paquete turístico si existe
        if (reserva.getPaqueteTuristico() != null) {
            precioTotal += reserva.getPaqueteTuristico().getPrecioBase();
        }

        // Precio de las habitaciones
        if (reserva.getHabitaciones() != null && !reserva.getHabitaciones().isEmpty()) {
            int diasEstancia = calcularDiasEstancia(reserva.getFechaInicio(), reserva.getFechaFin());

            for (Habitacion habitacion : reserva.getHabitaciones()) {
                precioTotal += habitacion.getPrecioPorNoche() * diasEstancia;
            }
        }

        return precioTotal;
    }

    /**
     * Este método calcula los días de estancia entre dos fechas
     * @param fechaInicio Fecha de inicio
     * @param fechaFin Fecha de fin
     * @return Número de días
     */
    private int calcularDiasEstancia(LocalDate fechaInicio, LocalDate fechaFin) {
        return (int) (fechaFin.toEpochDay() - fechaInicio.toEpochDay());
    }

    /**
     * Este método genera un reporte de ventas para un rango de fechas
     * @param fechaInicio Fecha de inicio del reporte
     * @param fechaFin Fecha de fin del reporte
     * @return Lista de reservas dentro del rango
     * @throws SQLException
     */
    public List<Reserva> generarReporteVentas(LocalDate fechaInicio, LocalDate fechaFin) throws SQLException {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        List<Reserva> reservas = new ArrayList<>();

        try {
            String query = "SELECT * FROM reserva WHERE fecha_inicio >= ? AND fecha_inicio <= ? AND estado != 'CANCELADA' ORDER BY fecha_inicio";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setDate(1, Date.valueOf(fechaInicio));
            preparedStatement.setDate(2, Date.valueOf(fechaFin));
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                Reserva reserva = crearReservaDesdeResultSet(resultSet);
                reservas.add(reserva);
            }

            return reservas;
        } catch (SQLException e) {
            throw new SQLException("Error al generar reporte de ventas: " + e.getMessage(), e);
        } finally {
            if (resultSet != null) resultSet.close();
            if (preparedStatement != null) preparedStatement.close();
        }
    }

    /**
     * Este método calcula el total de ventas para un rango de fechas
     * @param fechaInicio Fecha de inicio
     * @param fechaFin Fecha de fin
     * @return Total de ventas
     * @throws SQLException
     */
    public double calcularTotalVentas(LocalDate fechaInicio, LocalDate fechaFin) throws SQLException {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            String query = "SELECT SUM(precio_total) as total FROM reserva WHERE fecha_inicio >= ? AND fecha_inicio <= ? AND estado != 'CANCELADA'";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setDate(1, Date.valueOf(fechaInicio));
            preparedStatement.setDate(2, Date.valueOf(fechaFin));
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getDouble("total");
            }

            return 0;
        } catch (SQLException e) {
            throw new SQLException("Error al calcular total de ventas: " + e.getMessage(), e);
        } finally {
            if (resultSet != null) resultSet.close();
            if (preparedStatement != null) preparedStatement.close();
        }
    }

    /**
     * Este método genera un reporte de ocupación de habitaciones para un rango de fechas
     * @param fechaInicio Fecha de inicio del reporte
     * @param fechaFin Fecha de fin del reporte
     * @return Mapa con el ID de habitación y la cantidad de días ocupada
     * @throws SQLException
     */
    public List<Habitacion> generarReporteOcupacion(LocalDate fechaInicio, LocalDate fechaFin) throws SQLException {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        List<Habitacion> habitacionesOcupadas = new ArrayList<>();

        try {
            String query = "SELECT h.*, COUNT(rh.reserva_id) as ocupacion, " +
                    "th.nombre as tipo_nombre, th.descripcion as tipo_descripcion, th.precio as tipo_precio " +
                    "FROM habitacion h " +
                    "JOIN tipo_habitacion th ON h.tipo_habitacion_id = th.id " +
                    "LEFT JOIN reserva_habitacion rh ON h.id = rh.habitacion_id " +
                    "LEFT JOIN reserva r ON rh.reserva_id = r.id " +
                    "WHERE (r.fecha_inicio >= ? AND r.fecha_inicio <= ?) OR " +
                    "(r.fecha_fin >= ? AND r.fecha_fin <= ?) OR " +
                    "(r.fecha_inicio <= ? AND r.fecha_fin >= ?) " +
                    "AND r.estado != 'CANCELADA' " +
                    "GROUP BY h.id " +
                    "ORDER BY ocupacion DESC";

            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setDate(1, Date.valueOf(fechaInicio));
            preparedStatement.setDate(2, Date.valueOf(fechaFin));
            preparedStatement.setDate(3, Date.valueOf(fechaInicio));
            preparedStatement.setDate(4, Date.valueOf(fechaFin));
            preparedStatement.setDate(5, Date.valueOf(fechaInicio));
            preparedStatement.setDate(6, Date.valueOf(fechaFin));
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                // Crear tipo de habitación
                TipoHabitacion tipoHabitacion = new TipoHabitacion();
                tipoHabitacion.setId(resultSet.getInt("tipo_habitacion_id"));
                tipoHabitacion.setNombre(resultSet.getString("tipo_nombre"));
                tipoHabitacion.setDescripcion(resultSet.getString("tipo_descripcion"));
                tipoHabitacion.setPrecio(resultSet.getDouble("tipo_precio"));

                // Crear habitación
                Habitacion habitacion = new Habitacion();
                habitacion.setId(resultSet.getInt("id"));
                habitacion.setTipoHabitacion(tipoHabitacion);
                habitacion.setCapacidad(resultSet.getInt("capacidad"));
                habitacion.setPrecioPorNoche(resultSet.getDouble("precio_por_noche"));
                habitacion.setDisponible(resultSet.getBoolean("disponible"));

                habitacionesOcupadas.add(habitacion);
            }

            return habitacionesOcupadas;
        } catch (SQLException e) {
            throw new SQLException("Error al generar reporte de ocupación: " + e.getMessage(), e);
        } finally {
            if (resultSet != null) resultSet.close();
            if (preparedStatement != null) preparedStatement.close();
        }
    }

    /**
     * Este método genera un reporte de los paquetes más vendidos
     * @param limite Cantidad máxima de paquetes a retornar
     * @return Lista de paquetes más vendidos con su cantidad
     * @throws SQLException
     */
    public List<Object[]> reportePaquetesMasVendidos(int limite) throws SQLException {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        List<Object[]> paquetesVendidos = new ArrayList<>();

        try {
            String query = "SELECT p.id, p.nombre, COUNT(r.id) as cantidad " +
                    "FROM paquete_turistico p " +
                    "JOIN reserva r ON p.id = r.paquete_id " +
                    "WHERE r.estado != 'CANCELADA' " +
                    "GROUP BY p.id, p.nombre " +
                    "ORDER BY cantidad DESC " +
                    "LIMIT ?";

            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, limite);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                Object[] paqueteInfo = new Object[3];
                paqueteInfo[0] = resultSet.getInt("id");
                paqueteInfo[1] = resultSet.getString("nombre");
                paqueteInfo[2] = resultSet.getInt("cantidad");
                paquetesVendidos.add(paqueteInfo);
            }

            return paquetesVendidos;
        } catch (SQLException e) {
            throw new SQLException("Error al generar reporte de paquetes más vendidos: " + e.getMessage(), e);
        } finally {
            if (resultSet != null) resultSet.close();
            if (preparedStatement != null) preparedStatement.close();
        }
    }

    /**
     * Este método genera un reporte de los clientes más frecuentes
     * @param limite Cantidad máxima de clientes a retornar
     * @return Lista de clientes más frecuentes con su cantidad de reservas
     * @throws SQLException
     */
    public List<Object[]> reporteClientesFrecuentes(int limite) throws SQLException {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        List<Object[]> clientesFrecuentes = new ArrayList<>();

        try {
            String query = "SELECT c.identificacion, c.nombre, c.apellido, COUNT(r.id) as cantidad " +
                    "FROM cliente c " +
                    "JOIN reserva r ON c.id = r.cliente_id " +
                    "WHERE r.estado != 'CANCELADA' " +
                    "GROUP BY c.id, c.identificacion, c.nombre, c.apellido " +
                    "ORDER BY cantidad DESC " +
                    "LIMIT ?";

            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, limite);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                Object[] clienteInfo = new Object[4];
                clienteInfo[0] = resultSet.getString("identificacion");
                clienteInfo[1] = resultSet.getString("nombre");
                clienteInfo[2] = resultSet.getString("apellido");
                clienteInfo[3] = resultSet.getInt("cantidad");
                clientesFrecuentes.add(clienteInfo);
            }

            return clientesFrecuentes;
        } catch (SQLException e) {
            throw new SQLException("Error al generar reporte de clientes frecuentes: " + e.getMessage(), e);
        } finally {
            if (resultSet != null) resultSet.close();
            if (preparedStatement != null) preparedStatement.close();
        }
    }
}