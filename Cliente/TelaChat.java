/* ***************************************************************
* Autor............: Hugo Botelho Santana
* Matricula........: 202210485
* Inicio...........: 21/11/2024
* Ultima alteracao.: 28/11/2024
* Nome.............: Camada de Transporte/Aplicação - Aplicativo de Instant Messaging
* Funcao...........: Aplicativo de chat para troca de mensagens com o modelo cliente servidor
*************************************************************** */

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Priority;
import javafx.application.Platform;

public class TelaChat {
    private VBox layout = new VBox(10); // Layout principal da tela de chat
    private HistoricoMensagens historicoMensagens;
    private Principal app; // Instância principal do aplicativo
    private VBox listaMensagens; // Container para exibição das mensagens

    public TelaChat(Principal app, String nomeGrupo, HistoricoMensagens historicoMensagens) {
        this.app = app;
        this.historicoMensagens = historicoMensagens;

        layout.setStyle("-fx-alignment: top-center;");

        // Criando o Header
        HBox header = new HBox();
        header.setStyle("-fx-background-color: #333333; -fx-padding: 24px; -fx-alignment: center;");

        // icone de Voltar
        ImageView voltarIcon = new ImageView(new Image("/img/voltarIcon.png"));
        voltarIcon.setFitHeight(40);
        voltarIcon.setFitWidth(40);
        Button botaoVoltar = new Button();
        botaoVoltar.setGraphic(voltarIcon);
        botaoVoltar.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
        botaoVoltar.setOnAction(e -> {
            TelaMeusGrupos telaMeusGrupos = new TelaMeusGrupos(app);
            app.getRoot().getChildren().setAll(telaMeusGrupos.getLayout());
        });

        // Nome do Grupo
        Label nomeGrupoTexto = new Label(nomeGrupo);
        nomeGrupoTexto.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #E5AF18;");
        nomeGrupoTexto.setMaxWidth(400);
        nomeGrupoTexto.setWrapText(true);

        HBox.setHgrow(nomeGrupoTexto, Priority.ALWAYS);

        // icone de Sair do Grupo
        ImageView sairIcon = new ImageView(new Image("/img/sairIcon.png"));
        sairIcon.setFitHeight(40);
        sairIcon.setFitWidth(40);
        Button botaoSair = new Button();
        botaoSair.setGraphic(sairIcon);
        botaoSair.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
        botaoSair.setOnAction(e -> {
            enviarAPDULeave(nomeGrupo);
            TelaMeusGrupos.removerGrupo(nomeGrupo);
            TelaMeusGrupos telaMeusGrupos = new TelaMeusGrupos(app);
            app.getRoot().getChildren().setAll(telaMeusGrupos.getLayout());
        });

        header.getChildren().addAll(botaoVoltar, nomeGrupoTexto, botaoSair);

        // Lista de Mensagens
        listaMensagens = new VBox(10);
        listaMensagens.setStyle("-fx-padding: 10px; -fx-background-color: transparent;");
        renderizarMensagens();

        ScrollPane scrollMensagens = new ScrollPane(listaMensagens);
        scrollMensagens.setStyle("-fx-background: #F5F5F5; -fx-border-color: #F5F5F5;");
        scrollMensagens.setFitToWidth(true);
        scrollMensagens.setPrefHeight(500);

        // Campo para Envio de Mensagens
        HBox enviarMensagemLayout = new HBox(10);
        enviarMensagemLayout.setStyle("-fx-background-color: #E1E0E0; -fx-background-radius: 10px;");
        enviarMensagemLayout.setPadding(new Insets(10, 20, 10, 20));
        VBox.setMargin(enviarMensagemLayout, new Insets(10, 10, 10, 10));
        enviarMensagemLayout.setAlignment(Pos.CENTER_LEFT);

        ImageView iconeMensagem = new ImageView(new Image("/img/enviarIcon.png"));
        iconeMensagem.setFitHeight(24);
        iconeMensagem.setFitWidth(24);

        TextField campoMensagem = criarCampoComPlaceholder("Mensagem");
        campoMensagem.setMaxWidth(Double.MAX_VALUE);
        campoMensagem.setStyle("-fx-background-color: #E1E0E0; -fx-font-size: 16px; -fx-padding: 8px;");
        HBox.setHgrow(campoMensagem, Priority.ALWAYS);

        campoMensagem.setOnKeyPressed(event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
                String mensagem = campoMensagem.getText();
                if (!mensagem.isEmpty()) {
                    try {
                        String mensagemFormatada = "SEND|" + nomeGrupo + "|" + app.getNomeUsuario() + "|" + mensagem;
                        app.getClienteUDP().enviarMensagem(mensagemFormatada);

                        String horaAtual = java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));

                        // Adiciona mensagem ao histórico
                        Mensagem novaMensagem = new Mensagem("Voce", mensagem, horaAtual);
                        historicoMensagens.adicionarMensagem(novaMensagem);

                        // Atualiza as mensagens exibidas
                        renderizarMensagens();

                        // Limpa o campo de mensagem
                        campoMensagem.clear();
                    } catch (Exception e) {
                        System.err.println("Erro ao enviar mensagem: " + e.getMessage());
                    }
                }
            }
        });

        enviarMensagemLayout.getChildren().addAll(iconeMensagem, campoMensagem);
        layout.getChildren().addAll(header, scrollMensagens, enviarMensagemLayout);
    }

    public void renderizarMensagens() {
        Platform.runLater(() -> {
            listaMensagens.getChildren().clear();
            for (Mensagem mensagem : historicoMensagens.getMensagens()) {
                listaMensagens.getChildren().add(criarComponenteMensagem(mensagem));
            }
        });
    }

    private VBox criarComponenteMensagem(Mensagem mensagem) {
        VBox componenteMensagem = new VBox(5);

        Label remetenteLabel = new Label(mensagem.getRemetente());
        remetenteLabel.setStyle("-fx-font-weight: 500; -fx-text-fill: #B4B4B4;");

        Label conteudoMensagem = new Label(mensagem.getConteudo());
        conteudoMensagem.setStyle(
                "-fx-background-color: #333333; " +
                        "-fx-text-fill: #E5AF18; " +
                        "-fx-font-size: 16px; " +
                        "-fx-padding: 10px; " +
                        "-fx-background-radius: 10px; " +
                        "-fx-max-width: 250px; " +
                        "-fx-wrap-text: true;");

        Label horarioLabel = new Label(mensagem.getHora());
        horarioLabel.setStyle("-fx-font-weight: 500; -fx-text-fill: #B4B4B4;");

        if (mensagem.getRemetente().equals("Voce")) {
            componenteMensagem.setStyle("-fx-alignment: top-right;");
            conteudoMensagem.setStyle(
                    "-fx-background-color: #E5AF18; " +
                            "-fx-text-fill: #333333; " +
                            "-fx-font-size: 16px; " +
                            "-fx-padding: 10px; " +
                            "-fx-background-radius: 10px; " +
                            "-fx-max-width: 250px; " +
                            "-fx-wrap-text: true;");
        } else {
            componenteMensagem.setStyle("-fx-alignment: top-left;");
        }

        componenteMensagem.getChildren().addAll(remetenteLabel, conteudoMensagem, horarioLabel);
        return componenteMensagem;
    }

    private void enviarAPDULeave(String nomeGrupo) {
        try {
            app.getClienteTCP().enviarAPDULeave(app.getNomeUsuario(), nomeGrupo);
        } catch (Exception e) {
            System.err.println("Erro ao enviar APDU Leave: " + e.getMessage());
        }
    }

    private TextField criarCampoComPlaceholder(String placeholder) {
        TextField campo = new TextField();
        campo.setPromptText(placeholder);
        campo.setMaxWidth(300);
        campo.setStyle("-fx-background-color: #E1E0E0; -fx-font-size: 16px");

        campo.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                campo.setStyle("-fx-background-color: #E1E0E0; -fx-font-size: 16px");
            } else {
                campo.setStyle("-fx-background-color: #E1E0E0; -fx-font-size: 16px");
            }
        });

        return campo;
    }

    public VBox getLayout() {
        return layout;
    }
}
