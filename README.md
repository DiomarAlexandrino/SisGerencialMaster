
# SisGerencialMaster


---

### Sistema de Cadastro de Clientes no Modelo CRUD

O presente projeto consiste no desenvolvimento de um sistema de cadastro de clientes baseado no modelo CRUD (Create, Read, Update e Delete). O sistema permite a realização das operações de criação, consulta, atualização e exclusão de registros de clientes, além de aplicar validações com o objetivo de garantir a integridade e a consistência das informações armazenadas.

### Tecnologias Utilizadas

O sistema foi desenvolvido utilizando as seguintes tecnologias:

* Linguagem de programação Java
* Banco de dados PostgreSQL
* JDBC (Java Database Connectivity) para acesso ao banco de dados
* Ambiente de desenvolvimento IntelliJ IDEA

### Bibliotecas Externas

Para a implementação da interface gráfica e a melhoria da usabilidade do sistema, foram utilizadas bibliotecas externas adicionais:

**JCalendar (jcalendar-1.4.jar)**
A biblioteca JCalendar foi utilizada para o gerenciamento de campos de data na interface gráfica da aplicação, especialmente para o preenchimento da data de nascimento do cliente. Sua utilização contribui para a padronização das datas e reduz erros de entrada por parte do usuário.

**MigLayout (miglayout-3.7.4.jar)**
A biblioteca MigLayout foi empregada como gerenciador de layout da interface gráfica. Essa biblioteca possibilita uma organização mais flexível e eficiente dos componentes visuais, além de proporcionar melhor legibilidade do código quando comparada a outros gerenciadores de layout tradicionais do Java Swing.

### Execução do Sistema em Ambiente Local

Para que o código-fonte possa ser executado corretamente em ambiente local e o sistema seja iniciado de forma adequada, são necessárias algumas configurações e ajustes, descritos a seguir.

### Gestão de Dependências

É necessária a importação manual dos arquivos `.jar`, adicionando-os ao *classpath* do projeto no IntelliJ IDEA:

* postgresql-42.7.8.jar (driver de conexão com o PostgreSQL)
* jcalendar-1.4.jar
* miglayout-3.7.4.jar

### Ajustes de Persistência e Autenticação

Devem ser efetuados ajustes relacionados à persistência de dados e à autenticação no banco de dados, incluindo:

* Atualização das credenciais de acesso na classe `Conexao.java`, de modo a adequá-las às configurações do servidor PostgreSQL local.
* Validação da conectividade por meio da classe `TesteConexao.java`, a qual retorna uma mensagem de sucesso indicando que a conexão foi estabelecida corretamente.

Esses procedimentos asseguram a correta comunicação entre a aplicação e o banco de dados.

### Estrutura do Banco de Dados

O banco de dados foi modelado utilizando o PostgreSQL, sendo criada a tabela `clientes`, responsável por armazenar as informações cadastrais dos usuários do sistema. A estrutura da tabela é apresentada a seguir:

```sql
CREATE TABLE IF NOT EXISTS public.clientes
(
    id integer NOT NULL DEFAULT nextval('cliente_id_seq'::regclass),
    nome character varying(100) NOT NULL,
    idade integer,
    cpf character varying(11) NOT NULL,
    data_nascimento date,
    email character varying(100),
    telefone character varying(20),
    endereco character varying(150),
    numero character varying(10),
    cidade character varying(100),
    uf character(2),
    cep character varying(8),
    observacao text,
    CONSTRAINT cliente_pkey PRIMARY KEY (id),
    CONSTRAINT cliente_cpf_key UNIQUE (cpf)
);
```

A tabela possui como chave primária o campo `id` e uma restrição de unicidade para o campo `cpf`, garantindo que não haja duplicidade de registros para um mesmo cliente.

---
