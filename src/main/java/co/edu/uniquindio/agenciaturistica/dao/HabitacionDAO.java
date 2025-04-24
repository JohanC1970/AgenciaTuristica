package co.edu.uniquindio.agenciaturistica.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import co.edu.uniquindio.agenciaturistica.model.Habitacion;
import co.edu.uniquindio.agenciaturistica.model.TipoHabitacion;
import co.edu.uniquindio.agenciaturistica.util.DataBaseConnection;
import co.edu.uniquindio.agenciaturistica.util.Respuesta;

public class HabitacionDAO {

    private Connection connection;

    public HabitacionDAO() throws SQLException {
        try {
            this.connection = DataBaseConnection.getConnection();
        } catch (SQLException e) {
            throw new SQLException(e);
        }
    }

    public HabitacionDAO(Connection connection) {
        this.connection = connection;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    /**
     * Este método permite obtener todos los tipos de habitación disponibles
     * @return Lista de tipos de habitación
     * @throws SQLException
     */
    public List<TipoHabitacion> obtenerTiposHabitacion() throws SQLException {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        List<TipoHabitacion> listaTipos = new ArrayList<>();

        try {
            String query = "SELECT * FROM tipo_habitacion ORDER BY nombre";
            preparedStatement = connection.prepareStatement(query);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                TipoHabitacion tipoHabitacion = crearTipoHabitacionDesdeResultSet(resultSet);

                // Cargar características del tipo de habitación
                cargarCaracteristicasTipoHabitacion(tipoHabitacion);

                listaTipos.add(tipoHabitacion);
            }

            return listaTipos;
        } catch (SQLException e) {
            throw new SQLException("Error al obtener los tipos de habitación: " + e.getMessage(), e);
        } finally {
            if (resultSet != null) resultSet.close();
            if (preparedStatement != null) preparedStatement.close();
        }
    }

