package co.edu.uniquindio.agenciaturistica.dao;

import co.edu.uniquindio.agenciaturistica.model.Enums.Rol;
import co.edu.uniquindio.agenciaturistica.model.Usuario;
import co.edu.uniquindio.agenciaturistica.util.Respuesta;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;

import java.sql.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class UsuarioDAOTest {

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    private UsuarioDAO usuarioDAO;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        usuarioDAO = new UsuarioDAO(connection);
    }

    @Test
    public void testIniciarSesion_EmailNoExiste() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        Respuesta<Usuario> respuesta = usuarioDAO.iniciarSesion("correo@dominio.com", "1234");
        assertFalse(respuesta.isExito());
    }

    @Test
    public void testExisteEmail_True() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);

        boolean existe = usuarioDAO.existeEmail("correo@dominio.com");
        assertTrue(existe);
    }

    @Test
    public void testActualizarCodigoRecuperacion_EmailNoExiste() throws Exception {
        UsuarioDAO dao = spy(new UsuarioDAO(connection));
        doReturn(false).when(dao).existeEmail(anyString());

        Respuesta<String> respuesta = dao.actualizarCodigoRecuperacion("email", "codigo");
        assertFalse(respuesta.isExito());
    }

    @Test
    public void testRevertirCodigoRecuperacion() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        Respuesta<String> respuesta = usuarioDAO.revertirCodigoRecuperacion("email");
        assertTrue(respuesta.isExito());
    }

    @Test
    public void testActualizarCodigoVerificacion_EmailNoExiste() throws Exception {
        UsuarioDAO dao = spy(new UsuarioDAO(connection));
        doReturn(false).when(dao).existeEmail(anyString());

        Respuesta<String> respuesta = dao.actualizarCodigoVerificacion("email", "codigo");
        assertFalse(respuesta.isExito());
    }

    @Test
    public void testRevertirCodigoVerificacion() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        Respuesta<String> respuesta = usuarioDAO.revertirCodigoVerificacion("email");
        assertTrue(respuesta.isExito());
    }

    @Test
    public void testRegistrarUsuario_Exitoso() throws Exception {
        Usuario usuario = new Usuario("Nombre", "Apellido", "123", "correo@dominio.com", "password", Rol.ADMINISTRADOR);

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        Respuesta<Usuario> respuesta = usuarioDAO.registrarUsuario(usuario);
        assertTrue(respuesta.isExito());
        assertEquals("Usuario registrado exitosamente", respuesta.getMensaje());
    }
}