package co.edu.uniquindio.agenciaturistica.util;

import java.io.InputStream;
import java.util.Properties;

public class ConfigLoader {

    private static final Properties properties = new Properties();

    static{

        try(InputStream input = ConfigLoader.class.getResourceAsStream("/config.properties")) {
            if (input == null) {
                throw new RuntimeException("No se encontro el archivo config.properties");
            }
            properties.load(input);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al cargar el archivo de configuracion" , e);
        }
    }

    public static String get(String key){
        return properties.getProperty(key);
    }


}
