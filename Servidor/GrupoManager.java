/* ***************************************************************
* Autor............: Hugo Botelho Santana
* Matricula........: 202210485
* Inicio...........: 21/11/2024
* Ultima alteracao.: 28/11/2024
* Nome.............: Camada de Transporte/Aplicação - Aplicativo de Instant Messaging
* Funcao...........: Aplicativo de chat para troca de mensagens com o modelo cliente servidor
*************************************************************** */
import java.util.*;

public class GrupoManager {
    // Estrutura para armazenar grupos e seus membros
    private final Map<String, Set<Usuario>> grupos;
    private Principal app;

    public GrupoManager(Principal app) {
        this.grupos = new HashMap<>();
        this.app = app;
    }

    // Adicionar usuário a um grupo
    public synchronized void adicionarUsuario(String nomeGrupo, Usuario usuario) {
        grupos.computeIfAbsent(nomeGrupo, k -> new HashSet<>()).add(usuario);
        
        Set<AtualizarServidores> servidores = app.getServidoresTCP();
        if (servidores != null){
            List<AtualizarServidores> servidoresTCP = new ArrayList<>(servidores);
            for (AtualizarServidores servidor : servidoresTCP) {
                servidor.enviarAPDUJoin(nomeGrupo, nomeGrupo);
            }
        }
    }

    // Remover usuário de um grupo
    public synchronized void removerUsuario(String nomeGrupo, Usuario usuario) {
        if (grupos.containsKey(nomeGrupo)) {
            grupos.get(nomeGrupo).remove(usuario);
            // Remove o grupo se ele estiver vazio
            if (grupos.get(nomeGrupo).isEmpty()) {
                grupos.remove(nomeGrupo);
            }
        }
        Set<AtualizarServidores> servidores = app.getServidoresTCP();
        if (servidores != null){
            List<AtualizarServidores> servidoresTCP = new ArrayList<>(servidores);
            for (AtualizarServidores servidor : servidoresTCP) {
                servidor.enviarAPDULeave(nomeGrupo, nomeGrupo);
            }
        }
    }

    // Obter membros de um grupo
    public synchronized Set<Usuario> obterMembros(String nomeGrupo) {
        return grupos.getOrDefault(nomeGrupo, Collections.emptySet());
    }

    // Verificar se um grupo existe
    public synchronized boolean grupoExiste(String nomeGrupo) {
        return grupos.containsKey(nomeGrupo);
    }
}
