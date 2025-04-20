
/* ***************************************************************
* Autor............: Hugo Botelho Santana
* Matricula........: 202210485
* Inicio...........: 21/11/2024
* Ultima alteracao.: 28/11/2024
* Nome.............: Camada de Transporte/Aplicação - Aplicativo de Instant Messaging
* Funcao...........: Aplicativo de chat para troca de mensagens com o modelo cliente servidor
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
        descobrirServidores.iniciarSincronizacao(); //descobre os servidores da rede e armazena em servidores conhecidos

    }

    /**
     * Método chamado quando o usuário clica em "Entrar" na tela de início.
     * Inicializa o cliente TCP e UDP.
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

    /**
     * Método para criar o cliente UDP e iniciar a escuta de mensagens.
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

    /**
     * Método que inicia uma thread para escutar mensagens recebidas via UDP.
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

    // Métodos getter e setter
    public ClienteTCP getClienteTCP() {
        return clienteTCP;
    }

    public ClienteUDP getClienteUDP() {
        return clienteUDP;
    }

    public void setNomeUsuario(String nomeUsuario) {
        this.nomeUsuario = nomeUsuario;
    }

    public String getNomeUsuario() {
        return nomeUsuario;
    }

    public void setIpServidor(String ip) {
        this.ipServidor = ip;
        criarClienteUDP(ip, 6789); // so atualiza o ip do servidor do cliente UDP pois no tcp ja foi atualizado quando caiu no catch e elegeu um novo servidor
    }

    public String getIpServidor() {
        return ipServidor;
    }

    public StackPane getRoot() {
        return root;
    }

    public List<String> getGrupos() {
        return grupos;
    }

    public TelaMeusGrupos getTelaMeusGrupos() {
        return telaMeusGrupos;
    }

    public Map<String, HistoricoMensagens> getHistoricosMensagens() {
        return historicosMensagens;
    }

    public Set<String> getServidoresConhecidos() {
        return servidoresConhecidos;
    }

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
