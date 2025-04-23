package co.edu.uniquindio.agenciaturistica.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import co.edu.uniquindio.agenciaturistica.model.Actividad;
import co.edu.uniquindio.agenciaturistica.model.Hospedaje;
import co.edu.uniquindio.agenciaturistica.model.PaqueteTuristico;
import co.edu.uniquindio.agenciaturistica.util.DataBaseConnection;
import co.edu.uniquindio.agenciaturistica.util.Respuesta;

public class PaqueteDAO {

    private Connection connection;

    public PaqueteDAO() throws SQLException {
        try {
            this.connection = DataBaseConnection.getConnection();
        } catch (SQLException e) {
            throw new SQLException(e);
        }
    }

    public PaqueteDAO(Connection connection) {
        this.connection = connection;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    /**
     * Este método permite obtener todos los paquetes turísticos
     * @return Lista de paquetes turísticos
     * @throws SQLException
     */
    public List<PaqueteTuristico> obtenerTodosLosPaquetes() throws SQLException {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        List<PaqueteTuristico> listaPaquetes = new ArrayList<>();

        try {
            String query = "SELECT * FROM paquete_turistico ORDER BY nombre";
            preparedStatement = connection.prepareStatement(query);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                PaqueteTuristico paquete = crearPaqueteDesdeResultSet(resultSet);

                // Cargar actividades y hospedajes asociados al paquete
                cargarActividadesPaquete(paquete);
                cargarHospedajesPaquete(paquete);

                listaPaquetes.add(paquete);
            }

            return listaPaquetes;
        } catch (SQLException e) {
            throw new SQLException("Error al obtener los paquetes turísticos: " + e.getMessage(), e);
        } finally {
            if (resultSet != null) resultSet.close();
            if (preparedStatement != null) preparedStatement.close();
        }
    }

    /**
     * Este método permite guardar un nuevo paquete turístico
     * @param paquete Paquete a guardar
     * @return Respuesta con el resultado de la operación
     * @throws SQLException
     */
    public Respuesta<PaqueteTuristico> guardarPaquete(PaqueteTuristico paquete) throws SQLException {
        PreparedStatement preparedStatement = null;
        ResultSet generatedKeys = null;

        try {
            String query = "INSERT INTO paquete_turistico (nombre, descripcion, precio_base, duracion_dias, " +
                    "fecha_inicio, fecha_fin, cupo_maximo, cupos_disponibles) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

            preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, paquete.getNombre());
            preparedStatement.setString(2, paquete.getDescripcion());
            preparedStatement.setDouble(3, paquete.getPrecioBase());
            preparedStatement.setInt(4, paquete.getDuracionDias());
            preparedStatement.setDate(5, paquete.getFechaInicio() != null ? Date.valueOf(paquete.getFechaInicio()) : null);
            preparedStatement.setDate(6, paquete.getFechaFin() != null ? Date.valueOf(paquete.getFechaFin()) : null);
            preparedStatement.setInt(7, paquete.getCupoMaximo());
            preparedStatement.setInt(8, paquete.getCuposDisponibles());

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                // Obtener el ID generado
                generatedKeys = preparedStatement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    paquete.setId(generatedKeys.getInt(1));
                }

