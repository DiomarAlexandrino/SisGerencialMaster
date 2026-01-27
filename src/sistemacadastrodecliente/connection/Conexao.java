package sistemacadastrodecliente.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexao {

    private static final String URL =
            System.getenv().getOrDefault(
                    "DB_URL",
                    "jdbc:postgresql://localhost:5432/bd_cadastro_cliente"
            );

    private static final String USER =
            System.getenv().getOrDefault(
                    "DB_USER",
                    "postgres"
            );

    private static final String PASSWORD =
            System.getenv().getOrDefault(
                    "DB_PASSWORD",
                    "admin"
            );

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            throw new RuntimeException("Erro na conexão com o banco", e);
        }
    }
}