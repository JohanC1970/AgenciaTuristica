package co.edu.uniquindio.agenciaturistica.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import co.edu.uniquindio.agenciaturistica.model.Actividad;
import co.edu.uniquindio.agenciaturistica.util.DataBaseConnection;
import co.edu.uniquindio.agenciaturistica.util.Respuesta;

public class ActividadDAO {

    private Connection connection;

    public ActividadDAO() throws SQLException {
        try {
            this.connection = DataBaseConnection.getConnection();
        } catch (SQLException e) {
            throw new SQLException(e);
        }
    }

    public ActividadDAO(Connection connection) {
        this.connection = connection;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    /**
     * Este método permite obtener todas las actividades
     * @return Lista de actividades
     * @throws SQLException
     */
    public List<Actividad> obtenerTodasLasActividades() throws SQLException {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        List<Actividad> listaActividades = new ArrayList<>();

        try {
            String query = "SELECT * FROM actividad ORDER BY nombre";
            preparedStatement = connection.prepareStatement(query);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                Actividad actividad = crearActividadDesdeResultSet(resultSet);
                listaActividades.add(actividad);
            }

            return listaActividades;
        } catch (SQLException e) {
            throw new SQLException("Error al obtener las actividades: " + e.getMessage(), e);
        } finally {
            if (resultSet != null) resultSet.close();
            if (preparedStatement != null) preparedStatement.close();
        }
    }

    /**
     * Este método permite guardar una nueva actividad
     * @param actividad Actividad a guardar
     * @return Respuesta con el resultado de la operación
     * @throws SQLException
     */
    public Respuesta<Actividad> guardarActividad(Actividad actividad) throws SQLException {
        PreparedStatement preparedStatement = null;
        ResultSet generatedKeys = null;

        try {
            String query = "INSERT INTO actividad (nombre, descripcion, ubicacion, precio, duracion, " +
                    "cupo_maximo, cupos_disponibles, fecha_inicio, fecha_fin) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

            // Calcular fecha fin sumando la duración a la fecha inicio
            LocalDateTime fechaFin = null;
            if (actividad.getFechaInicio() != null) {
                fechaFin = actividad.getFechaInicio().plusHours(actividad.getDuracion());
            }

            preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, actividad.getNombre());
            preparedStatement.setString(2, actividad.getDescripcion());
            preparedStatement.setString(3, actividad.getUbicacion());
            preparedStatement.setDouble(4, actividad.getPrecio());
            preparedStatement.setInt(5, actividad.getDuracion());
            preparedStatement.setInt(6, actividad.getCupoMaximo());
            preparedStatement.setInt(7, actividad.getCuposDisponibles());
            preparedStatement.setTimestamp(8, actividad.getFechaInicio() != null ?
                    Timestamp.valueOf(actividad.getFechaInicio()) : null);
            preparedStatement.setTimestamp(9, fechaFin != null ? Timestamp.valueOf(fechaFin) : null);

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                // Obtener el ID generado
                generatedKeys = preparedStatement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    actividad.setId(generatedKeys.getInt(1));
                }

                // Establecer la fecha fin calculada
                actividad.setFechaFin(fechaFin);

