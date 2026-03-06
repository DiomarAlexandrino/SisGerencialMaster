package sistemacadastrodecliente.util;

import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

public class ValidadorCEP {

    // ====================== Faixas Offline Atualizadas ======================
    private static final Map<String, int[]> FAIXAS_POR_ESTADO = Map.ofEntries(
            Map.entry("AC", new int[]{69900000, 69999999}),
            Map.entry("AL", new int[]{57000000, 57999999}),
            Map.entry("AM", new int[]{69000000, 69899999}),
            Map.entry("AP", new int[]{68900000, 68999999}),
            Map.entry("BA", new int[]{40000000, 48999999}),
            Map.entry("CE", new int[]{60000000, 63999999}),
            Map.entry("DF", new int[]{70000000, 73699999}),
            Map.entry("ES", new int[]{29000000, 29999999}),
            Map.entry("GO", new int[]{72800000, 76799999}),
            Map.entry("MA", new int[]{65000000, 65999999}),
            Map.entry("MG", new int[]{30000000, 39999999}),
            Map.entry("MS", new int[]{79000000, 79999999}),
            Map.entry("MT", new int[]{78000000, 78899999}),
            Map.entry("PA", new int[]{66000000, 68899999}),
            Map.entry("PB", new int[]{58000000, 58999999}),
            Map.entry("PE", new int[]{50000000, 56999999}),
            Map.entry("PI", new int[]{64000000, 64999999}),
            Map.entry("PR", new int[]{80000000, 87999999}),
            Map.entry("RJ", new int[]{20000000, 28999999}),
            Map.entry("RN", new int[]{59000000, 59999999}),
            Map.entry("RO", new int[]{76800000, 76999999}),
            Map.entry("RR", new int[]{69300000, 69399999}),
            Map.entry("RS", new int[]{90000000, 99999999}),
            Map.entry("SC", new int[]{88000000, 89999999}),
            Map.entry("SE", new int[]{49000000, 49999999}),
            Map.entry("SP", new int[]{1000000, 19999999}),
            Map.entry("TO", new int[]{77000000, 77999999})
    );

    private static final int MAX_CACHE_SIZE = 100;
    private static final Map<String, DadosCEP> cacheCEPs = new LinkedHashMap<>(MAX_CACHE_SIZE, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, DadosCEP> eldest) {
            return size() > MAX_CACHE_SIZE;
        }
    };

    public static DadosCEP buscarDados(String cep) {
        String cepLimpo = cep.replaceAll("[^0-9]", "");
        
        if (cepLimpo.length() != 8) {
       
            return null;
        }

        if (cacheCEPs.containsKey(cepLimpo)) {
            DadosCEP dadosCache = cacheCEPs.get(cepLimpo);
           
            return dadosCache;
        }

        DadosCEP dados = buscarDadosOnline(cepLimpo);

        if (dados == null) {
            dados = buscarDadosOffline(cepLimpo);
        } else {
            cacheCEPs.put(cepLimpo, dados);
        }

    
        return dados;
    }

    public static DadosCEP buscarDadosOnline(String cep) {
        // Tenta ViaCEP (campos: localidade, uf, logradouro, bairro)
        DadosCEP dados = consultarApi("https://viacep.com.br/ws/" + cep + "/json/", 
                                      "localidade", "uf", "logradouro", "bairro");
        
        // Se falhar, tenta BrasilAPI (campos: city, state, street, neighborhood)
        if (dados == null) {
            dados = consultarApi("https://brasilapi.com.br/api/cep/v1/" + cep, 
                                 "city", "state", "street", "neighborhood");
        }
        
        return dados;
    }

    private static DadosCEP consultarApi(String urlString, String campoCidade, String campoUf, String campoRua, String campoBairro) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlString);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(4000); 
            conn.setReadTimeout(4000);

            if (conn.getResponseCode() != 200) return null;

            Scanner sc = new Scanner(conn.getInputStream(), "UTF-8");
            String json = sc.useDelimiter("\\A").next();
            sc.close();

            if (json.contains("\"erro\":true") || json.contains("\"errors\":")) return null;

            String cidade = extrairCampo(json, campoCidade);
            String uf = extrairCampo(json, campoUf);
            String rua = extrairCampo(json, campoRua);
            String bairro = extrairCampo(json, campoBairro);

            if (uf != null && !uf.isEmpty()) {
                return new DadosCEP(cidade, uf, rua, bairro);
            }
        } catch (Exception e) {
            return null;
        } finally {
            if (conn != null) conn.disconnect();
        }
        return null;
    }

    public static DadosCEP buscarDadosOffline(String cep) {
        try {
            int cepNum = Integer.parseInt(cep.replace("-", ""));
            for (Map.Entry<String, int[]> entry : FAIXAS_POR_ESTADO.entrySet()) {
                if (cepNum >= entry.getValue()[0] && cepNum <= entry.getValue()[1]) {
                    return new DadosCEP("", entry.getKey(), "", "");
                }
            }
        } catch (NumberFormatException ignored) {}
        return null;
    }

    private static String extrairCampo(String json, String campo) {
        Pattern pattern = Pattern.compile("\"" + campo + "\":\\s*\"([^\"]*)\"");
        Matcher matcher = pattern.matcher(json);
        return matcher.find() ? matcher.group(1) : "";
    }

    public static boolean validar(String cep, String cidadeAtual, String uf, DadosCEP dados) {
        if (!validarFormato(cep) || dados == null) return false;

        if (dados.cidade == null || dados.cidade.isBlank()) {
            return dados.uf.equalsIgnoreCase(uf);
        }

        return dados.cidade.equalsIgnoreCase(cidadeAtual.trim()) && dados.uf.equalsIgnoreCase(uf.trim());
    }

    private static boolean validarFormato(String cep) {
        return cep != null && cep.trim().matches("^\\d{5}-?\\d{3}$");
    }

    // ====================== Classe de Dados Atualizada ======================
    public static class DadosCEP {
        public final String cidade;
        public final String uf;
        public final String logradouro;
        public final String bairro; // Novo campo

        public DadosCEP(String cidade, String uf, String logradouro, String bairro) {
            this.cidade = cidade != null ? cidade : "";
            this.uf = uf != null ? uf : "";
            this.logradouro = logradouro != null ? logradouro : "";
            this.bairro = bairro != null ? bairro : "";
        }
    }
}