package co.edu.uniquindio.agenciaturistica.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import co.edu.uniquindio.agenciaturistica.model.Habitacion;
import co.edu.uniquindio.agenciaturistica.model.Hospedaje;
import co.edu.uniquindio.agenciaturistica.model.TipoHabitacion;
import co.edu.uniquindio.agenciaturistica.util.DataBaseConnection;
import co.edu.uniquindio.agenciaturistica.util.Respuesta;

public class HospedajeDAO {

    private Connection connection;

    public HospedajeDAO() throws SQLException {
        try {
            this.connection = DataBaseConnection.getConnection();
        } catch (SQLException e) {
            throw new SQLException(e);
        }
    }

    public HospedajeDAO(Connection connection) {
        this.connection = connection;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    /**
     * Este método permite obtener todos los hospedajes
     * @return Lista de hospedajes
     * @throws SQLException
     */
    public List<Hospedaje> obtenerTodosLosHospedajes() throws SQLException {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        List<Hospedaje> listaHospedajes = new ArrayList<>();

        try {
            String query = "SELECT * FROM hospedaje ORDER BY nombre";
            preparedStatement = connection.prepareStatement(query);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                Hospedaje hospedaje = crearHospedajeDesdeResultSet(resultSet);

                // Cargar habitaciones asociadas al hospedaje
                cargarHabitacionesHospedaje(hospedaje);

                listaHospedajes.add(hospedaje);
            }

            return listaHospedajes;
        } catch (SQLException e) {
            throw new SQLException("Error al obtener los hospedajes: " + e.getMessage(), e);
        } finally {
            if (resultSet != null) resultSet.close();
            if (preparedStatement != null) preparedStatement.close();
        }
    }

    /**
     * Este método permite guardar un nuevo hospedaje
     * @param hospedaje Hospedaje a guardar
     * @return Respuesta con el resultado de la operación
     * @throws SQLException
     */
    public Respuesta<Hospedaje> guardarHospedaje(Hospedaje hospedaje) throws SQLException {
        PreparedStatement preparedStatement = null;
        ResultSet generatedKeys = null;

        try {
            String query = "INSERT INTO hospedaje (nombre, direccion, ciudad, telefono, estrellas, descripcion) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";

            preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, hospedaje.getNombre());
            preparedStatement.setString(2, hospedaje.getDireccion());
            preparedStatement.setString(3, hospedaje.getCiudad());
            preparedStatement.setString(4, hospedaje.getTelefono());
            preparedStatement.setInt(5, hospedaje.getEstrellas());
            preparedStatement.setString(6, hospedaje.getDescripcion());

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                // Obtener el ID generado
                generatedKeys = preparedStatement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    hospedaje.setId(generatedKeys.getInt(1));
                }