                return new Respuesta<>(true, "Actividad guardada correctamente", actividad);
            } else {
                return new Respuesta<>(false, "No se pudo guardar la actividad", null);
            }
        } catch (SQLException e) {
            throw new SQLException("Error al guardar la actividad: " + e.getMessage(), e);
        } finally {
            if (generatedKeys != null) generatedKeys.close();
            if (preparedStatement != null) preparedStatement.close();
        }
    }

    /**
     * Este método permite actualizar una actividad existente
     * @param actividad Actividad con los datos actualizados
     * @return Respuesta con el resultado de la operación
     * @throws SQLException
     */
    public Respuesta<Actividad> actualizarActividad(Actividad actividad) throws SQLException {
        PreparedStatement preparedStatement = null;

        try {
            // Verificar si existe la actividad
            Respuesta<Actividad> respuestaBusqueda = buscarActividadPorId(actividad.getId());
            if (!respuestaBusqueda.isExito()) {
                return respuestaBusqueda;
            }

            // Calcular fecha fin sumando la duración a la fecha inicio
            LocalDateTime fechaFin = null;
            if (actividad.getFechaInicio() != null) {
                fechaFin = actividad.getFechaInicio().plusHours(actividad.getDuracion());
            }

            String query = "UPDATE actividad SET nombre = ?, descripcion = ?, ubicacion = ?, " +
                    "precio = ?, duracion = ?, cupo_maximo = ?, cupos_disponibles = ?, " +
                    "fecha_inicio = ?, fecha_fin = ? WHERE id = ?";

            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, actividad.getNombre());
            preparedStatement.setString(2, actividad.getDescripcion());
            preparedStatement.setString(3, actividad.getUbicacion());
            preparedStatement.setDouble(4, actividad.getPrecio());
            preparedStatement.setInt(5, actividad.getDuracion());
            preparedStatement.setInt(6, actividad.getCupoMaximo());
            preparedStatement.setInt(7, actividad.getCuposDisponibles());
            preparedStatement.setTimestamp(8, actividad.getFechaInicio() != null ?
                    Timestamp.valueOf(actividad.getFechaInicio()) : null);
            preparedStatement.setTimestamp(9, fechaFin != null ? Timestamp.valueOf(fechaFin) : null);
            preparedStatement.setInt(10, actividad.getId());

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                // Establecer la fecha fin calculada
                actividad.setFechaFin(fechaFin);

                return new Respuesta<>(true, "Actividad actualizada correctamente", actividad);
            } else {
                return new Respuesta<>(false, "No se pudo actualizar la actividad", null);
            }
        } catch (SQLException e) {
            throw new SQLException("Error al actualizar la actividad: " + e.getMessage(), e);
        } finally {
            if (preparedStatement != null) preparedStatement.close();
        }
    }

    /**
     * Este método permite eliminar una actividad
     * @param id ID de la actividad a eliminar
     * @return Respuesta con el resultado de la operación
     * @throws SQLException
     */
    public Respuesta<Actividad> eliminarActividad(int id) throws SQLException {
        PreparedStatement preparedStatement = null;

        try {
            // Verificar si existe la actividad
            Respuesta<Actividad> respuestaBusqueda = buscarActividadPorId(id);
            if (!respuestaBusqueda.isExito()) {
                return respuestaBusqueda;
            }

            // Verificar si la actividad tiene paquetes asociados
            if (tienePaquetesAsociados(id)) {
                return new Respuesta<>(false, "No se puede eliminar la actividad porque está asociada a uno o más paquetes", null);
            }

            // Eliminar la actividad
            String query = "DELETE FROM actividad WHERE id = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, id);

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                return new Respuesta<>(true, "Actividad eliminada correctamente", respuestaBusqueda.getData());
            } else {
                return new Respuesta<>(false, "No se pudo eliminar la actividad", null);
            }
        } catch (SQLException e) {
            throw new SQLException("Error al eliminar la actividad: " + e.getMessage(), e);
        } finally {
            if (preparedStatement != null) preparedStatement.close();
        }
    }

    /**
     * Este método permite buscar una actividad por su ID
     * @param id ID de la actividad a buscar
     * @return Respuesta con el resultado de la operación
     * @throws SQLException
     */
    public Respuesta<Actividad> buscarActividadPorId(int id) throws SQLException {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            String query = "SELECT * FROM actividad WHERE id = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, id);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                Actividad actividad = crearActividadDesdeResultSet(resultSet);
                return new Respuesta<>(true, "Actividad encontrada", actividad);
            } else {
                return new Respuesta<>(false, "No se encontró la actividad con el ID proporcionado", null);
            }
        } catch (SQLException e) {
            throw new SQLException("Error al buscar la actividad: " + e.getMessage(), e);
        } finally {
            if (resultSet != null) resultSet.close();
            if (preparedStatement != null) preparedStatement.close();
        }
    }

    /**
     * Este método permite verificar si una actividad tiene paquetes asociados
     * @param actividadId ID de la actividad a verificar
     * @return true si tiene paquetes asociados, false en caso contrario
     * @throws SQLException
     */
    private boolean tienePaquetesAsociados(int actividadId) throws SQLException {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            String query = "SELECT 1 FROM paquete_actividad WHERE actividad_id = ? LIMIT 1";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, actividadId);
            resultSet = preparedStatement.executeQuery();

            return resultSet.next();
        } catch (SQLException e) {
            throw new SQLException("Error al verificar paquetes asociados: " + e.getMessage(), e);
        } finally {
            if (resultSet != null) resultSet.close();
            if (preparedStatement != null) preparedStatement.close();
        }
    }

    /**
     * Método utilitario para crear un objeto Actividad a partir de un ResultSet
     * @param resultSet
     * @return
     * @throws SQLException
     */
    private Actividad crearActividadDesdeResultSet(ResultSet resultSet) throws SQLException {
        Actividad actividad = new Actividad();
        actividad.setId(resultSet.getInt("id"));
        actividad.setNombre(resultSet.getString("nombre"));
        actividad.setDescripcion(resultSet.getString("descripcion"));
        actividad.setUbicacion(resultSet.getString("ubicacion"));
        actividad.setPrecio(resultSet.getDouble("precio"));
        actividad.setDuracion(resultSet.getInt("duracion"));
        actividad.setCupoMaximo(resultSet.getInt("cupo_maximo"));
        actividad.setCuposDisponibles(resultSet.getInt("cupos_disponibles"));

        if (resultSet.getTimestamp("fecha_inicio") != null) {
            actividad.setFechaInicio(resultSet.getTimestamp("fecha_inicio").toLocalDateTime());
        }

        if (resultSet.getTimestamp("fecha_fin") != null) {
            actividad.setFechaFin(resultSet.getTimestamp("fecha_fin").toLocalDateTime());
        }

        return actividad;
    }
}
