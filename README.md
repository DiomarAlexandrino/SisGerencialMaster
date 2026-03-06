# SisGerencialMaster

![Java](https://img.shields.io/badge/Java-17-orange)
![Swing](https://img.shields.io/badge/Java-Swing-blue)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Database-336791)
![MVC](https://img.shields.io/badge/Architecture-MVC-green)
![Status](https://img.shields.io/badge/Status-Em%20Desenvolvimento-yellow)

Sistema desktop desenvolvido em **Java Swing** para **cadastro e gerenciamento de clientes**, utilizando arquitetura baseada no padrão **MVC** e persistência de dados em **PostgreSQL**.

O sistema implementa o padrão **CRUD (Create, Read, Update e Delete)** permitindo gerenciar registros de clientes com validação de dados, máscaras de entrada e organização modular do código.

---

# 📸 Preview da Aplicação

<img width="1696" height="962" alt="image" src="https://github.com/user-attachments/assets/c0f05650-1b46-4798-b675-624b23ddce2a" />


```
docs/imagens/tela-cadastro.png
```

Sugestão de estrutura:

```
docs
 ├─ imagens
 │   ├─ tela-cadastro.png
 │   ├─ tela-edicao.png
 │   └─ tabela-clientes.png
```

---

# 🚀 Funcionalidades

* Cadastro de clientes
* Atualização de clientes
* Exclusão de clientes
* Listagem de clientes em tabela
* Validação automática de formulário
* Máscaras para CPF, telefone e CEP
* Busca automática de endereço via CEP
* Alternância de tema visual (claro / escuro)
* Componentes personalizados para interface

---

# 🧠 Arquitetura do Sistema

O projeto foi estruturado seguindo o padrão **MVC (Model – View – Controller)** para separar responsabilidades e melhorar a organização do código.

```
                +----------------+
                |      View      |
                |  TelaCadastro  |
                +--------+-------+
                         |
                         |
                +--------v-------+
                |   Controller   |
                | ControleCadastro|
                +--------+-------+
                         |
                         |
                +--------v-------+
                |      DAO       |
                |   ClienteDAO   |
                +--------+-------+
                         |
                         |
                +--------v-------+
                |    Database    |
                |   PostgreSQL   |
                +----------------+
```

---

# 📂 Estrutura do Projeto

```
src
│
├── controller
│   ├── ControleDoCadastro.java
│   └── ControleEstadoTela.java
│
├── dao
│   └── ClienteDAO.java
│
├── model
│   └── Cliente.java
│
├── view
│   └── TelaDoCadastro.java
│
├── validation
│   ├── ValidadorFormulario.java
│   ├── ValidadorCEP.java
│   └── TipoValidacao.java
│
├── components
│   ├── CampoDataComCalendario.java
│   └── ComboBoxUF.java
│
└── util
    ├── Tema.java
    ├── TemaEnum.java
    └── UF.java
```

---

# ⚙️ Tecnologias Utilizadas

| Tecnologia    | Função                      |
| ------------- | --------------------------- |
| Java          | Linguagem principal         |
| Java Swing    | Interface gráfica           |
| PostgreSQL    | Banco de dados              |
| JDBC          | Comunicação com banco       |
| MVC           | Arquitetura do sistema      |
| SwingWorker   | Processamento em background |
| MaskFormatter | Máscaras de entrada         |

---

# 📚 Bibliotecas Utilizadas

### JCalendar

Arquivo:

```
jcalendar-1.4.jar
```

Utilizado para manipulação de **datas com componente de calendário** na interface gráfica.

---

### MigLayout

Arquivo:

```
miglayout-3.7.4.jar
```

Gerenciador de layout que permite organizar componentes da interface de forma flexível e eficiente.

---

# 🗄 Banco de Dados

Banco utilizado:

**PostgreSQL**

Nome do banco:

```
bd_cadastro_cliente
```

Tabela principal:

```
clientes
```

A tabela armazena os dados cadastrais dos clientes.

---

## Criação do Banco

```sql
CREATE DATABASE bd_cadastro_cliente
    WITH
    OWNER = postgres
    ENCODING = 'UTF8'
    LC_COLLATE = 'pt-BR'
    LC_CTYPE = 'pt-BR'
    LOCALE_PROVIDER = 'libc'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1
    IS_TEMPLATE = False;
```

---

## Criação da Sequence

```sql
CREATE SEQUENCE IF NOT EXISTS public.clientes_id_seq;
```

---

## Criação da Tabela

```sql
CREATE TABLE IF NOT EXISTS public.clientes
(
    id integer NOT NULL DEFAULT nextval('clientes_id_seq'::regclass),
    nome character varying(100) NOT NULL,
    cpf character varying(11) NOT NULL,
    data_nascimento date,
    email character varying(100),
    telefone character varying(20),
    endereco character varying(150),
    bairro character varying(50),
    numero character varying(10),
    cidade character varying(100),
    uf character(2),
    cep character varying(8),
    observacao text,
    CONSTRAINT clientes_pkey PRIMARY KEY (id),
    CONSTRAINT clientes_cpf_key UNIQUE (cpf),
    CONSTRAINT clientes_email_key UNIQUE (email),
    CONSTRAINT clientes_uf_check CHECK (char_length(uf) = 2)
);
```

---

# ▶️ Como Executar o Projeto

### 1️⃣ Clonar o repositório

```
git clone https://github.com/seu-usuario/seu-repositorio.git
```

---

### 2️⃣ Adicionar dependências

Adicione manualmente ao projeto:

```
postgresql-42.7.8.jar
jcalendar-1.4.jar
miglayout-3.7.4.jar
```

---

### 3️⃣ Configurar conexão

Editar a classe:

```
Conexao.java
```

Definindo:

* usuário
* senha
* porta
* nome do banco

---

### 4️⃣ Testar conexão

Execute:

```
TesteConexao.java
```

---

### 5️⃣ Executar o sistema

Rodar a classe principal:

```
TelaDoCadastro.java
```

---

# 📖 Documentação Completa

A documentação técnica completa do projeto está disponível em:

```
src/document.md
```

Contendo explicações detalhadas sobre:

* DAO
* Controllers
* Sistema de validação
* Busca de CEP
* Sistema de temas
* Estrutura do projeto

---

# 🔮 Melhorias Futuras

* Pesquisa avançada de clientes
* Paginação da tabela
* Exportação para Excel
* Exportação para PDF
* Sistema de login
* Controle de permissões
* API REST
* Versão Web

---

# 👨‍💻 Autor

Projeto desenvolvido para estudo de:

* **Java**
* **Java Swing**
* **Arquitetura MVC**
* **Integração com PostgreSQL**
* **Boas práticas de desenvolvimento**
