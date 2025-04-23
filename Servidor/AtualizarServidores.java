
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class AtualizarServidores {

  private String host;
  private int porta;
  private int indiceAtual = 0;
  private Principal app;
  private BlockingQueue<String> filaMensagens;
  private Thread threadEnvio;

  // Construtor
  public AtualizarServidores(String host, int porta, Principal app) {
    this.host = host;
    this.porta = porta;
    this.app = app;
    this.filaMensagens = new LinkedBlockingQueue<>();
    iniciarThreadDeEnvio();
  }

  private void iniciarThreadDeEnvio() {
    threadEnvio = new Thread(() -> {
      while (true) {
        try {
          String mensagem = filaMensagens.take(); // bloqueia até ter uma mensagem
          String[] partes = mensagem.split("\\|");
          String tipo = partes[0].trim();
          String nomeUsuario = partes[1].trim();
          String nomeGrupo = partes[2].trim();
          if (partes.length != 3) {
            System.err.println("Mensagem mal formatada: " + mensagem);
            continue;
          }
          conectarESalvarAPDU(tipo, nomeUsuario, nomeGrupo);
        } catch (InterruptedException e) {
          System.out.println("Thread de envio interrompida.");
          break; // encerra a thread se for interrompida
        }
      }
    });
    threadEnvio.start();
  }

  /*
   * ***************************************************************
   * Metodo: conectarESalvarAPDU
   * Funcao: Estabelece uma conexão com o servidor e envia uma mensagem APDU.
   * Em caso de falha, tenta novamente após 1 segundo.
   * Parametros:
   * String tipoMensagem - tipo da mensagem ("JOIN" ou "LEAVE")
   * String nomeUsuario - nome do usuário
   * String nomeGrupo - nome do grupo
   * Retorno: void
   */
  private void conectarESalvarAPDU(String tipoMensagem, String nomeUsuario, String nomeGrupo) {
    try (Socket socket = new Socket(host, porta)) {
      System.out.println("Conectado ao servidor " + host + ":" + porta);
      // app.setIpServidor(host); //atualiza o servidor ativo

      // Envia a mensagem
      ObjectOutputStream saida = new ObjectOutputStream(socket.getOutputStream());
      String mensagem = tipoMensagem + "|" + nomeUsuario + "|" + nomeGrupo;
      saida.writeObject(mensagem);
      saida.flush();
      System.out.println("Mensagem enviada: " + mensagem);

      // Aguarda a resposta do servidor
      ObjectInputStream entrada = new ObjectInputStream(socket.getInputStream());
      String resposta = (String) entrada.readObject(); // Lê a resposta
      System.out.println("Resposta do servidor: " + resposta);
    } catch (IOException | ClassNotFoundException e) {
      System.err.println("Erro ao conectar ou enviar mensagem: " + e.getMessage());
      try {
        Thread.sleep(1000); // espera 1 segundo para escolher o novo servidor
      } catch (InterruptedException e1) {
        // TODO Auto-generated catch block
        e1.printStackTrace();
      }
      // escolherNovoServidor(tipoMensagem, nomeUsuario, nomeGrupo); // Se falhar,
      // tenta outro
      conectarESalvarAPDU(tipoMensagem, nomeUsuario, nomeGrupo); // tenta novamente depois de 1s
    }

  }

  /*
   * ***************************************************************
   * Metodo: enviarAPDUJoin
   * Funcao: Inicia uma thread que envia uma mensagem de "JOIN" para o servidor
   * atual.
   * Parametros:
   * String nomeUsuario - nome do usuário que está entrando no grupo
   * String nomeGrupo - nome do grupo que o usuário deseja entrar
   * Retorno: void
   */
  public void enviarAPDUJoin(String nomeUsuario, String nomeGrupo) {
    // Thread threadJoin = new Thread(() -> conectarESalvarAPDU("ATUALIZAR_JOIN",
    // nomeUsuario, nomeGrupo));
    // threadJoin.start(); // Inicia a thread de envio JOIN
    String mensagem = "ATUALIZAR_JOIN|" + nomeUsuario + "|" + nomeGrupo;
    filaMensagens.add(mensagem);
  }

  /*
   * ***************************************************************
   * Metodo: enviarAPDULeave
   * Funcao: Inicia uma thread que envia uma mensagem de "LEAVE" para o servidor
   * atual.
   * Parametros:
   * String nomeUsuario - nome do usuário que está saindo do grupo
   * String nomeGrupo - nome do grupo que o usuário deseja sair
   * Retorno: void
   */
  public void enviarAPDULeave(String nomeUsuario, String nomeGrupo) {
    // Thread threadLeave = new Thread(() -> conectarESalvarAPDU("ATUALIZAR_LEAVE",
    // nomeUsuario, nomeGrupo));
    // threadLeave.start(); // Inicia a thread de envio LEAVE
    String mensagem = "ATUALIZAR_LEAVE|" + nomeUsuario + "|" + nomeGrupo;
    filaMensagens.add(mensagem);

  }

  // private void escolherNovoServidor(String tipoMensagem, String nomeUsuario,
  // String nomeGrupo) {
  // List<String> listaServidores = new
  // ArrayList<String>(app.getServidoresConhecidos());

  // if (listaServidores.isEmpty())
  // return;

  // int total = listaServidores.size();
  // indiceAtual++;
  // if (indiceAtual >= total) {
  // indiceAtual = 0;
  // }

  // // Pega apenas o próximo da lista
  // // int proximoIndice = (indiceAtual + 1) % total;
  // String ip = listaServidores.get(indiceAtual);
  // host = ip;

  // System.out.println("Tentando próximo servidor: " + ip);
  // conectarESalvarAPDU(tipoMensagem, nomeUsuario, nomeGrupo);
  // }
}
