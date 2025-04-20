import java.net.*;

public class ClienteUDP {
    private final DatagramSocket clienteSocket;
    private final DatagramSocket clienteSocket1;
    private InetAddress enderecoServidor;
    private final int portaServidor;

    public ClienteUDP (String ipServidor, int portaServidor) throws Exception {
        this.clienteSocket = new DatagramSocket(); // Socket para comunicação UDP
        this.enderecoServidor = InetAddress.getByName(ipServidor); // Endereço do servidor
        this.portaServidor = portaServidor;
        this.clienteSocket1 = new DatagramSocket(9876); // Escolha uma porta fixada
    }

    /*
     * ***************************************************************
     * Metodo: enviarRequisicao
     * Funcao: envia a mensagem pelo socket criado
     * Parametros: uma mensagem do tipo string.
     * Retorno: sem retorno
     * ***************************************************************
     */
    public void enviarMensagem(String mensagem) {
        try {
            byte[] dadosEnvio = mensagem.getBytes();
            DatagramPacket pacoteEnvio = new DatagramPacket(
                    dadosEnvio,
                    dadosEnvio.length,
                    enderecoServidor,
                    portaServidor);
            clienteSocket.send(pacoteEnvio);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * ***************************************************************
     * Metodo: receberHorario
     * Funcao: retorna o horario que recebeu do servidor
     * Parametros: sem parametro.
     * Retorno: retorna o horario do tipo String
     * ***************************************************************
     */
    public String receberMensagem() {
        try {
            byte[] dadosRecebidos = new byte[1024];
            DatagramPacket pacoteRecebido = new DatagramPacket(dadosRecebidos, dadosRecebidos.length);
            clienteSocket1.receive(pacoteRecebido); // Bloqueia até receber um pacote
            return new String(pacoteRecebido.getData(), 0, pacoteRecebido.getLength());
        } catch (Exception e) {
            e.printStackTrace();
            return "Erro";
        }
    }

    /*
     * ***************************************************************
     * Metodo: setIpServidor
     * Funcao: atualiza o ip do servidor
     * Parametros: recebe um novoIP do tipo string.
     * Retorno: sem retorno
     * ***************************************************************
     */
    public void setIpServidor(String novoIP) {
        try {
            enderecoServidor = InetAddress.getByName(novoIP);
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    /*
     * ***************************************************************
     * Metodo: fechar
     * Funcao: da um socket.close para fechar o socket
     * Parametros: sem parametro.
     * Retorno: sem retorno
     * ***************************************************************
     */
    public void fechar() {
        // escutando = false; // Para a thread de escuta
        clienteSocket.close();
    }
}