                return new Respuesta<>(true, "Paquete turístico guardado correctamente", paquete);
            } else {
                return new Respuesta<>(false, "No se pudo guardar el paquete turístico", null);
            }
        } catch (SQLException e) {
            throw new SQLException("Error al guardar el paquete turístico: " + e.getMessage(), e);
        } finally {
            if (generatedKeys != null) generatedKeys.close();
            if (preparedStatement != null) preparedStatement.close();
        }
    }

    /**
     * Este método permite actualizar un paquete turístico existente
     * @param paquete Paquete con los datos actualizados
     * @return Respuesta con el resultado de la operación
     * @throws SQLException
     */
    public Respuesta<PaqueteTuristico> actualizarPaquete(PaqueteTuristico paquete) throws SQLException {
        PreparedStatement preparedStatement = null;

        try {
            // Verificar si existe el paquete
            Respuesta<PaqueteTuristico> respuestaBusqueda = buscarPaquetePorId(paquete.getId());
            if (!respuestaBusqueda.isExito()) {
                return respuestaBusqueda;
            }

            String query = "UPDATE paquete_turistico SET nombre = ?, descripcion = ?, precio_base = ?, " +
                    "duracion_dias = ?, fecha_inicio = ?, fecha_fin = ?, cupo_maximo = ?, " +
                    "cupos_disponibles = ? WHERE id = ?";

            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, paquete.getNombre());
            preparedStatement.setString(2, paquete.getDescripcion());
            preparedStatement.setDouble(3, paquete.getPrecioBase());
            preparedStatement.setInt(4, paquete.getDuracionDias());
            preparedStatement.setDate(5, paquete.getFechaInicio() != null ? Date.valueOf(paquete.getFechaInicio()) : null);
            preparedStatement.setDate(6, paquete.getFechaFin() != null ? Date.valueOf(paquete.getFechaFin()) : null);
            preparedStatement.setInt(7, paquete.getCupoMaximo());
            preparedStatement.setInt(8, paquete.getCuposDisponibles());
            preparedStatement.setInt(9, paquete.getId());

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                return new Respuesta<>(true, "Paquete turístico actualizado correctamente", paquete);
            } else {
                return new Respuesta<>(false, "No se pudo actualizar el paquete turístico", null);
            }
        } catch (SQLException e) {
            throw new SQLException("Error al actualizar el paquete turístico: " + e.getMessage(), e);
        } finally {
            if (preparedStatement != null) preparedStatement.close();
        }
    }

    /**
     * Este método permite eliminar un paquete turístico
     * @param id ID del paquete a eliminar
     * @return Respuesta con el resultado de la operación
     * @throws SQLException
     */
    public Respuesta<PaqueteTuristico> eliminarPaquete(int id) throws SQLException {
        PreparedStatement preparedStatement = null;

        try {
            // Verificar si existe el paquete
            Respuesta<PaqueteTuristico> respuestaBusqueda = buscarPaquetePorId(id);
            if (!respuestaBusqueda.isExito()) {
                return respuestaBusqueda;
            }

            // Verificar si el paquete tiene reservas asociadas
            if (tieneReservasAsociadas(id)) {
                return new Respuesta<>(false, "No se puede eliminar el paquete porque tiene reservas asociadas", null);
            }

            // Eliminar relaciones con actividades
            eliminarRelacionesActividades(id);

            // Eliminar relaciones con hospedajes
            eliminarRelacionesHospedajes(id);

            // Eliminar el paquete
            String query = "DELETE FROM paquete_turistico WHERE id = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, id);

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                return new Respuesta<>(true, "Paquete turístico eliminado correctamente", respuestaBusqueda.getData());
            } else {
                return new Respuesta<>(false, "No se pudo eliminar el paquete turístico", null);
            }
        } catch (SQLException e) {
            throw new SQLException("Error al eliminar el paquete turístico: " + e.getMessage(), e);
        } finally {
            if (preparedStatement != null) preparedStatement.close();
        }
    }

    /**
     * Este método permite buscar un paquete turístico por su ID
     * @param id ID del paquete a buscar
     * @return Respuesta con el resultado de la operación
     * @throws SQLException
     */
    public Respuesta<PaqueteTuristico> buscarPaquetePorId(int id) throws SQLException {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            String query = "SELECT * FROM paquete_turistico WHERE id = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, id);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                PaqueteTuristico paquete = crearPaqueteDesdeResultSet(resultSet);

                // Cargar actividades y hospedajes asociados al paquete
                cargarActividadesPaquete(paquete);
                cargarHospedajesPaquete(paquete);

                return new Respuesta<>(true, "Paquete turístico encontrado", paquete);
            } else {
                return new Respuesta<>(false, "No se encontró el paquete turístico con el ID proporcionado", null);
            }
        } catch (SQLException e) {
            throw new SQLException("Error al buscar el paquete turístico: " + e.getMessage(), e);
        } finally {
            if (resultSet != null) resultSet.close();
            if (preparedStatement != null) preparedStatement.close();
        }
    }

    /**
     * Este método permite verificar si un paquete turístico tiene reservas asociadas
     * @param paqueteId ID del paquete a verificar
     * @return true si tiene reservas, false en caso contrario
     * @throws SQLException
     */
    public boolean tieneReservasAsociadas(int paqueteId) throws SQLException {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            String query = "SELECT 1 FROM reserva WHERE paquete_id = ? LIMIT 1";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, paqueteId);
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
     * Este método permite eliminar todas las relaciones de un paquete con actividades
     * @param paqueteId ID del paquete
     * @throws SQLException
     */
    private void eliminarRelacionesActividades(int paqueteId) throws SQLException {
        PreparedStatement preparedStatement = null;

        try {
            String query = "DELETE FROM paquete_actividad WHERE paquete_id = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, paqueteId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new SQLException("Error al eliminar relaciones con actividades: " + e.getMessage(), e);
        } finally {
            if (preparedStatement != null) preparedStatement.close();
        }
    }

    /**
     * Este método permite eliminar todas las relaciones de un paquete con hospedajes
     * @param paqueteId ID del paquete
     * @throws SQLException
     */
    private void eliminarRelacionesHospedajes(int paqueteId) throws SQLException {
        PreparedStatement preparedStatement = null;

        try {
            String query = "DELETE FROM paquete_hospedaje WHERE paquete_id = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, paqueteId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new SQLException("Error al eliminar relaciones con hospedajes: " + e.getMessage(), e);
        } finally {
            if (preparedStatement != null) preparedStatement.close();
        }
    }

    /**
     * Este método permite cargar las actividades asociadas a un paquete
     * @param paquete Paquete al que se cargarán las actividades
     * @throws SQLException
     */
    private void cargarActividadesPaquete(PaqueteTuristico paquete) throws SQLException {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            String query = "SELECT a.* FROM actividad a " +
                    "JOIN paquete_actividad pa ON a.id = pa.actividad_id " +
                    "WHERE pa.paquete_id = ?";

            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, paquete.getId());
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
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

                paquete.getActividades().add(actividad);
            }
        } catch (SQLException e) {
            throw new SQLException("Error al cargar actividades del paquete: " + e.getMessage(), e);
        } finally {
            if (resultSet != null) resultSet.close();
            if (preparedStatement != null) preparedStatement.close();
        }
    }

    /**
     * Este método permite cargar los hospedajes asociados a un paquete
     * @param paquete Paquete al que se cargarán los hospedajes
     * @throws SQLException
     */
    private void cargarHospedajesPaquete(PaqueteTuristico paquete) throws SQLException {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            String query = "SELECT h.* FROM hospedaje h " +
                    "JOIN paquete_hospedaje ph ON h.id = ph.hospedaje_id " +
                    "WHERE ph.paquete_id = ?";

            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, paquete.getId());
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                Hospedaje hospedaje = new Hospedaje();
                hospedaje.setId(resultSet.getInt("id"));
                hospedaje.setNombre(resultSet.getString("nombre"));
                hospedaje.setDireccion(resultSet.getString("direccion"));
                hospedaje.setCiudad(resultSet.getString("ciudad"));
                hospedaje.setTelefono(resultSet.getString("telefono"));
                hospedaje.setEstrellas(resultSet.getInt("estrellas"));
                hospedaje.setDescripcion(resultSet.getString("descripcion"));

                paquete.getHospedajes().add(hospedaje);
            }
        } catch (SQLException e) {
            throw new SQLException("Error al cargar hospedajes del paquete: " + e.getMessage(), e);
        } finally {
            if (resultSet != null) resultSet.close();
            if (preparedStatement != null) preparedStatement.close();
        }
    }

    /**
     * Este método permite agregar una actividad a un paquete turístico
     * @param paqueteId ID del paquete
     * @param actividadId ID de la actividad
     * @return Respuesta con el resultado de la operación
     * @throws SQLException
     */
    public Respuesta<Boolean> agregarActividadAPaquete(int paqueteId, int actividadId) throws SQLException {
        PreparedStatement preparedStatement = null;

        try {
            // Verificar si ya existe la relación
            if (existeRelacionPaqueteActividad(paqueteId, actividadId)) {
                return new Respuesta<>(false, "La actividad ya está asociada a este paquete", false);
            }

            String query = "INSERT INTO paquete_actividad (paquete_id, actividad_id) VALUES (?, ?)";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, paqueteId);
            preparedStatement.setInt(2, actividadId);

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                return new Respuesta<>(true, "Actividad agregada al paquete correctamente", true);
            } else {
                return new Respuesta<>(false, "No se pudo agregar la actividad al paquete", false);
            }
        } catch (SQLException e) {
            throw new SQLException("Error al agregar actividad al paquete: " + e.getMessage(), e);
        } finally {
            if (preparedStatement != null) preparedStatement.close();
        }
    }

    /**
     * Este método permite verificar si ya existe una relación entre un paquete y una actividad
     * @param paqueteId ID del paquete
     * @param actividadId ID de la actividad
     * @return true si existe la relación, false en caso contrario
     * @throws SQLException
     */
    private boolean existeRelacionPaqueteActividad(int paqueteId, int actividadId) throws SQLException {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            String query = "SELECT 1 FROM paquete_actividad WHERE paquete_id = ? AND actividad_id = ? LIMIT 1";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, paqueteId);
            preparedStatement.setInt(2, actividadId);
            resultSet = preparedStatement.executeQuery();

            return resultSet.next();
        } catch (SQLException e) {
            throw new SQLException("Error al verificar relación paquete-actividad: " + e.getMessage(), e);
        } finally {
            if (resultSet != null) resultSet.close();
            if (preparedStatement != null) preparedStatement.close();
        }
    }

    /**
     * Este método permite eliminar una actividad de un paquete turístico
     * @param paqueteId ID del paquete
     * @param actividadId ID de la actividad
     * @return Respuesta con el resultado de la operación
     * @throws SQLException
     */
    public Respuesta<Boolean> eliminarActividadDePaquete(int paqueteId, int actividadId) throws SQLException {
        PreparedStatement preparedStatement = null;

        try {
            String query = "DELETE FROM paquete_actividad WHERE paquete_id = ? AND actividad_id = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, paqueteId);
            preparedStatement.setInt(2, actividadId);

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                return new Respuesta<>(true, "Actividad eliminada del paquete correctamente", true);
            } else {
                return new Respuesta<>(false, "No se encontró la relación entre el paquete y la actividad", false);
            }
        } catch (SQLException e) {
            throw new SQLException("Error al eliminar actividad del paquete: " + e.getMessage(), e);
        } finally {
            if (preparedStatement != null) preparedStatement.close();
        }
    }

    /**
     * Este método permite agregar un hospedaje a un paquete turístico
     * @param paqueteId ID del paquete
     * @param hospedajeId ID del hospedaje
     * @return Respuesta con el resultado de la operación
     * @throws SQLException
     */
    public Respuesta<Boolean> agregarHospedajeAPaquete(int paqueteId, int hospedajeId) throws SQLException {
        PreparedStatement preparedStatement = null;

        try {
            // Verificar si ya existe la relación
            if (existeRelacionPaqueteHospedaje(paqueteId, hospedajeId)) {
                return new Respuesta<>(false, "El hospedaje ya está asociado a este paquete", false);
            }

            String query = "INSERT INTO paquete_hospedaje (paquete_id, hospedaje_id) VALUES (?, ?)";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, paqueteId);
            preparedStatement.setInt(2, hospedajeId);

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                return new Respuesta<>(true, "Hospedaje agregado al paquete correctamente", true);
            } else {
                return new Respuesta<>(false, "No se pudo agregar el hospedaje al paquete", false);
            }
        } catch (SQLException e) {
            throw new SQLException("Error al agregar hospedaje al paquete: " + e.getMessage(), e);
        } finally {
            if (preparedStatement != null) preparedStatement.close();
        }
    }

    /**
     * Este método permite verificar si ya existe una relación entre un paquete y un hospedaje
     * @param paqueteId ID del paquete
     * @param hospedajeId ID del hospedaje
     * @return true si existe la relación, false en caso contrario
     * @throws SQLException
     */
    private boolean existeRelacionPaqueteHospedaje(int paqueteId, int hospedajeId) throws SQLException {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            String query = "SELECT 1 FROM paquete_hospedaje WHERE paquete_id = ? AND hospedaje_id = ? LIMIT 1";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, paqueteId);
            preparedStatement.setInt(2, hospedajeId);
            resultSet = preparedStatement.executeQuery();

            return resultSet.next();
        } catch (SQLException e) {
            throw new SQLException("Error al verificar relación paquete-hospedaje: " + e.getMessage(), e);
        } finally {
            if (resultSet != null) resultSet.close();
            if (preparedStatement != null) preparedStatement.close();
        }
    }

    /**
     * Este método permite eliminar un hospedaje de un paquete turístico
     * @param paqueteId ID del paquete
     * @param hospedajeId ID del hospedaje
     * @return Respuesta con el resultado de la operación
     * @throws SQLException
     */
    public Respuesta<Boolean> eliminarHospedajeDePaquete(int paqueteId, int hospedajeId) throws SQLException {
        PreparedStatement preparedStatement = null;

        try {
            String query = "DELETE FROM paquete_hospedaje WHERE paquete_id = ? AND hospedaje_id = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, paqueteId);
            preparedStatement.setInt(2, hospedajeId);

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                return new Respuesta<>(true, "Hospedaje eliminado del paquete correctamente", true);
            } else {
                return new Respuesta<>(false, "No se encontró la relación entre el paquete y el hospedaje", false);
            }
        } catch (SQLException e) {
            throw new SQLException("Error al eliminar hospedaje del paquete: " + e.getMessage(), e);
        } finally {
            if (preparedStatement != null) preparedStatement.close();
        }
    }

    /**
     * Método utilitario para crear un objeto PaqueteTuristico a partir de un ResultSet
     * @param resultSet
     * @return
     * @throws SQLException
     */
    private PaqueteTuristico crearPaqueteDesdeResultSet(ResultSet resultSet) throws SQLException {
        PaqueteTuristico paquete = new PaqueteTuristico();
        paquete.setId(resultSet.getInt("id"));
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
    }
}
