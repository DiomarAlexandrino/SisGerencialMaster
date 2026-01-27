package sistemacadastrodecliente.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import sistemacadastrodecliente.connection.Conexao;
import sistemacadastrodecliente.model.Cliente;

public class ClienteDAO {

    // CADASTRAR CLIENTE
    public void cadastrar(Cliente cliente) {
        String sql = """
                  INSERT INTO clientes
                  (nome, cpf, data_nascimento, email, telefone,
                   endereco, numero, cidade, uf, cep, observacao)
                  VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                  """;

        try (Connection conn = Conexao.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            

            ps.setString(1, cliente.getNome());
            ps.setString(2, cliente.getCpf());
            ps.setDate(3, Date.valueOf(cliente.getDataNascimento()));
            ps.setString(4, cliente.getEmail());
            ps.setString(5, cliente.getTelefone());
            ps.setString(6, cliente.getEndereco());
            ps.setString(7, cliente.getNumero());
            ps.setString(8, cliente.getCidade());
            ps.setString(9, cliente.getUf());
            ps.setString(10, cliente.getCep());
            ps.setString(11, cliente.getObservacao());

            int linhasAfetadas = ps.executeUpdate();
            if (linhasAfetadas > 0) {
                System.out.println("✅ Cliente cadastrado com sucesso!");
            } else {
                System.out.println("⚠️ Cliente não foi cadastrado!");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
// BUSCAR POR ID (Adicione este método)

    public Cliente buscarPorId(int id) {
        String sql = "SELECT * FROM clientes WHERE id = ?";

        try (Connection conn = Conexao.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Cliente c = new Cliente();
                c.setId(rs.getInt("id"));
                c.setNome(rs.getString("nome"));
                c.setCpf(rs.getString("cpf"));
                c.setEmail(rs.getString("email"));
                c.setTelefone(rs.getString("telefone"));
                c.setEndereco(rs.getString("endereco"));
                c.setNumero(rs.getString("numero"));
                c.setCidade(rs.getString("cidade"));
                c.setUf(rs.getString("uf"));
                c.setCep(rs.getString("cep"));
                c.setObservacao(rs.getString("observacao"));

                Date data = rs.getDate("data_nascimento");
                if (data != null) {
                    c.setDataNascimento(data.toLocalDate());
                }

                return c;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // BUSCAR POR NOME
    public List<Cliente> buscarPorNome(String nome) {
        List<Cliente> clientes = new ArrayList<>();
        String sql = """
        SELECT * FROM clientes
        WHERE nome ILIKE ?
        ORDER BY id
        """;

        try (Connection conn = Conexao.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + nome + "%");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Cliente cliente = new Cliente();
                cliente.setId(rs.getInt("id"));
                cliente.setNome(rs.getString("nome"));
                cliente.setCpf(rs.getString("cpf"));
                cliente.setDataNascimento(rs.getDate("data_nascimento").toLocalDate());
                cliente.setEmail(rs.getString("email"));
                cliente.setTelefone(rs.getString("telefone"));
                cliente.setEndereco(rs.getString("endereco"));
                cliente.setNumero(rs.getString("numero"));
                cliente.setCidade(rs.getString("cidade"));
                cliente.setUf(rs.getString("uf"));
                cliente.setCep(rs.getString("cep"));
                cliente.setObservacao(rs.getString("observacao"));
                clientes.add(cliente);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return clientes;
    }

    // BUSCAR POR CPF
    public Cliente buscarPorCpf(String cpf) {
        String sql = "SELECT * FROM clientes WHERE cpf = ?";

        try (Connection conn = Conexao.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, cpf);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Cliente c = new Cliente();
                c.setId(rs.getInt("id"));
                c.setNome(rs.getString("nome"));
                c.setCpf(rs.getString("cpf"));
                c.setEmail(rs.getString("email"));
                c.setTelefone(rs.getString("telefone"));
                c.setEndereco(rs.getString("endereco"));
                c.setNumero(rs.getString("numero"));
                c.setCidade(rs.getString("cidade"));
                c.setUf(rs.getString("uf"));
                c.setCep(rs.getString("cep"));
                c.setObservacao(rs.getString("observacao"));

                Date data = rs.getDate("data_nascimento");
                if (data != null) {
                    c.setDataNascimento(data.toLocalDate());
                }

                return c;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // ATUALIZAR CLIENTE
    public void atualizar(Cliente cliente) {
        String sql = """
                UPDATE clientes SET 
                  nome = ?, cpf = ?, data_nascimento = ?, email = ?, telefone = ?,
                  endereco = ?, numero = ?, cidade = ?, uf = ?, cep = ?, observacao = ?
                WHERE id = ?
                """;

        try (Connection conn = Conexao.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, cliente.getNome());
            ps.setString(2, cliente.getCpf());
            ps.setDate(3, Date.valueOf(cliente.getDataNascimento()));
            ps.setString(4, cliente.getEmail());
            ps.setString(5, cliente.getTelefone());
            ps.setString(6, cliente.getEndereco());
            ps.setString(7, cliente.getNumero());
            ps.setString(8, cliente.getCidade());
            ps.setString(9, cliente.getUf());
            ps.setString(10, cliente.getCep());
            ps.setString(11, cliente.getObservacao());
            ps.setInt(12, cliente.getId());

            int linhasAfetadas = ps.executeUpdate();
            if (linhasAfetadas > 0) {
                System.out.println("✅ Cliente atualizado com sucesso!");
            } else {
                System.out.println("⚠️ Cliente não encontrado!");
            }

        } catch (SQLException e) {
            System.out.println("❌ Erro ao atualizar cliente");
            e.printStackTrace();
        }
    }

    // EXCLUIR POR ID
    public boolean excluir(int id) {
        String sql = "DELETE FROM clientes WHERE id = ?";

        try (Connection conn = Conexao.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            int linhasAfetadas = ps.executeUpdate(); // retorna quantas linhas foram afetadas
            return linhasAfetadas > 0; // true se algum cliente foi excluído

        } catch (SQLException e) {
            e.printStackTrace();
            return false; // false em caso de erro
        }
    }

    // EXCLUIR POR CPF
    public void excluirPorCpf(String cpf) {
        String sql = "DELETE FROM clientes WHERE cpf = ?";

        try (Connection conn = Conexao.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cpf);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erro ao excluir cliente");
        }
    }

    // LISTAR CLIENTES
    public List<Cliente> listar() {
        List<Cliente> lista = new ArrayList<>();
        String sql = "SELECT id, nome, cpf, telefone, email, numero,  data_nascimento, cidade, endereco, cep, uf, observacao FROM clientes";

        try (Connection conn = Conexao.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Cliente c = new Cliente();
                c.setId(rs.getInt("id"));
                c.setNome(rs.getString("nome"));
                c.setCpf(rs.getString("cpf"));
                c.setDataNascimento(rs.getDate("data_nascimento").toLocalDate());
                c.setEmail(rs.getString("email"));
                c.setTelefone(rs.getString("telefone"));
                c.setEndereco(rs.getString("endereco"));
                c.setNumero(rs.getString("numero"));
                c.setCidade(rs.getString("cidade"));
                c.setUf(rs.getString("uf"));
                c.setCep(rs.getString("cep"));
                c.setObservacao(rs.getString("observacao"));
                lista.add(c);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return lista;
    }

    public boolean existeCpf(String cpf) {
        String sql = "SELECT 1 FROM clientes WHERE cpf = ?"; // atenção ao nome da tabela
        try (Connection conn = Conexao.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, cpf);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
