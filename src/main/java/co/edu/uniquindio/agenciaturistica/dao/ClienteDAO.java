package co.edu.uniquindio.agenciaturistica.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import co.edu.uniquindio.agenciaturistica.model.Cliente;
import co.edu.uniquindio.agenciaturistica.util.DataBaseConnection;
import co.edu.uniquindio.agenciaturistica.util.Respuesta;

public class ClienteDAO {

    private Connection connection;

    public ClienteDAO() throws SQLException {
        try {
            this.connection = DataBaseConnection.getConnection();
        } catch (SQLException e) {
            throw new SQLException(e);
        }
    }

    public ClienteDAO(Connection connection) {
        this.connection = connection;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    /**
     * Este método permite obtener todos los clientes registrados en el sistema
     * @return Lista de clientes
     * @throws SQLException
     */
    public List<Cliente> obtenerTodosLosClientes() throws SQLException {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        List<Cliente> listaClientes = new ArrayList<>();

        try {
            String query = "SELECT * FROM cliente ORDER BY nombre, apellido";
            preparedStatement = connection.prepareStatement(query);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                Cliente cliente = crearClienteDesdeResultSet(resultSet);
                listaClientes.add(cliente);
            }

            return listaClientes;
        } catch (SQLException e) {
            throw new SQLException("Error al obtener los clientes: " + e.getMessage(), e);
        } finally {
            if (resultSet != null) resultSet.close();
            if (preparedStatement != null) preparedStatement.close();
        }
    }

