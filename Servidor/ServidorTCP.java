
/* ***************************************************************
* Autor............: Hugo Botelho Santana
* Matricula........: 202210485
* Inicio...........: 21/11/2024
* Ultima alteracao.: 28/11/2024
* Nome.............: Camada de Transporte/Aplicação - Aplicativo de Instant Messaging
* Funcao...........: Aplicativo de chat para troca de mensagens com o modelo cliente servidor
*************************************************************** */
import java.net.*;
import java.io.*;
import java.util.Map;

public class ServidorTCP {

  private final GrupoManager grupoManager;
  private final Map<String, Usuario> usuarios;

  public ServidorTCP(GrupoManager grupoManager, Map<String, Usuario> usuarios) {
    this.grupoManager = grupoManager;
    this.usuarios = usuarios;
  }

  public void iniciar() {
    try {
      int portaLocal = 6789;
      ServerSocket servidorSocket = new ServerSocket(portaLocal); // Socket do servidor
      System.out.println("Servidor TCP iniciado na porta " + portaLocal + "...");

      while (true) {
        Socket conexao = servidorSocket.accept(); // Aceita conexões de clientes
        new Thread(new ProcessaCliente(conexao)).start();
      }
    } catch (Exception e) {
      System.err.println("Erro no servidor TCP: " + e.getMessage());
    }
  }

  private class ProcessaCliente implements Runnable {
    private final Socket conexao;

    public ProcessaCliente(Socket conexao) {
      this.conexao = conexao;
    }

    @Override
    public void run() {
      ObjectInputStream entrada = null;
      ObjectOutputStream saida = null;
      try {
        entrada = new ObjectInputStream(conexao.getInputStream());
        String mensagemRecebida = (String) entrada.readObject(); // Lê a mensagem do cliente
        System.out.println("Mensagem recebida via TCP: " + mensagemRecebida);

        String resposta = processarMensagem(mensagemRecebida, conexao);

        saida = new ObjectOutputStream(conexao.getOutputStream());
        saida.writeObject(resposta); // Envia a resposta
        saida.flush(); // Garante que a resposta será enviada ao cliente

      } catch (IOException e) {
        System.err.println("Erro de I/O ao processar cliente: " + e.getMessage());
      } catch (ClassNotFoundException e) {
        System.err.println("Erro ao ler objeto do cliente: " + e.getMessage());
      } finally {
        try {
          if (entrada != null)
            entrada.close();
          if (saida != null)
            saida.close();
          conexao.close(); // Fechar conexão ao final
        } catch (IOException e) {
          System.err.println("Erro ao fechar a conexão: " + e.getMessage());
        }
      }
    }

    private String processarMensagem(String mensagem, Socket conexao) {
      String[] partes = mensagem.split("\\|");
      if (partes.length < 3) {
        return "Erro: Mensagem mal formatada. Esperado TIPO|USUARIO|GRUPO.";
      }

      String tipo = partes[0].trim();
      String nomeUsuario = partes[1].trim();
      String nomeGrupo = partes[2].trim();

      Usuario usuario;
      synchronized (usuarios) {
        usuario = usuarios.computeIfAbsent(nomeUsuario,
            k -> new Usuario(nomeUsuario, conexao.getInetAddress(), conexao.getPort()));
      }

      synchronized (grupoManager) {
        switch (tipo.toUpperCase()) {
          case "JOIN":
            grupoManager.adicionarUsuario(nomeGrupo, usuario);
            return "Usuário " + nomeUsuario + " adicionado ao grupo " + nomeGrupo;

          case "LEAVE":
            if (grupoManager.grupoExiste(nomeGrupo)) {
              grupoManager.removerUsuario(nomeGrupo, usuario);
              return "Usuário " + nomeUsuario + " removido do grupo " + nomeGrupo;
            } else {
              return "Erro: Grupo " + nomeGrupo + " não existe.";
            }

          default:
            return "Erro: Tipo de mensagem desconhecido. Use JOIN ou LEAVE.";
        }
      }
    }
  }
}
