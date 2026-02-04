package sistemacadastrodecliente.util;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Scanner;

public class ValidadorCEP {

    // Faixas de CEP por estado (offline)
    private static final Map<String, int[]> FAIXAS_POR_ESTADO = Map.ofEntries(
        Map.entry("AC", new int[]{69900000, 69999999}),
        Map.entry("AL", new int[]{57000000, 57999999}),
        Map.entry("AP", new int[]{68900000, 68999999}),
        Map.entry("AM", new int[]{69000000, 69299999}),
        Map.entry("BA", new int[]{40000000, 48999999}),
        Map.entry("CE", new int[]{60000000, 63999999}),
        Map.entry("DF", new int[]{70000000, 73699999}),
        Map.entry("ES", new int[]{29000000, 29999999}),
        Map.entry("GO", new int[]{72800000, 76799999}),
        Map.entry("MA", new int[]{65000000, 65999999}),
        Map.entry("MT", new int[]{78000000, 78899999}),
        Map.entry("MS", new int[]{79000000, 79999999}),
        Map.entry("MG", new int[]{30000000, 39999999}),
        Map.entry("PA", new int[]{66000000, 68899999}),
        Map.entry("PB", new int[]{58000000, 58999999}),
        Map.entry("PR", new int[]{80000000, 87999999}),
        Map.entry("PE", new int[]{50000000, 56999999}),
        Map.entry("PI", new int[]{64000000, 64999999}),
        Map.entry("RJ", new int[]{20000000, 28999999}),
        Map.entry("RN", new int[]{59000000, 59999999}),
        Map.entry("RS", new int[]{90000000, 99999999}),
        Map.entry("RO", new int[]{76800000, 76999999}),
        Map.entry("RR", new int[]{69300000, 69399999}),
        Map.entry("SC", new int[]{88000000, 89999999}),
        Map.entry("SP", new int[]{10000000, 19999999}),
        Map.entry("SE", new int[]{49000000, 49999999}),
        Map.entry("TO", new int[]{77000000, 77999999})
    );

    // Classe interna para armazenar cidade e UF
    public static class DadosCEP {
        public final String cidade;
        public final String uf;

        public DadosCEP(String cidade, String uf) {
            this.cidade = cidade;
            this.uf = uf;
        }
    }

    // Validação principal
    public static boolean validar(String cep, String cidade, String uf) {
        if (!validarFormato(cep)) return false;

        if (isOnline()) {
            return validarOnline(cep, cidade, uf);
        } else {
            return validarOffline(cep, uf);
        }
    }

    // Verifica se a internet/API está disponível
    private static boolean isOnline() {
        try {
            URL url = new URL("https://viacep.com.br/ws/01001000/json/");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(2000);
            conn.setReadTimeout(2000);
            conn.setRequestMethod("GET");
            return conn.getResponseCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }

    // Validação online
    private static boolean validarOnline(String cep, String cidade, String uf) {
        DadosCEP dados = buscarDados(cep);
        if (dados == null) return false;

        return dados.cidade.equalsIgnoreCase(cidade) &&
               dados.uf.equalsIgnoreCase(uf);
    }

    // Validação offline
    private static boolean validarOffline(String cep, String uf) {
        int cepNum = Integer.parseInt(cep.replace("-", ""));
        int[] faixa = FAIXAS_POR_ESTADO.get(uf.toUpperCase());
        return faixa != null && cepNum >= faixa[0] && cepNum <= faixa[1];
    }

    // Formato do CEP
    private static boolean validarFormato(String cep) {
        return cep != null && cep.trim().matches("\\d{5}-?\\d{3}");
    }

    // Buscar dados do CEP (cidade + UF)
    public static DadosCEP buscarDados(String cep) {
        try {
            URL url = new URL("https://viacep.com.br/ws/" + cep + "/json/");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);

            Scanner sc = new Scanner(conn.getInputStream());
            String json = sc.useDelimiter("\\A").next();
            sc.close();

            if (json.contains("\"erro\": true")) return null;

            // Extrair cidade e UF diretamente
            String cidade = extrairCampo(json, "localidade");
            String uf = extrairCampo(json, "uf");

            if (cidade == null || uf == null) return null;
            return new DadosCEP(cidade, uf);

        } catch (Exception e) {
            return null;
        }
    }

    // Extrai um campo do JSON simples (sem precisar de biblioteca)
    private static String extrairCampo(String json, String campo) {
        String pattern = "\"" + campo + "\":\\s*\"([^\"]*)\"";
        java.util.regex.Matcher m = java.util.regex.Pattern.compile(pattern).matcher(json);
        if (m.find()) return m.group(1);
        return null;
    }
}