                return new Respuesta<>(true, "Hospedaje guardado correctamente", hospedaje);
            } else {
                return new Respuesta<>(false, "No se pudo guardar el hospedaje", null);
            }
        } catch (SQLException e) {
            throw new SQLException("Error al guardar el hospedaje: " + e.getMessage(), e);
        } finally {
            if (generatedKeys != null) generatedKeys.close();
            if (preparedStatement != null) preparedStatement.close();
        }
    }

    /**
     * Este método permite actualizar un hospedaje existente
     * @param hospedaje Hospedaje con los datos actualizados
     * @return Respuesta con el resultado de la operación
     * @throws SQLException
     */
    public Respuesta<Hospedaje> actualizarHospedaje(Hospedaje hospedaje) throws SQLException {
        PreparedStatement preparedStatement = null;

        try {
            // Verificar si existe el hospedaje
            Respuesta<Hospedaje> respuestaBusqueda = buscarHospedajePorId(hospedaje.getId());
            if (!respuestaBusqueda.isExito()) {
                return respuestaBusqueda;
            }

            String query = "UPDATE hospedaje SET nombre = ?, direccion = ?, ciudad = ?, " +
                    "telefono = ?, estrellas = ?, descripcion = ? WHERE id = ?";

            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, hospedaje.getNombre());
            preparedStatement.setString(2, hospedaje.getDireccion());
            preparedStatement.setString(3, hospedaje.getCiudad());
            preparedStatement.setString(4, hospedaje.getTelefono());
            preparedStatement.setInt(5, hospedaje.getEstrellas());
            preparedStatement.setString(6, hospedaje.getDescripcion());
            preparedStatement.setInt(7, hospedaje.getId());

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                // Cargar habitaciones actualizadas
                cargarHabitacionesHospedaje(hospedaje);

                return new Respuesta<>(true, "Hospedaje actualizado correctamente", hospedaje);
            } else {
                return new Respuesta<>(false, "No se pudo actualizar el hospedaje", null);
            }
        } catch (SQLException e) {
            throw new SQLException("Error al actualizar el hospedaje: " + e.getMessage(), e);
        } finally {
            if (preparedStatement != null) preparedStatement.close();
        }
    }

    /**
     * Este método permite eliminar un hospedaje y todas sus habitaciones
     * @param id ID del hospedaje a eliminar
     * @return Respuesta con el resultado de la operación
     * @throws SQLException
     */
    public Respuesta<Hospedaje> eliminarHospedaje(int id) throws SQLException {
        PreparedStatement preparedStatement = null;

        try {
            // Verificar si existe el hospedaje
            Respuesta<Hospedaje> respuestaBusqueda = buscarHospedajePorId(id);
            if (!respuestaBusqueda.isExito()) {
                return respuestaBusqueda;
            }

            // Verificar si el hospedaje tiene paquetes asociados
            if (tienePaquetesAsociados(id)) {
                return new Respuesta<>(false, "No se puede eliminar el hospedaje porque está asociado a uno o más paquetes turísticos", null);
            }

            // Verificar si hay habitaciones con reservas
            if (tieneHabitacionesConReservas(id)) {
                return new Respuesta<>(false, "No se puede eliminar el hospedaje porque algunas de sus habitaciones tienen reservas asociadas", null);
            }

            // Eliminar todas las habitaciones asociadas
            eliminarHabitacionesDeHospedaje(id);

            // Eliminar el hospedaje
            String query = "DELETE FROM hospedaje WHERE id = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, id);

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                return new Respuesta<>(true, "Hospedaje eliminado correctamente", respuestaBusqueda.getData());
            } else {
                return new Respuesta<>(false, "No se pudo eliminar el hospedaje", null);
            }
        } catch (SQLException e) {
            throw new SQLException("Error al eliminar el hospedaje: " + e.getMessage(), e);
        } finally {
            if (preparedStatement != null) preparedStatement.close();
        }
    }

    /**
     * Este método permite buscar un hospedaje por su ID
     * @param id ID del hospedaje a buscar
     * @return Respuesta con el resultado de la operación
     * @throws SQLException
     */
    public Respuesta<Hospedaje> buscarHospedajePorId(int id) throws SQLException {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            String query = "SELECT * FROM hospedaje WHERE id = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, id);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                Hospedaje hospedaje = crearHospedajeDesdeResultSet(resultSet);

                // Cargar habitaciones asociadas al hospedaje
                cargarHabitacionesHospedaje(hospedaje);

                return new Respuesta<>(true, "Hospedaje encontrado", hospedaje);
            } else {
                return new Respuesta<>(false, "No se encontró el hospedaje con el ID proporcionado", null);
            }
        } catch (SQLException e) {
            throw new SQLException("Error al buscar el hospedaje: " + e.getMessage(), e);
        } finally {
            if (resultSet != null) resultSet.close();
            if (preparedStatement != null) preparedStatement.close();
        }
    }

    /**
     * Este método permite verificar si un hospedaje tiene paquetes asociados
     * @param hospedajeId ID del hospedaje a verificar
     * @return true si tiene paquetes asociados, false en caso contrario
     * @throws SQLException
     */
    private boolean tienePaquetesAsociados(int hospedajeId) throws SQLException {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            String query = "SELECT 1 FROM paquete_hospedaje WHERE hospedaje_id = ? LIMIT 1";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, hospedajeId);
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
     * Este método permite verificar si un hospedaje tiene habitaciones con reservas
     * @param hospedajeId ID del hospedaje a verificar
     * @return true si tiene habitaciones con reservas, false en caso contrario
     * @throws SQLException
     */
    private boolean tieneHabitacionesConReservas(int hospedajeId) throws SQLException {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            String query = "SELECT 1 FROM reserva_habitacion rh " +
                    "JOIN habitacion h ON rh.habitacion_id = h.id " +
                    "WHERE h.hospedaje_id = ? LIMIT 1";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, hospedajeId);
            resultSet = preparedStatement.executeQuery();

            return resultSet.next();
        } catch (SQLException e) {
            throw new SQLException("Error al verificar habitaciones con reservas: " + e.getMessage(), e);
        } finally {
            if (resultSet != null) resultSet.close();
            if (preparedStatement != null) preparedStatement.close();
        }
    }

    /**
     * Este método permite eliminar todas las habitaciones de un hospedaje
     * @param hospedajeId ID del hospedaje
     * @throws SQLException
     */
    private void eliminarHabitacionesDeHospedaje(int hospedajeId) throws SQLException {
        PreparedStatement preparedStatement = null;

        try {
            String query = "DELETE FROM habitacion WHERE hospedaje_id = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, hospedajeId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new SQLException("Error al eliminar habitaciones del hospedaje: " + e.getMessage(), e);
        } finally {
            if (preparedStatement != null) preparedStatement.close();
        }
    }

    /**
     * Este método permite cargar las habitaciones asociadas a un hospedaje
     * @param hospedaje Hospedaje al que se cargarán las habitaciones
     * @throws SQLException
     */
    private void cargarHabitacionesHospedaje(Hospedaje hospedaje) throws SQLException {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            String query = "SELECT h.*, th.nombre as tipo_nombre, th.descripcion as tipo_descripcion, th.precio as tipo_precio " +
                    "FROM habitacion h " +
                    "JOIN tipo_habitacion th ON h.tipo_habitacion_id = th.id " +
                    "WHERE h.hospedaje_id = ?";

            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, hospedaje.getId());
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

                hospedaje.getHabitaciones().add(habitacion);
            }
        } catch (SQLException e) {
            throw new SQLException("Error al cargar habitaciones del hospedaje: " + e.getMessage(), e);
        } finally {
            if (resultSet != null) resultSet.close();
            if (preparedStatement != null) preparedStatement.close();
        }
    }

    /**
     * Método utilitario para crear un objeto Hospedaje a partir de un ResultSet
     * @param resultSet
     * @return
     * @throws SQLException
     */
    private Hospedaje crearHospedajeDesdeResultSet(ResultSet resultSet) throws SQLException {
        Hospedaje hospedaje = new Hospedaje();
        hospedaje.setId(resultSet.getInt("id"));
        hospedaje.setNombre(resultSet.getString("nombre"));
        hospedaje.setDireccion(resultSet.getString("direccion"));
        hospedaje.setCiudad(resultSet.getString("ciudad"));
        hospedaje.setTelefono(resultSet.getString("telefono"));
        hospedaje.setEstrellas(resultSet.getInt("estrellas"));
        hospedaje.setDescripcion(resultSet.getString("descripcion"));

        return hospedaje;
    }
}