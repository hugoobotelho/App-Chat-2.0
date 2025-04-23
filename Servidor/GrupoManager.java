
import java.util.*;

public class GrupoManager {
  // Estrutura para armazenar grupos e seus membros
  private final Map<String, Set<Usuario>> grupos;
  private Principal app;

  public GrupoManager(Principal app) {
    this.grupos = new HashMap<>();
    this.app = app;
  }

  /*
   * ***************************************************************
   * Metodo: adicionarUsuario
   * Funcao: Adiciona um usuário a um grupo. Cria o grupo se ele ainda não
   * existir.
   * Também envia APDU de "JOIN" para os servidores TCP conhecidos.
   * Parametros:
   * String nomeGrupo - nome do grupo
   * Usuario usuario - usuário que será adicionado
   * Retorno: void
   */
  public synchronized void adicionarUsuario(String nomeGrupo, Usuario usuario, Boolean isUpdate) {
    grupos.computeIfAbsent(nomeGrupo, k -> new HashSet<>()).add(usuario);
    if (!isUpdate) {
      Set<AtualizarServidores> servidores = app.getServidoresTCP();
      if (servidores != null) {
        List<AtualizarServidores> servidoresTCP = new ArrayList<>(servidores);
        for (AtualizarServidores servidor : servidoresTCP) {
          for (String message : app.getMessageLog()) {
            String[] partes = message.split("\\|");
            String tipo = partes[0].trim();
            String nomeUsuario = partes[1].trim();
            String nomeDoGrupo = partes[2].trim();
            String timeStamp = partes[3].trim();
            if (tipo.equals("JOIN")) {
              servidor.enviarAPDUJoin(nomeUsuario, nomeDoGrupo, timeStamp);
            } else if (tipo.equals("LEAVE")) {
              servidor.enviarAPDULeave(nomeUsuario, nomeDoGrupo, timeStamp);
            }
          }
          // servidor.enviarAPDUJoin(nomeGrupo, nomeGrupo);
        }
      }
    }

    imprimirGrupos();

  }

  /*
   * ***************************************************************
   * Metodo: removerUsuario
   * Funcao: Remove um usuário de um grupo. Remove o grupo completamente se ele
   * ficar vazio.
   * Também envia APDU de "LEAVE" para os servidores TCP conhecidos.
   * Parametros:
   * String nomeGrupo - nome do grupo
   * Usuario usuario - usuário que será removido
   * Retorno: void
   */
  public synchronized void removerUsuario(String nomeGrupo, Usuario usuario, Boolean isUpdate) {
    if (grupos.containsKey(nomeGrupo)) {
      grupos.get(nomeGrupo).remove(usuario);
      // Remove o grupo se ele estiver vazio
      if (grupos.get(nomeGrupo).isEmpty()) {
        grupos.remove(nomeGrupo);
      }
    }
    if (!isUpdate) {
      Set<AtualizarServidores> servidores = app.getServidoresTCP();
      if (servidores != null) {
        List<AtualizarServidores> servidoresTCP = new ArrayList<>(servidores);
        for (AtualizarServidores servidor : servidoresTCP) {
          for (String message : app.getMessageLog()) {
            String[] partes = message.split("\\|");
            String tipo = partes[0].trim();
            String nomeUsuario = partes[1].trim();
            String nomeDoGrupo = partes[2].trim();
            String timeStamp = partes[3].trim();
            if (tipo.equals("JOIN")) {
              servidor.enviarAPDUJoin(nomeUsuario, nomeDoGrupo, timeStamp);
            } else if (tipo.equals("LEAVE")) {
              servidor.enviarAPDULeave(nomeUsuario, nomeDoGrupo, timeStamp);
            }
          }
        }
        // servidor.enviarAPDULeave(nomeGrupo, nomeGrupo);
      }
    }
    
    imprimirGrupos();

  }

  /*
   * ***************************************************************
   * Metodo: obterMembros
   * Funcao: Retorna o conjunto de membros de um grupo específico.
   * Parametros: String nomeGrupo - nome do grupo
   * Retorno: Set<Usuario> - conjunto de usuários que pertencem ao grupo, ou
   * conjunto vazio se não existir
   */
  public synchronized Set<Usuario> obterMembros(String nomeGrupo) {
    return grupos.getOrDefault(nomeGrupo, Collections.emptySet());
  }

  /*
   * ***************************************************************
   * Metodo: grupoExiste
   * Funcao: Verifica se um grupo já existe no gerenciador.
   * Parametros: String nomeGrupo - nome do grupo a ser verificado
   * Retorno: boolean - true se o grupo existe, false caso contrário
   */
  public synchronized boolean grupoExiste(String nomeGrupo) {
    return grupos.containsKey(nomeGrupo);
  }

  /*
   * ***************************************************************
   * Metodo: imprimirGrupos
   * Funcao: Imprime todos os grupos e seus respectivos membros no console.
   * Parametros: nenhum
   * Retorno: void
   */
  public synchronized void imprimirGrupos() {
    if (grupos.isEmpty()) {
      System.out.println("Nenhum grupo cadastrado.");
      return;
    }

    System.out.println("==== GRUPOS ATUAIS ====");
    for (Map.Entry<String, Set<Usuario>> entry : grupos.entrySet()) {
      System.out.println("Grupo: " + entry.getKey());
      for (Usuario usuario : entry.getValue()) {
        System.out.println("  - " + usuario.getNome()); // ou .toString() se preferir
      }
    }
    System.out.println("========================");
  }
}
