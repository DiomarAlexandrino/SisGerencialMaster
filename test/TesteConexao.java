/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author diomar.alexandrino
 */
import java.sql.Connection;
import sistemacadastrodecliente.connection.Conexao;

public class TesteConexao {
    public static void main(String[] args) {
        try (Connection conn = Conexao.getConnection()) {
            if (conn != null) {
                System.out.println("Conexão realizada com sucesso!");
            } else {
                System.out.println("Falha na conexão.");
            }
        } catch (Exception e) {
            System.err.println("Erro ao conectar: " + e.getMessage());
            e.printStackTrace();
        }
    }
}