    /**
     * Este método permite guardar un nuevo cliente en la base de datos
     * @param cliente Cliente a guardar
     * @return Respuesta con el resultado de la operación
     * @throws SQLException
     */
    public Respuesta<Cliente> guardarCliente(Cliente cliente) throws SQLException {
        PreparedStatement preparedStatement = null;

        try {
            // Verificar si ya existe un cliente con la misma identificación
            if (existeIdentificacion(cliente.getIdentificacion())) {
                return new Respuesta<>(false, "Ya existe un cliente con la identificación proporcionada", null);
            }

            // Verificar si ya existe un cliente con el mismo correo
            if (existeCorreo(cliente.getCorreo())) {
                return new Respuesta<>(false, "Ya existe un cliente con el correo electrónico proporcionado", null);
            }

            String query = "INSERT INTO cliente (nombre, apellido, identificacion, correo, telefono, fecha_nacimiento) VALUES (?, ?, ?, ?, ?, ?)";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, cliente.getNombre());
            preparedStatement.setString(2, cliente.getApellido());
            preparedStatement.setString(3, cliente.getIdentificacion());
            preparedStatement.setString(4, cliente.getCorreo());
            preparedStatement.setString(5, cliente.getTelefono());
            preparedStatement.setDate(6, cliente.getFechaNacimiento() != null ? Date.valueOf(cliente.getFechaNacimiento()) : null);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                return new Respuesta<>(true, "Cliente registrado exitosamente", cliente);
            } else {
                return new Respuesta<>(false, "No se pudo registrar el cliente", null);
            }
        } catch (SQLException e) {
            throw new SQLException("Error al guardar el cliente: " + e.getMessage(), e);
        } finally {
            if (preparedStatement != null) preparedStatement.close();
        }
    }

    /**
     * Este método permite actualizar los datos de un cliente
     * @param cliente Cliente con los datos actualizados
     * @return Respuesta con el resultado de la operación
     * @throws SQLException
     */
    public Respuesta<Cliente> actualizarCliente(Cliente cliente) throws SQLException {
        PreparedStatement preparedStatement = null;

        try {
            // Verificar si existe el cliente con la identificación proporcionada
            if (!existeIdentificacion(cliente.getIdentificacion())) {
                return new Respuesta<>(false, "No existe un cliente con la identificación proporcionada", null);
            }

            // Obtener el cliente actual para verificar el correo
            Respuesta<Cliente> clienteActualRespuesta = buscarClientePorIdentificacion(cliente.getIdentificacion());
            if (!clienteActualRespuesta.isExito()) {
                return clienteActualRespuesta;
            }

            Cliente clienteActual = clienteActualRespuesta.getData();

            // Si el correo ha cambiado, verificar que no exista otro cliente con ese correo
            if (!clienteActual.getCorreo().equals(cliente.getCorreo()) && existeCorreo(cliente.getCorreo())) {
                return new Respuesta<>(false, "Ya existe otro cliente con el correo electrónico proporcionado", null);
            }

            String query = "UPDATE cliente SET nombre = ?, apellido = ?, correo = ?, telefono = ?, fecha_nacimiento = ? WHERE identificacion = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, cliente.getNombre());
            preparedStatement.setString(2, cliente.getApellido());
            preparedStatement.setString(3, cliente.getCorreo());
            preparedStatement.setString(4, cliente.getTelefono());
            preparedStatement.setDate(5, cliente.getFechaNacimiento() != null ? Date.valueOf(cliente.getFechaNacimiento()) : null);
            preparedStatement.setString(6, cliente.getIdentificacion());

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                return new Respuesta<>(true, "Cliente actualizado exitosamente", cliente);
            } else {
                return new Respuesta<>(false, "No se pudo actualizar el cliente", null);
            }
        } catch (SQLException e) {
            throw new SQLException("Error al actualizar el cliente: " + e.getMessage(), e);
        } finally {
            if (preparedStatement != null) preparedStatement.close();
        }
    }

    /**
     * Este método permite eliminar un cliente de la base de datos
     * @param identificacion Identificación del cliente a eliminar
     * @return Respuesta con el resultado de la operación
     * @throws SQLException
     */
    public Respuesta<Cliente> eliminarCliente(String identificacion) throws SQLException {
        PreparedStatement preparedStatement = null;

        try {
            // Verificar si existe el cliente con la identificación proporcionada
            Respuesta<Cliente> clienteRespuesta = buscarClientePorIdentificacion(identificacion);
            if (!clienteRespuesta.isExito()) {
                return clienteRespuesta;
            }

            Cliente cliente = clienteRespuesta.getData();

            // Verificar si el cliente tiene reservas asociadas
            if (tieneReservasAsociadas(identificacion)) {
                return new Respuesta<>(false, "No se puede eliminar el cliente porque tiene reservas asociadas", null);
            }

            String query = "DELETE FROM cliente WHERE identificacion = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, identificacion);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                return new Respuesta<>(true, "Cliente eliminado exitosamente", cliente);
            } else {
                return new Respuesta<>(false, "No se pudo eliminar el cliente", null);
            }
        } catch (SQLException e) {
            throw new SQLException("Error al eliminar el cliente: " + e.getMessage(), e);
        } finally {
            if (preparedStatement != null) preparedStatement.close();
        }
    }

    /**
     * Este método permite buscar un cliente por su identificación
     * @param identificacion Identificación del cliente a buscar
     * @return Respuesta con el resultado de la operación
     * @throws SQLException
     */
    public Respuesta<Cliente> buscarClientePorIdentificacion(String identificacion) throws SQLException {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            String query = "SELECT * FROM cliente WHERE identificacion = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, identificacion);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                Cliente cliente = crearClienteDesdeResultSet(resultSet);
                return new Respuesta<>(true, "Cliente encontrado", cliente);
            } else {
                return new Respuesta<>(false, "No se encontró un cliente con la identificación proporcionada", null);
            }
        } catch (SQLException e) {
            throw new SQLException("Error al buscar el cliente: " + e.getMessage(), e);
        } finally {
            if (resultSet != null) resultSet.close();
            if (preparedStatement != null) preparedStatement.close();
        }
    }

    /**
     * Este método permite verificar si existe un cliente con la identificación proporcionada
     * @param identificacion Identificación a verificar
     * @return true si existe, false si no
     * @throws SQLException
     */
    public boolean existeIdentificacion(String identificacion) throws SQLException {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            String query = "SELECT 1 FROM cliente WHERE identificacion = ? LIMIT 1";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, identificacion);
            resultSet = preparedStatement.executeQuery();

            return resultSet.next();
        } catch (SQLException e) {
            throw new SQLException("Error al verificar la identificación: " + e.getMessage(), e);
        } finally {
            if (resultSet != null) resultSet.close();
            if (preparedStatement != null) preparedStatement.close();
        }
    }

    /**
     * Este método permite verificar si existe un cliente con el correo proporcionado
     * @param correo Correo a verificar
     * @return true si existe, false si no
     * @throws SQLException
     */
    public boolean existeCorreo(String correo) throws SQLException {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            String query = "SELECT 1 FROM cliente WHERE correo = ? LIMIT 1";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, correo);
            resultSet = preparedStatement.executeQuery();

            return resultSet.next();
        } catch (SQLException e) {
            throw new SQLException("Error al verificar el correo: " + e.getMessage(), e);
        } finally {
            if (resultSet != null) resultSet.close();
            if (preparedStatement != null) preparedStatement.close();
        }
    }

    /**
     * Este método permite verificar si un cliente tiene reservas asociadas
     * @param identificacion Identificación del cliente
     * @return true si tiene reservas, false si no
     * @throws SQLException
     */
    public boolean tieneReservasAsociadas(String identificacion) throws SQLException {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            String query = "SELECT 1 FROM reserva r JOIN cliente c ON r.cliente_id = c.id WHERE c.identificacion = ? LIMIT 1";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, identificacion);
            resultSet = preparedStatement.executeQuery();

            return resultSet.next();
        } catch (SQLException e) {
            throw new SQLException("Error al verificar las reservas asociadas: " + e.getMessage(), e);
        } finally {
            if (resultSet != null) resultSet.close();
            if (preparedStatement != null) preparedStatement.close();
        }
    }

    /**
     * Método utilitario para crear un objeto Cliente a partir de un ResultSet
     * @param resultSet
     * @return
     * @throws SQLException
     */
    private Cliente crearClienteDesdeResultSet(ResultSet resultSet) throws SQLException {
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
    }
}
