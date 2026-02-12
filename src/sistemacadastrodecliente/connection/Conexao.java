/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemacadastrodecliente.connection;

/**
 *
 * @author diomar.alexandrino
 */
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Properties;



public final class Conexao {

    private static final String PROPERTIES_FILE = "database.properties";

    private static String url;
    private static String user;
    private static String password;

    // Bloco estático executado uma única vez
    static {
        carregarConfiguracoes();
    }

    private Conexao() {
        // Impede instanciamento da classe
    }

    private static void carregarConfiguracoes() {
        try {
            Properties props = new Properties();
            InputStream input = null;

            // 1️⃣ Tenta carregar de fora do JAR (mesma pasta do .jar)
            try {
                input = new FileInputStream(PROPERTIES_FILE);
            } catch (Exception ignored) {
            }

            // 2️⃣ Se não encontrar fora, tenta dentro do JAR
            if (input == null) {
                input = Conexao.class
                        .getClassLoader()
                        .getResourceAsStream(PROPERTIES_FILE);
            }

            if (input == null) {
                throw new RuntimeException(
                        "Arquivo '" + PROPERTIES_FILE + "' não encontrado."
                );
            }

            props.load(input);

            url = Objects.requireNonNull(
                    props.getProperty("db.url"),
                    "Propriedade db.url não encontrada."
            );

            user = Objects.requireNonNull(
                    props.getProperty("db.user"),
                    "Propriedade db.user não encontrada."
            );

            password = Objects.requireNonNull(
                    props.getProperty("db.password"),
                    "Propriedade db.password não encontrada."
            );

        } catch (Exception e) {
            throw new RuntimeException(
                    "Erro ao carregar configurações do banco de dados.",
                    e
            );
        }
    }

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao conectar ao banco de dados.",
                    e
            );
        }
    }
}
