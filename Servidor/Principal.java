/* ***************************************************************
* Autor............: Hugo Botelho Santana
* Matricula........: 202210485
* Inicio...........: 19/04/2025
* Ultima alteracao.: 20/04/2025
* Nome.............: Programa de Chat/WhatZap com múltiplos servidores (conexões UDP e TCP)
* Funcao...........: Aplicativo de chat para troca de mensagens com o modelo n clientes e n servidores
*************************************************************** */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Principal { 

  private static final Map<String, Usuario> usuarios = new HashMap<>(); // Gerencia usuários
  private Set<String> servidoresConhecidos;
  private static DescobrirServidores descobrirServidores;
  private static Principal app;
  private static GrupoManager grupoManager; // Gerencia grupos
  private Set<AtualizarServidores> servidoresTCP = new HashSet<>();

  public static void main(String[] args) {

    app = new Principal();

    descobrirServidores = new DescobrirServidores(app);
    descobrirServidores.iniciarDescobrimento();

    grupoManager = new GrupoManager(app);

    // Inicia o servidor UDP em uma thread separada
    Thread servidorUDPThread = new Thread(() -> {
      ServidorUDP servidorUDP = new ServidorUDP(grupoManager, usuarios);
      servidorUDP.iniciar();
    });

    // Inicia o servidor TCP em uma thread separada
    Thread servidorTCPThread = new Thread(() -> {
      ServidorTCP servidorTCP = new ServidorTCP(grupoManager, usuarios);
      servidorTCP.iniciar();
    });

    // Inicia as threads
    servidorUDPThread.start();
    servidorTCPThread.start();

    System.out.println("Servidores UDP e TCP iniciados...");
  }

  /* ***************************************************************
  * Metodo: getGrupoManager
  * Funcao: Retorna a instância do gerenciador de grupos (GrupoManager).
  * Parametros: nenhum
  * Retorno: GrupoManager - instância do gerenciador de grupos
  *************************************************************** */
  public static GrupoManager getGrupoManager() {
    return grupoManager;
  }

  /* ***************************************************************
  * Metodo: getUsuarios
  * Funcao: Retorna o mapa de usuários cadastrados na aplicação.
  * Parametros: nenhum
  * Retorno: Map<String, Usuario> - mapa contendo os usuários registrados
  *************************************************************** */
  public static Map<String, Usuario> getUsuarios() {
    return usuarios;
  }
 /* ***************************************************************
  * Metodo: getServidoresConhecidos
  * Funcao: Retorna o conjunto de IPs dos servidores conhecidos detectados na rede.
  * Parametros: nenhum
  * Retorno: Set<String> - conjunto de IPs de servidores conhecidos
  *************************************************************** */
  public Set<String> getServidoresConhecidos() {
    return servidoresConhecidos;
  }
  /* ***************************************************************
  * Metodo: setServidoresConhecidos
  * Funcao: Adiciona um novo servidor à lista de servidores conhecidos, evitando duplicatas,
  *         e inicializa uma thread AtualizarServidores para ele.
  * Parametros: String novoServidor - IP do novo servidor a ser adicionado
  * Retorno: void
  *************************************************************** */
  public void setServidoresConhecidos(String novoServidor) {
    if (!servidoresConhecidos.contains(novoServidor)) {
      AtualizarServidores atualizarServidores = new AtualizarServidores(novoServidor, 6789, app);
      servidoresTCP.add(atualizarServidores);
      servidoresConhecidos.add(novoServidor);
      System.out.println("Servidor adicionado: " + novoServidor);
    }

    // if (ipServidor == null) {
    // ipServidor = novoServidor;
    // if (clienteTCP == null) {
    // clienteTCP = new ClienteTCP(ipServidor, 6789, this);
    // }
    // if (clienteUDP == null) {
    // criarClienteUDP(ipServidor, 6789);
    // }
    // }
  }
  /* ***************************************************************
  * Metodo: getServidoresTCP
  * Funcao: Retorna o conjunto de threads AtualizarServidores conectadas a servidores TCP conhecidos.
  * Parametros: nenhum
  * Retorno: Set<AtualizarServidores> - conjunto de threads responsáveis pela comunicação com servidores TCP
  *************************************************************** */
  public Set<AtualizarServidores> getServidoresTCP() {
    return servidoresTCP;
  }
}
