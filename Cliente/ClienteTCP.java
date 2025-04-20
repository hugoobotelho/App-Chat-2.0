/* ***************************************************************
* Autor............: Hugo Botelho Santana
* Matricula........: 202210485
* Inicio...........: 21/11/2024
* Ultima alteracao.: 28/11/2024
* Nome.............: Camada de Transporte/Aplicação - Aplicativo de Instant Messaging
* Funcao...........: Aplicativo de chat para troca de mensagens com o modelo cliente servidor
*************************************************************** */

import java.io.*;
import java.net.*;
import java.util.ArrayList;
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

    // Método para conectar ao servidor e enviar a APDU
    private void conectarESalvarAPDU(String tipoMensagem, String nomeUsuario, String nomeGrupo) {
        try (Socket socket = new Socket(host, porta)) {
            System.out.println("Conectado ao servidor " + host + ":" + porta);
            app.setIpServidor(host); //atualiza o servidor ativo 

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
            escolherNovoServidor(tipoMensagem, nomeUsuario, nomeGrupo); // Se falhar, tenta outro
        }

    }

    // Método para enviar a APDU JOIN em uma thread
    public void enviarAPDUJoin(String nomeUsuario, String nomeGrupo) {
        Thread threadJoin = new Thread(() -> conectarESalvarAPDU("JOIN", nomeUsuario, nomeGrupo));
        threadJoin.start(); // Inicia a thread de envio JOIN
    }

    // Método para enviar a APDU LEAVE em uma thread
    public void enviarAPDULeave(String nomeUsuario, String nomeGrupo) {
        Thread threadLeave = new Thread(() -> conectarESalvarAPDU("LEAVE", nomeUsuario, nomeGrupo));
        threadLeave.start(); // Inicia a thread de envio LEAVE
    }

    private void escolherNovoServidor(String tipoMensagem, String nomeUsuario, String nomeGrupo) {
        List<String> listaServidores = new ArrayList<String>(app.getServidoresConhecidos());

        if (listaServidores.isEmpty())
            return;

        int total = listaServidores.size();
        indiceAtual++;
        if (indiceAtual >= total) {
            indiceAtual = 0;
        }

        // Pega apenas o próximo da lista
        // int proximoIndice = (indiceAtual + 1) % total;
        String ip = listaServidores.get(indiceAtual);
        host = ip;

        System.out.println("Tentando próximo servidor: " + ip);
        conectarESalvarAPDU(tipoMensagem, nomeUsuario, nomeGrupo);
    }
}
