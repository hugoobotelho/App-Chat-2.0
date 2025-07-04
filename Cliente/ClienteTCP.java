
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class ClienteTCP {

  private String host;
  private int porta;
  private int indiceAtual = 0;
  private Principal app;

  // Construtor
  public ClienteTCP(String host, int porta, Principal app) {
    this.host = host;
    this.porta = porta;
    this.app = app;
  }

  /*
   * ***************************************************************
   * Metodo: conectarESalvarAPDU
   * Funcao: Conecta ao servidor TCP, envia uma APDU com dados formatados e
   * aguarda resposta.
   * Parametros: String tipoMensagem - tipo da requisição (JOIN ou LEAVE)
   * String nomeUsuario - nome do usuário
   * String nomeGrupo - nome do grupo
   * Retorno: void
   */
  private void conectarESalvarAPDU(String tipoMensagem, String nomeUsuario, String nomeGrupo) {
    try (Socket socket = new Socket(host, porta)) {
      System.out.println("Conectado ao servidor " + host + ":" + porta);
      app.setIpServidor(host); // atualiza o servidor ativo

      // Envia a mensagem
      ObjectOutputStream saida = new ObjectOutputStream(socket.getOutputStream());
      String mensagem = tipoMensagem + "|" + nomeUsuario + "|" + nomeGrupo + "|" + InetAddress.getLocalHost().getHostAddress();
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
      escolherNovoServidor(tipoMensagem, nomeUsuario, nomeGrupo); // Se falhar, tenta outro
    }

  }

  /*
   * ***************************************************************
   * Metodo: enviarAPDUJoin
   * Funcao: Inicia uma thread para envio da APDU JOIN ao servidor.
   * Parametros: String nomeUsuario - nome do usuário
   * String nomeGrupo - nome do grupo a ser ingressado
   * Retorno: void
   */
  public void enviarAPDUJoin(String nomeUsuario, String nomeGrupo) {
    Thread threadJoin = new Thread(() -> conectarESalvarAPDU("JOIN", nomeUsuario, nomeGrupo));
    threadJoin.start(); // Inicia a thread de envio JOIN
  }

  /*
   * ***************************************************************
   * Metodo: enviarAPDULeave
   * Funcao: Inicia uma thread para envio da APDU LEAVE ao servidor.
   * Parametros: String nomeUsuario - nome do usuário
   * String nomeGrupo - nome do grupo a ser deixado
   * Retorno: void
   */
  public void enviarAPDULeave(String nomeUsuario, String nomeGrupo) {
    Thread threadLeave = new Thread(() -> conectarESalvarAPDU("LEAVE", nomeUsuario, nomeGrupo));
    threadLeave.start(); // Inicia a thread de envio LEAVE
  }

  /*
   * ***************************************************************
   * Metodo: escolherNovoServidor
   * Funcao: Seleciona um novo servidor da lista de conhecidos e tenta reconexão
   * com a mesma APDU.
   * Parametros: String tipoMensagem - tipo da requisição (JOIN ou LEAVE)
   * String nomeUsuario - nome do usuário
   * String nomeGrupo - nome do grupo
   * Retorno: void
   */
  // private void escolherNovoServidor(String tipoMensagem, String nomeUsuario, String nomeGrupo) {
  //   List<String> listaServidores = new ArrayList<String>(app.getServidoresConhecidos());

  //   // if (listaServidores.isEmpty())
  //   // return;

  //   int total = listaServidores.size();
  //   indiceAtual++;
  //   if (indiceAtual >= total) {
  //     indiceAtual = 0;
  //   }

  //   // Pega apenas o próximo da lista
  //   // int proximoIndice = (indiceAtual + 1) % total;
  //   String ip = listaServidores.get(indiceAtual);
  //   host = ip;

  //   System.out.println("Tentando próximo servidor: " + ip);
  //   conectarESalvarAPDU(tipoMensagem, nomeUsuario, nomeGrupo);
  // }

  private void escolherNovoServidor(String tipoMensagem, String nomeUsuario, String nomeGrupo) {
    List<String> servidoresConhecidos = new ArrayList<>(app.getServidoresConhecidos());

    if (servidoresConhecidos.isEmpty()) {
      System.out.println("Nenhum servidor conhecido para tentar.");
      return;
    }

    // Ordena IPs em ordem crescente (alvo: consistência entre os clientes)
    Collections.sort(servidoresConhecidos);

    // Avança para o próximo da lista ordenada

    int indice = servidoresConhecidos.indexOf(host);
    if (indice == -1) {
      indice = 0; // ou talvez definir explicitamente como líder o primeiro da lista
    } else {
      indice++;
      if (indice >= servidoresConhecidos.size()) {
        indice = 0;
      }
    }
    
    // indiceAtual++;
    // if (indiceAtual >= servidoresConhecidos.size()) {
    //   indiceAtual = 0;
    // }

    String ipEscolhido = servidoresConhecidos.get(indice);
    host = ipEscolhido;

    System.out.println("Tentando próximo servidor (ordem ordenada): " + ipEscolhido);
    conectarESalvarAPDU(tipoMensagem, nomeUsuario, nomeGrupo);
  }

}
