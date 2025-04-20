
/* ***************************************************************
* Autor: Hugo Botelho Santana
* Matricula: 202210485
* Inicio: 19/04/2025
* Ultima alteracao: 20/04/2025
* Nome: Programa de Chat/WhatZap com múltiplos servidores (conexões UDP e TCP)
* Funcao: Aplicativo de chat para troca de mensagens com o modelo n clientes e n servidores
*************************************************************** */
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class Principal extends Application {
  private StackPane root = new StackPane(); // Usando StackPane para facilitar centralização
  private String nomeUsuario; // Nome do usuário conectado
  private String ipServidor;
  private ClienteTCP clienteTCP; // Instância do cliente TCP
  private ClienteUDP clienteUDP; // Instância do cliente UDP
  private Set<String> servidoresConhecidos;
  private DescobrirServidores descobrirServidores;

  private final static List<String> grupos = new ArrayList<>(); // Lista dinâmica de grupos
  private final static Map<String, HistoricoMensagens> historicosMensagens = new HashMap<>();

  private static TelaMeusGrupos telaMeusGrupos;

  @Override
  public void start(Stage primaryStage) {
    Scene scene = new Scene(root, 390, 644);
    primaryStage.setScene(scene);
    primaryStage.setTitle("Aplicativo de Instant Messaging");
    primaryStage.setResizable(false);
    primaryStage.show();

    // Configura o evento de encerramento do aplicativo
    primaryStage.setOnCloseRequest(t -> {
      if (clienteUDP != null) {
        clienteUDP.fechar(); // Fecha o cliente UDP
      }
      Platform.exit();
      System.exit(0);
    });

    telaMeusGrupos = new TelaMeusGrupos(this);

    // Mostra a tela inicial ao iniciar o programa
    TelaInicio telaInicio = new TelaInicio(this);
    root.getChildren().setAll(telaInicio.getLayout());

    // Centralizando o layout da TelaInicio
    root.setAlignment(telaInicio.getLayout(), javafx.geometry.Pos.CENTER);

    servidoresConhecidos = new HashSet<>();

    descobrirServidores = new DescobrirServidores(this);
    descobrirServidores.iniciarSincronizacao(); // descobre os servidores da rede e armazena em servidores conhecidos

  }

  /*
   * ***************************************************************
   * Metodo: criarClientes
   * Funcao: Cria as conexões TCP e UDP para o cliente com o IP do
   * servidor.
   * Parametros: String nomeUsuario - nome do usuário conectado.
   * Retorno: void
   */
  public void criarClientes(String nomeUsuario) {
    // this.ipServidor = ipServidor;
    this.nomeUsuario = nomeUsuario;

    if (ipServidor != null) {
      System.out.println("Cliente criado!");
      // Criando e conectando o cliente TCP
      clienteTCP = new ClienteTCP(ipServidor, 6789, this);

      // Criando e conectando o cliente UDP
      criarClienteUDP(ipServidor, 6789);
    }
  }

  /*
   * ***************************************************************
   * Metodo: criarClienteUDP
   * Funcao: Inicializa o cliente UDP e atualiza o IP do servidor se
   * necessário.
   * Parametros: String ipServidor - IP do servidor, int porta - porta de
   * conexão.
   * Retorno: void
   */
  public void criarClienteUDP(String ipServidor, int porta) {
    try {
      if (clienteUDP != null) {
        clienteUDP.setIpServidor(ipServidor); // atualiza o ip do servidor caso o usuario mude na tela de
                                              // configuracoes
      } else {
        clienteUDP = new ClienteUDP(ipServidor, porta); // Inicializa o cliente UDP
      }
      System.out.println("Cliente UDP criado e conectado ao servidor " + ipServidor + ":" + porta);
      iniciarThreadRecebimentoUDP(); // Inicia a thread para receber mensagens via UDP
    } catch (Exception e) {
      System.err.println("Erro ao criar ClienteUDP: " + e.getMessage());
    }
  }

  /*
   * ***************************************************************
   * Metodo: iniciarThreadRecebimentoUDP
   * Funcao: Cria e inicia uma thread para escutar mensagens recebidas
   * via UDP.
   * Parametros: sem parâmetros.
   * Retorno: void
   */
  private void iniciarThreadRecebimentoUDP() {
    new Thread(() -> {
      try {
        while (true) {
          String mensagemRecebida = clienteUDP.receberMensagem(); // Aguarda mensagens do servidor
          System.out.println("Mensagem recebida via UDP: " + mensagemRecebida);

          // Criar uma thread para processar e renderizar a mensagem recebida
          new Thread(() -> processarMensagemRecebida(mensagemRecebida)).start();
        }
      } catch (Exception e) {
        System.err.println("Erro ao receber mensagem UDP: " + e.getMessage());
      }
    }).start();
  }

  /*
   * ***************************************************************
   * Metodo: processarMensagemRecebida
   * Funcao: Processa uma mensagem recebida via UDP e atualiza a
   * interface gráfica.
   * Parametros: String mensagemRecebida - conteúdo da mensagem.
   * Retorno: void
   */
  private void processarMensagemRecebida(String mensagemRecebida) {
    try {
      // Separar os campos da mensagem
      String[] partes = mensagemRecebida.split("\\|");
      if (partes.length < 4 || !"SEND".equals(partes[0])) {
        System.err.println("Formato de mensagem inválido: " + mensagemRecebida);
        return;
      }

      String grupo = partes[1];
      String usuario = partes[2];
      String mensagem = partes[3];

      // Adicionar a mensagem ao histórico
      HistoricoMensagens historico = historicosMensagens.get(grupo);
      if (historico == null) {
        System.err.println("Grupo não encontrado: " + grupo);
        return;
      }
      String horaAtual = java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
      Mensagem novaMensagem = new Mensagem(usuario, mensagem, horaAtual);
      historico.adicionarMensagem(novaMensagem);

      // Atualizar a interface gráfica na thread da aplicação
      Platform.runLater(() -> {
        TelaMeusGrupos telaGrupos = getTelaMeusGrupos();
        Map<String, TelaChat> telasChat = telaGrupos.getTelasChat(); // Supondo que este método foi adicionado

        TelaChat telaChat = telasChat.get(grupo);
        if (telaChat != null) {
          telaChat.renderizarMensagens(); // Re-renderiza as mensagens
        }
      });
    } catch (Exception e) {
      System.err.println("Erro ao processar mensagem recebida: " + e.getMessage());
    }
  }

  /* ***************************************************************
  * Metodo: getClienteTCP
  * Funcao: Retorna a instância do cliente TCP.
  * Parametros: sem parâmetros.
  * Retorno: ClienteTCP
  *************************************************************** */
  public ClienteTCP getClienteTCP() {
    return clienteTCP;
  }

  /* ***************************************************************
  * Metodo: getClienteUDP
  * Funcao: Retorna a instância do cliente UDP.
  * Parametros: sem parâmetros.
  * Retorno: ClienteUDP
  *************************************************************** */
  public ClienteUDP getClienteUDP() {
    return clienteUDP;
  }
  /* ***************************************************************
  * Metodo: setNomeUsuario
  * Funcao: Define o nome do usuário atual.
  * Parametros: String nomeUsuario.
  * Retorno: void
  *************************************************************** */
  public void setNomeUsuario(String nomeUsuario) {
    this.nomeUsuario = nomeUsuario;
  }
  /* ***************************************************************
  * Metodo: getNomeUsuario
  * Funcao: Retorna o nome do usuário atual.
  * Parametros: sem parâmetros.
  * Retorno: String
  *************************************************************** */
  public String getNomeUsuario() {
    return nomeUsuario;
  }
  /* ***************************************************************
  * Metodo: setIpServidor
  * Funcao: Define o IP do servidor e atualiza a conexão UDP.
  * Parametros: String ip - IP do servidor.
  * Retorno: void
  *************************************************************** */
  public void setIpServidor(String ip) {
    this.ipServidor = ip;
    criarClienteUDP(ip, 6789); // so atualiza o ip do servidor do cliente UDP pois no tcp ja foi atualizado
                               // quando caiu no catch e elegeu um novo servidor
  }
  /* ***************************************************************
  * Metodo: getIpServidor
  * Funcao: Retorna o IP atual do servidor.
  * Parametros: sem parâmetros.
  * Retorno: String
  *************************************************************** */
  public String getIpServidor() {
    return ipServidor;
  }
  /* ***************************************************************
  * Metodo: getRoot
  * Funcao: Retorna o layout principal da aplicação.
  * Parametros: sem parâmetros.
  * Retorno: StackPane
  *************************************************************** */
  public StackPane getRoot() {
    return root;
  }
  /* ***************************************************************
  * Metodo: getGrupos
  * Funcao: Retorna a lista de grupos ativos.
  * Parametros: sem parâmetros.
  * Retorno: List<String>
  *************************************************************** */
  public List<String> getGrupos() {
    return grupos;
  }
  /* ***************************************************************
  * Metodo: getTelaMeusGrupos
  * Funcao: Retorna a tela de gerenciamento de grupos.
  * Parametros: sem parâmetros.
  * Retorno: TelaMeusGrupos
  *************************************************************** */
  public TelaMeusGrupos getTelaMeusGrupos() {
    return telaMeusGrupos;
  }

  /* ***************************************************************
  * Metodo: getHistoricosMensagens
  * Funcao: Retorna o mapa com o histórico de mensagens de cada grupo.
  * Parametros: sem parâmetros.
  * Retorno: Map<String, HistoricoMensagens>
  *************************************************************** */
  public Map<String, HistoricoMensagens> getHistoricosMensagens() {
    return historicosMensagens;
  }
  /* ***************************************************************
  * Metodo: getServidoresConhecidos
  * Funcao: Retorna o conjunto de IPs dos servidores conhecidos.
  * Parametros: sem parâmetros.
  * Retorno: Set<String>
  *************************************************************** */
  public Set<String> getServidoresConhecidos() {
    return servidoresConhecidos;
  }
  /* ***************************************************************
  * Metodo: setServidoresConhecidos
  * Funcao: Adiciona um novo IP ao conjunto de servidores conhecidos e inicializa conexões se necessário.
  * Parametros: String novoServidor - IP do servidor a ser adicionado.
  * Retorno: void
  *************************************************************** */
  public void setServidoresConhecidos(String novoServidor) {
    servidoresConhecidos.add(novoServidor);
    System.out.println("Servidor adicionado: " + novoServidor);
    if (ipServidor == null) {
      ipServidor = novoServidor;
      if (clienteTCP == null) {
        clienteTCP = new ClienteTCP(ipServidor, 6789, this);
      }
      if (clienteUDP == null) {
        criarClienteUDP(ipServidor, 6789);
      }
    }
  }

  /**
   * ***************************************************************
   * Metodo: main.
   * Funcao: metodo para iniciar a aplicacao.
   * Parametros: padrao java.
   * Retorno: sem retorno.
   * ***************************************************************
   */
  public static void main(String[] args) {
    launch(args);
  }
}