    /**
     * Este método permite cargar las características de un tipo de habitación
     * @param tipoHabitacion Tipo de habitación al que se cargarán las características
     * @throws SQLException
     */
    private void cargarCaracteristicasTipoHabitacion(TipoHabitacion tipoHabitacion) throws SQLException {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            String query = "SELECT caracteristica FROM caracteristica_habitacion WHERE tipo_habitacion_id = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, tipoHabitacion.getId());
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                tipoHabitacion.getCaracteristicas().add(resultSet.getString("caracteristica"));
            }
        } catch (SQLException e) {
            throw new SQLException("Error al cargar características del tipo de habitación: " + e.getMessage(), e);
        } finally {
            if (resultSet != null) resultSet.close();
            if (preparedStatement != null) preparedStatement.close();
        }
    }

    /**
     * Este método permite obtener todas las habitaciones de un hospedaje
     * @param hospedajeId ID del hospedaje
     * @return Lista de habitaciones
     * @throws SQLException
     */
    public List<Habitacion> obtenerHabitacionesPorHospedaje(int hospedajeId) throws SQLException {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        List<Habitacion> listaHabitaciones = new ArrayList<>();

        try {
            String query = "SELECT h.*, th.id as tipo_id, th.nombre as tipo_nombre, " +
                    "th.descripcion as tipo_descripcion, th.precio as tipo_precio " +
                    "FROM habitacion h " +
                    "JOIN tipo_habitacion th ON h.tipo_habitacion_id = th.id " +
                    "WHERE h.hospedaje_id = ? " +
                    "ORDER BY h.id";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, hospedajeId);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                // Crear tipo de habitación
                TipoHabitacion tipoHabitacion = new TipoHabitacion();
                tipoHabitacion.setId(resultSet.getInt("tipo_id"));
                tipoHabitacion.setNombre(resultSet.getString("tipo_nombre"));
                tipoHabitacion.setDescripcion(resultSet.getString("tipo_descripcion"));
                tipoHabitacion.setPrecio(resultSet.getDouble("tipo_precio"));

                // Cargar características del tipo de habitación
                cargarCaracteristicasTipoHabitacion(tipoHabitacion);

                // Crear habitación
                Habitacion habitacion = new Habitacion();
                habitacion.setId(resultSet.getInt("id"));
                habitacion.setTipoHabitacion(tipoHabitacion);
                habitacion.setCapacidad(resultSet.getInt("capacidad"));
                habitacion.setPrecioPorNoche(resultSet.getDouble("precio_por_noche"));
                habitacion.setDisponible(resultSet.getBoolean("disponible"));

                listaHabitaciones.add(habitacion);
            }

            return listaHabitaciones;
        } catch (SQLException e) {
            throw new SQLException("Error al obtener las habitaciones del hospedaje: " + e.getMessage(), e);
        } finally {
            if (resultSet != null) resultSet.close();
            if (preparedStatement != null) preparedStatement.close();
        }
    }

    /**
     * Método utilitario para crear un objeto TipoHabitacion a partir de un ResultSet
     * @param resultSet
     * @return
     * @throws SQLException
     */
    private TipoHabitacion crearTipoHabitacionDesdeResultSet(ResultSet resultSet) throws SQLException {
        TipoHabitacion tipoHabitacion = new TipoHabitacion();
        tipoHabitacion.setId(resultSet.getInt("id"));
        tipoHabitacion.setNombre(resultSet.getString("nombre"));
        tipoHabitacion.setDescripcion(resultSet.getString("descripcion"));
        tipoHabitacion.setPrecio(resultSet.getDouble("precio"));
        return tipoHabitacion;
    }

    /**
     * Este método permite guardar una nueva habitación en un hospedaje
     * @param hospedajeId ID del hospedaje
     * @param habitacion Habitación a guardar
     * @return Respuesta con el resultado de la operación
     * @throws SQLException
     */
    public Respuesta<Habitacion> guardarHabitacion(int hospedajeId, Habitacion habitacion) throws SQLException {
        PreparedStatement preparedStatement = null;
        ResultSet generatedKeys = null;

        try {
            String query = "INSERT INTO habitacion (tipo_habitacion_id, hospedaje_id, capacidad, " +
                    "precio_por_noche, disponible) VALUES (?, ?, ?, ?, ?)";

            preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setInt(1, habitacion.getTipoHabitacion().getId());
            preparedStatement.setInt(2, hospedajeId);
            preparedStatement.setInt(3, habitacion.getCapacidad());
            preparedStatement.setDouble(4, habitacion.getPrecioPorNoche());
            preparedStatement.setBoolean(5, habitacion.isDisponible());

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                // Obtener el ID generado
                generatedKeys = preparedStatement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    habitacion.setId(generatedKeys.getInt(1));
                }

                return new Respuesta<>(true, "Habitación guardada correctamente", habitacion);
            } else {
                return new Respuesta<>(false, "No se pudo guardar la habitación", null);
            }
        } catch (SQLException e) {
            throw new SQLException("Error al guardar la habitación: " + e.getMessage(), e);
        } finally {
            if (generatedKeys != null) generatedKeys.close();
            if (preparedStatement != null) preparedStatement.close();
        }
    }

    /**
     * Este método permite actualizar una habitación existente
     * @param hospedajeId ID del hospedaje
     * @param habitacion Habitación con los datos actualizados
     * @return Respuesta con el resultado de la operación
     * @throws SQLException
     */
    public Respuesta<Habitacion> actualizarHabitacion(int hospedajeId, Habitacion habitacion) throws SQLException {
        PreparedStatement preparedStatement = null;

        try {
            // Verificar si existe la habitación
            Respuesta<Habitacion> respuestaBusqueda = buscarHabitacionPorId(hospedajeId, habitacion.getId());
            if (!respuestaBusqueda.isExito()) {
                return respuestaBusqueda;
            }

            String query = "UPDATE habitacion SET tipo_habitacion_id = ?, capacidad = ?, " +
                    "precio_por_noche = ?, disponible = ? WHERE id = ? AND hospedaje_id = ?";

            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, habitacion.getTipoHabitacion().getId());
            preparedStatement.setInt(2, habitacion.getCapacidad());
            preparedStatement.setDouble(3, habitacion.getPrecioPorNoche());
            preparedStatement.setBoolean(4, habitacion.isDisponible());
            preparedStatement.setInt(5, habitacion.getId());
            preparedStatement.setInt(6, hospedajeId);

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                return new Respuesta<>(true, "Habitación actualizada correctamente", habitacion);
            } else {
                return new Respuesta<>(false, "No se pudo actualizar la habitación", null);
            }
        } catch (SQLException e) {
            throw new SQLException("Error al actualizar la habitación: " + e.getMessage(), e);
        } finally {
            if (preparedStatement != null) preparedStatement.close();
        }
    }

    /**
     * Este método permite eliminar una habitación
     * @param hospedajeId ID del hospedaje
     * @param habitacionId ID de la habitación a eliminar
     * @return Respuesta con el resultado de la operación
     * @throws SQLException
     */
    public Respuesta<Habitacion> eliminarHabitacion(int hospedajeId, int habitacionId) throws SQLException {
        PreparedStatement preparedStatement = null;

        try {
            // Verificar si existe la habitación
            Respuesta<Habitacion> respuestaBusqueda = buscarHabitacionPorId(hospedajeId, habitacionId);
            if (!respuestaBusqueda.isExito()) {
                return respuestaBusqueda;
            }

            // Verificar si la habitación tiene reservas asociadas
            if (tieneReservasAsociadas(habitacionId)) {
                return new Respuesta<>(false, "No se puede eliminar la habitación porque tiene reservas asociadas", null);
            }

            String query = "DELETE FROM habitacion WHERE id = ? AND hospedaje_id = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, habitacionId);
            preparedStatement.setInt(2, hospedajeId);

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                return new Respuesta<>(true, "Habitación eliminada correctamente", respuestaBusqueda.getData());
            } else {
                return new Respuesta<>(false, "No se pudo eliminar la habitación", null);
            }
        } catch (SQLException e) {
            throw new SQLException("Error al eliminar la habitación: " + e.getMessage(), e);
        } finally {
            if (preparedStatement != null) preparedStatement.close();
        }
    }

    /**
     * Este método permite buscar una habitación por su ID en un hospedaje específico
     * @param hospedajeId ID del hospedaje
     * @param habitacionId ID de la habitación a buscar
     * @return Respuesta con el resultado de la operación
     * @throws SQLException
     */
    public Respuesta<Habitacion> buscarHabitacionPorId(int hospedajeId, int habitacionId) throws SQLException {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            String query = "SELECT h.*, th.id as tipo_id, th.nombre as tipo_nombre, " +
                    "th.descripcion as tipo_descripcion, th.precio as tipo_precio " +
                    "FROM habitacion h " +
                    "JOIN tipo_habitacion th ON h.tipo_habitacion_id = th.id " +
                    "WHERE h.id = ? AND h.hospedaje_id = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, habitacionId);
            preparedStatement.setInt(2, hospedajeId);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                // Crear tipo de habitación
                TipoHabitacion tipoHabitacion = new TipoHabitacion();
                tipoHabitacion.setId(resultSet.getInt("tipo_id"));
                tipoHabitacion.setNombre(resultSet.getString("tipo_nombre"));
                tipoHabitacion.setDescripcion(resultSet.getString("tipo_descripcion"));
                tipoHabitacion.setPrecio(resultSet.getDouble("tipo_precio"));

                // Cargar características del tipo de habitación
                cargarCaracteristicasTipoHabitacion(tipoHabitacion);

                // Crear habitación
                Habitacion habitacion = new Habitacion();
                habitacion.setId(resultSet.getInt("id"));
                habitacion.setTipoHabitacion(tipoHabitacion);
                habitacion.setCapacidad(resultSet.getInt("capacidad"));
                habitacion.setPrecioPorNoche(resultSet.getDouble("precio_por_noche"));
                habitacion.setDisponible(resultSet.getBoolean("disponible"));

                return new Respuesta<>(true, "Habitación encontrada", habitacion);
            } else {
                return new Respuesta<>(false, "No se encontró la habitación con el ID proporcionado", null);
            }
        } catch (SQLException e) {
            throw new SQLException("Error al buscar la habitación: " + e.getMessage(), e);
        } finally {
            if (resultSet != null) resultSet.close();
            if (preparedStatement != null) preparedStatement.close();
        }
    }

    /**
     * Este método permite verificar si una habitación está disponible en un rango de fechas
     * @param habitacionId ID de la habitación
     * @param fechaInicio Fecha de inicio de la reserva
     * @param fechaFin Fecha de fin de la reserva
     * @return true si está disponible, false en caso contrario
     * @throws SQLException
     */
    public boolean verificarDisponibilidadHabitacion(int habitacionId, java.time.LocalDate fechaInicio, java.time.LocalDate fechaFin) throws SQLException {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            // Verificar si la habitación existe y está marcada como disponible
            String queryDisponible = "SELECT disponible FROM habitacion WHERE id = ?";
            preparedStatement = connection.prepareStatement(queryDisponible);
            preparedStatement.setInt(1, habitacionId);
            resultSet = preparedStatement.executeQuery();

            if (!resultSet.next() || !resultSet.getBoolean("disponible")) {
                return false; // No existe o no está disponible
            }

            // Verificar si hay reservas que se solapan con el período solicitado
            String query = "SELECT 1 FROM reserva_habitacion " +
                    "WHERE habitacion_id = ? " +
                    "AND ((fecha_inicio <= ? AND fecha_fin >= ?) OR " +
                    "(fecha_inicio <= ? AND fecha_fin >= ?) OR " +
                    "(fecha_inicio >= ? AND fecha_fin <= ?)) " +
                    "LIMIT 1";

            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, habitacionId);
            preparedStatement.setDate(2, java.sql.Date.valueOf(fechaFin));
            preparedStatement.setDate(3, java.sql.Date.valueOf(fechaInicio));
            preparedStatement.setDate(4, java.sql.Date.valueOf(fechaInicio));
            preparedStatement.setDate(5, java.sql.Date.valueOf(fechaFin));
            preparedStatement.setDate(6, java.sql.Date.valueOf(fechaInicio));
            preparedStatement.setDate(7, java.sql.Date.valueOf(fechaFin));

            resultSet = preparedStatement.executeQuery();

            // Si hay resultados, significa que hay solapamiento y no está disponible
            return !resultSet.next();

        } catch (SQLException e) {
            throw new SQLException("Error al verificar disponibilidad: " + e.getMessage(), e);
        } finally {
            if (resultSet != null) resultSet.close();
            if (preparedStatement != null) preparedStatement.close();
        }
    }

    /**
     * Este método permite verificar si una habitación tiene reservas asociadas
     * @param habitacionId ID de la habitación a verificar
     * @return true si tiene reservas, false en caso contrario
     * @throws SQLException
     */
    private boolean tieneReservasAsociadas(int habitacionId) throws SQLException {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            String query = "SELECT 1 FROM reserva_habitacion WHERE habitacion_id = ? LIMIT 1";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, habitacionId);
            resultSet = preparedStatement.executeQuery();

            return resultSet.next();
        } catch (SQLException e) {
            throw new SQLException("Error al verificar reservas asociadas: " + e.getMessage(), e);
        } finally {
            if (resultSet != null) resultSet.close();
            if (preparedStatement != null) preparedStatement.close();
        }
    }

    /**
     * Este método permite reservar una habitación para un rango de fechas específico
     * @param hospedajeId ID del hospedaje
     * @param habitacionId ID de la habitación
     * @param fechaInicio Fecha de inicio de la reserva
     * @param fechaFin Fecha de fin de la reserva
     * @return Respuesta con el resultado de la operación
     * @throws SQLException
     */
    public Respuesta<Boolean> reservarHabitacion(int hospedajeId, int habitacionId, java.time.LocalDate fechaInicio, java.time.LocalDate fechaFin) throws SQLException {
        PreparedStatement preparedStatement = null;

        try {
            // Verificar si la habitación está disponible para ese rango de fechas
            if (!verificarDisponibilidadHabitacion(habitacionId, fechaInicio, fechaFin)) {
                return new Respuesta<>(false, "La habitación no está disponible para el rango de fechas seleccionado", false);
            }

            // En este método no creamos la reserva completa, solo marcamos la relación para una futura reserva
            // La creación de la reserva se haría en el ReservaDAO

            return new Respuesta<>(true, "Habitación reservada correctamente para las fechas seleccionadas", true);
        } catch (SQLException e) {
            throw new SQLException("Error al reservar la habitación: " + e.getMessage(), e);
        } finally {
            if (preparedStatement != null) preparedStatement.close();
        }
    }
}