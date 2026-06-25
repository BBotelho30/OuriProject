package com.meu.ourigame;

import com.meu.ourigame.model.JogoOuri;
import com.meu.ourigame.model.Tabuleiro;
import com.meu.ourigame.network.Client;
import com.meu.ourigame.network.NetworkConnection;
import com.meu.ourigame.network.Server;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import javafx.scene.layout.Region;
import javafx.scene.layout.Priority;
import javafx.scene.text.Text;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class OuriGameUI extends Application {

    private Stage stage;

    private JogoOuri jogo;
    private Button[][] botoesCasas;
    private Label lblJogadorAtual;
    private Label lblDepositoJ1;
    private Label lblDepositoJ2;
    private Label lblEstadoRede;

    private NetworkConnection connection;
    private boolean modoRede = false;
    private int jogadorLocal = -1;

    private String nomeJogador1 = "Jogador 1";
    private String nomeJogador2 = "Jogador 2";

    private Label lblDepositoLateralJ1;
    private Label lblDepositoLateralJ2;

    private Label lblNomeJ1;
    private Label lblNomeJ2;

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        mostrarMenuInicial();
    }

    private void mostrarMenuInicial() {

        Label titulo = new Label("Jogo Ouri");
        titulo.getStyleClass().add("title");

        Label subtitulo = new Label("Escolhe uma opção para começar!");
        subtitulo.getStyleClass().add("subtitle");

        Button btnJogar = criarBotaoMenu("▷", "Jogar", "btn-blue");
        btnJogar.setOnAction(e -> mostrarPaginaJogar());

        Button btnComoJogar = criarBotaoMenu("▷", "Como Jogar", "btn-purple");
        btnComoJogar.setOnAction(e -> mostrarComoJogar());

        Button btnRegras = criarBotaoMenu("🗎", "Regras", "btn-pink");
        btnRegras.setOnAction(e -> mostrarRegras());

        HBox botoes = new HBox(28, btnJogar, btnComoJogar, btnRegras);
        botoes.setAlignment(Pos.CENTER);

        VBox cardJogadores = criarCardInfo("♙", "Jogadores", "2", "card-icon-purple");
        VBox cardObjetivo = criarCardInfo("♕", "Objetivo", "Capturar\n24 sementes", "card-icon-yellow");
        VBox cardTabuleiro = criarCardInfo("▦", "Tabuleiro", "12 Cavidades", "card-icon-purple");

        HBox cards = new HBox(25, cardJogadores, cardObjetivo, cardTabuleiro);
        cards.setAlignment(Pos.CENTER);

        Label rodape = new Label("Escolhe uma opção para começar!");
        rodape.getStyleClass().add("subtitle");

        VBox painel = new VBox(25, titulo, subtitulo, botoes, cards, rodape);
        painel.getStyleClass().add("app-panel");
        painel.setAlignment(Pos.CENTER);
        painel.setPrefSize(900, 600);

        StackPane root = new StackPane(painel);
        root.setPadding(new Insets(40));

        Scene scene = new Scene(root, 900, 600);
        aplicarCss(scene);

        stage.setTitle("Ouri - Menu Inicial");
        stage.setScene(scene);
        stage.show();
    }

    private void iniciarJogoLocal() {
        modoRede = false;
        jogadorLocal = -1;
        connection = null;
        mostrarJogo();
    }

    private void iniciarServidor() {
        modoRede = true;
        jogadorLocal = 0;

        connection = new Server();
        connection.setOnMessage(this::receberMensagem);

        mostrarJogo();

        jogo.setJogadorAtual(0);
        atualizarInterface();

        connection.start();

        if (connection != null) {
            connection.send("NOME:" + nomeJogador1);
        }
    }

    private void iniciarCliente(String ipServidor) {
        modoRede = true;
        jogadorLocal = 1;

        connection = new Client(ipServidor);
        connection.setOnMessage(this::receberMensagem);

        mostrarJogo();

        jogo.setJogadorAtual(0);
        atualizarInterface();

        connection.start();

        if (connection != null) {
            connection.send("NOME:" + nomeJogador2);
        }
    }

    private void mostrarJogo() {
        jogo = new JogoOuri();
        botoesCasas = new Button[2][Tabuleiro.NUM_CASAS];

        BorderPane root = new BorderPane();
        root.getStyleClass().add("game-root");
        root.setPadding(new Insets(25));

        lblJogadorAtual = new Label();
        lblJogadorAtual.getStyleClass().add("turn-label");

        lblEstadoRede = new Label();
        lblEstadoRede.getStyleClass().add("subtitle");

        HBox barraJogadores = criarBarraJogadores();

        VBox topo = new VBox(30, barraJogadores);
        topo.setPadding(new Insets(20, 0, 25, 0));

        root.setTop(topo);

        root.setCenter(criarTabuleiro());
        root.setBottom(criarRodape());

        atualizarInterface();

        Scene scene = new Scene(root, 1000, 650);
        aplicarCss(scene);

        stage.setTitle("Ouri");
        stage.setScene(scene);
        stage.show();
    }

    private HBox criarTabuleiro() {
        GridPane casas = new GridPane();
        casas.setAlignment(Pos.CENTER);
        casas.setHgap(18);
        casas.setVgap(18);

        for (int i = 0; i < Tabuleiro.NUM_CASAS; i++) {
            Button btn = criarBotaoCasa(0, i);
            botoesCasas[0][i] = btn;
            casas.add(btn, i, 0);
        }

        for (int i = 0; i < Tabuleiro.NUM_CASAS; i++) {
            Button btn = criarBotaoCasa(1, i);
            botoesCasas[1][i] = btn;
            casas.add(btn, i, 1);
        }

        lblDepositoLateralJ1 = new Label();
        lblDepositoLateralJ1.getStyleClass().add("store-value");

        lblDepositoLateralJ2 = new Label();
        lblDepositoLateralJ2.getStyleClass().add("store-value");

        VBox depositoJ1 = criarDepositoApenasNumero(lblDepositoLateralJ1, "store-box-blue");
        VBox depositoJ2 = criarDepositoApenasNumero(lblDepositoLateralJ2, "store-box-pink");

        HBox tabuleiro = new HBox(28, depositoJ1, casas, depositoJ2);
        tabuleiro.getStyleClass().add("board-card");
        tabuleiro.setAlignment(Pos.CENTER);

        return tabuleiro;
    }

    private Button criarBotaoCasa(int jogador, int casa) {
        Button btn = new Button();
        btn.getStyleClass().add("house-button");
        btn.setOnAction(e -> jogarCasa(jogador, casa));
        return btn;
    }

    private BorderPane criarRodape() {
        Button btnNovoJogo = new Button("✕  Desistir / Novo Jogo");
        btnNovoJogo.getStyleClass().add("danger-button");

        Button btnMenu = new Button("⌂  Voltar ao Menu");
        btnMenu.getStyleClass().add("secondary-button");

        btnNovoJogo.setOnAction(e -> {
            jogo = new JogoOuri();
            atualizarInterface();

            if (modoRede && connection != null) {
                connection.send("NOVO");
            }
        });

        btnMenu.setOnAction(e -> {
            if (modoRede && connection != null) {
                connection.send("SAIR_MENU");
            }

            fecharLigacaoRede();
            mostrarMenuInicial();
        });

        lblEstadoRede.setAlignment(Pos.CENTER);

        BorderPane rodape = new BorderPane();
        rodape.setLeft(btnNovoJogo);
        rodape.setCenter(lblEstadoRede);
        rodape.setRight(btnMenu);
        rodape.setPadding(new Insets(20, 45, 10, 45));

        return rodape;
    }

    private void jogarCasa(int jogador, int casa) {
        if (modoRede && jogador != jogadorLocal) {
            mostrarMensagem("Jogada inválida", "Só podes jogar nas tuas casas.");
            return;
        }

        if (modoRede && jogo.getJogadorAtual() != jogadorLocal) {
            mostrarMensagem("Aguarda", "Ainda não é a tua vez.");
            return;
        }

        if (jogador != jogo.getJogadorAtual()) {
            mostrarMensagem("Jogada inválida", "Não é a vez desse jogador.");
            return;
        }

        boolean jogadaValida = jogo.jogar(casa);

        if (!jogadaValida) {
            mostrarMensagem("Jogada inválida", "Escolhe uma casa com mais de 1 semente.");
            return;
        }

        atualizarInterface();

        if (modoRede && connection != null) {
            connection.send("JOGADA:" + jogador + ":" + casa);
        }

        verificarFimDeJogo();
    }

    private void receberMensagem(String mensagem) {
        Platform.runLater(() -> {

            if (mensagem.startsWith("JOGADA:")) {
                String[] partes = mensagem.split(":");

                if (partes.length != 3) {
                    System.out.println("Mensagem de jogada inválida recebida: " + mensagem);
                    return;
                }

                int jogadorRecebido = Integer.parseInt(partes[1]);
                int casaRecebida = Integer.parseInt(partes[2]);

                jogo.setJogadorAtual(jogadorRecebido);

                boolean jogadaValida = jogo.jogar(casaRecebida);

                if (jogadaValida) {
                    atualizarInterface();
                    verificarFimDeJogo();
                }
            }

            if (mensagem.equals("NOVO")) {
                jogo = new JogoOuri();
                atualizarInterface();
            }

            if (mensagem.equals("SAIR_MENU")) {
                mostrarMensagem("Ligação terminada", "O outro jogador voltou ao menu.");
                fecharLigacaoRede();
                mostrarMenuInicial();
            }

            if (mensagem.startsWith("NOME:")) {
                String nomeRecebido = mensagem.substring(5);

                if (jogadorLocal == 0) {
                    nomeJogador2 = nomeRecebido;

                    if (connection != null) {
                        connection.send("NOME:" + nomeJogador1);
                    }

                } else {
                    nomeJogador1 = nomeRecebido;
                }

                atualizarInterface();
            }
        });
    }

    private void verificarFimDeJogo() {
        if (jogo.isFimDeJogo()) {
            mostrarMensagem("Fim de jogo", jogo.getVencedor());
        }
    }

    private void atualizarInterface() {
        if (lblNomeJ1 != null) {
            lblNomeJ1.setText(nomeJogador1);
        }

        if (lblNomeJ2 != null) {
            lblNomeJ2.setText(nomeJogador2);
        }

        Tabuleiro tabuleiro = jogo.getTabuleiro();

        for (int jogador = 0; jogador < 2; jogador++) {
            for (int casa = 0; casa < Tabuleiro.NUM_CASAS; casa++) {
                botoesCasas[jogador][casa].setText(String.valueOf(tabuleiro.getCasa(jogador, casa)));
            }
        }

        lblDepositoJ1.setText("Capturadas: " + tabuleiro.getDeposito(0));
        lblDepositoJ2.setText("Capturadas: " + tabuleiro.getDeposito(1));
        
        lblDepositoLateralJ1.setText(String.valueOf(tabuleiro.getDeposito(0)));
        lblDepositoLateralJ2.setText(String.valueOf(tabuleiro.getDeposito(1)));
        
        if (!modoRede) {

            String nomeAtual = jogo.getJogadorAtual() == 0
                    ? nomeJogador1
                    : nomeJogador2;

            lblJogadorAtual.setText("Vez de " + nomeAtual);

        } else {

            boolean minhaVez = jogo.getJogadorAtual() == jogadorLocal;

            if (minhaVez) {

                lblJogadorAtual.setText("🎮 É a tua vez!");

            } else {

                String nomeOponente = jogadorLocal == 0
                        ? nomeJogador2
                        : nomeJogador1;

                lblJogadorAtual.setText("⏳ À espera da jogada de " + nomeOponente);
            }
        };

        if (!modoRede) {
            lblEstadoRede.setText("Modo local");
        } else if (jogadorLocal == 0) {
            lblEstadoRede.setText("Modo rede: Servidor / Jogador 1");
        } else {
            lblEstadoRede.setText("Modo rede: Cliente / Jogador 2");
        }
    }

    private void mostrarMensagem(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    private void fecharLigacaoRede() {
        if (connection != null) {
            connection.close();
            connection = null;
        }

        modoRede = false;
        jogadorLocal = -1;
    }

    private void aplicarCss(Scene scene) {
        scene.getStylesheets().add(
                getClass().getResource("/css/style.css").toExternalForm()
        );
    }

    private VBox criarCardInfo(String icone, String titulo, String valor, String classeIcone) {
        Label lblIcone = new Label(icone);
        lblIcone.getStyleClass().add(classeIcone);

        Label lblTitulo = new Label(titulo);
        lblTitulo.getStyleClass().add("info-label");

        Label lblValor = new Label(valor);
        lblValor.getStyleClass().add("info-value");

        VBox texto = new VBox(5, lblTitulo, lblValor);
        texto.setAlignment(Pos.CENTER_LEFT);

        HBox linha = new HBox(18, lblIcone, texto);
        linha.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(linha);
        card.getStyleClass().add("info-card");
        card.setPrefWidth(230);
        card.setPrefHeight(110);
        card.setAlignment(Pos.CENTER_LEFT);

        return card;
    }

    private void mostrarRegras() {
        Button btnVoltarTopo = new Button("← Voltar");
        btnVoltarTopo.getStyleClass().add("top-back");
        btnVoltarTopo.setOnAction(e -> mostrarMenuInicial());

        Label titulo = new Label("Regras do Jogo");
        titulo.getStyleClass().add("page-title");

        VBox conteudo = new VBox(13);
        conteudo.getStyleClass().add("content-card");
        conteudo.setMaxWidth(830);

        conteudo.getChildren().add(criarPasso("1",
                "O jogo inicia com 4 sementes em cada uma das 12 cavidades do tabuleiro."));

        conteudo.getChildren().add(criarDivisor());

        conteudo.getChildren().add(criarPasso("2",
                "Os jogadores jogam à vez, escolhendo uma cavidade do seu lado para iniciar a distribuição das sementes."));

        conteudo.getChildren().add(criarDivisor());

        conteudo.getChildren().add(criarPasso("3",
                "As sementes devem ser colocadas uma a uma, seguindo o sentido anti-horário."));

        conteudo.getChildren().add(criarDivisor());

        conteudo.getChildren().add(criarPasso("4",
                "Se a última semente cair numa cavidade do adversário e essa cavidade passar a ter 2 ou 3 sementes, o jogador recolhe essas sementes, assim como as das cavidades anteriores do lado adversário que também tenham 2 ou 3 sementes."));

        conteudo.getChildren().add(criarDivisor());

        conteudo.getChildren().add(criarPasso("5",
                "Não é permitido realizar jogadas que deixem o adversário sem sementes disponíveis."));

        conteudo.getChildren().add(criarDivisor());

        conteudo.getChildren().add(criarPasso("6",
                "A partida termina quando um dos jogadores alcançar 24 sementes capturadas."));

        VBox centro = new VBox(25, titulo, conteudo);
        centro.setAlignment(Pos.CENTER);

        BorderPane root = new BorderPane();
        root.setTop(btnVoltarTopo);
        root.setCenter(centro);
        root.setPadding(new Insets(22, 34, 35, 34));

        Scene scene = new Scene(root, 1000, 650);
        aplicarCss(scene);

        stage.setTitle("Ouri - Regras");
        stage.setScene(scene);
        stage.show();
    }

    private void mostrarComoJogar() {
        Button btnVoltarTopo = new Button("← Voltar");
        btnVoltarTopo.getStyleClass().add("top-back");
        btnVoltarTopo.setOnAction(e -> mostrarMenuInicial());

        Label titulo = new Label("Como Jogar");
        titulo.getStyleClass().add("page-title");

        VBox conteudo = new VBox(18);
        conteudo.getStyleClass().add("content-card");
        conteudo.setMaxWidth(760);

        conteudo.getChildren().add(criarPasso("1",
                "Escolha uma cavidade do seu lado do tabuleiro para começar a distribuir as sementes."));

        conteudo.getChildren().add(criarDivisor());

        conteudo.getChildren().add(criarPasso("2",
                "As sementes são distribuídas uma a uma pelas cavidades seguintes, no sentido anti-horário."));

        conteudo.getChildren().add(criarDivisor());

        conteudo.getChildren().add(criarPasso("3",
                "Se a última semente cair numa cavidade do adversário e essa cavidade ficar com 2 ou 3 sementes, o jogador captura essas sementes e também as cavidades anteriores do adversário que tenham igualmente 2 ou 3 sementes."));

        conteudo.getChildren().add(criarDivisor());

        conteudo.getChildren().add(criarPasso("4",
                "Ganha o jogador que conseguir capturar 24 ou mais sementes primeiro."));

        VBox centro = new VBox(35, titulo, conteudo);
        centro.setAlignment(Pos.CENTER);

        BorderPane root = new BorderPane();
        root.setTop(btnVoltarTopo);
        root.setCenter(centro);
        root.setPadding(new Insets(22, 34, 35, 34));

        Scene scene = new Scene(root, 1000, 650);
        aplicarCss(scene);

        stage.setTitle("Ouri - Como Jogar");
        stage.setScene(scene);
        stage.show();
    }

    private HBox criarPasso(String numero, String texto) {

        Label circulo = new Label(numero);
        circulo.getStyleClass().add("step-circle");

        Text descricao = new Text(texto);

        descricao.setFill(Color.WHITE);
        descricao.setStyle(
                "-fx-font-size: 16px;"
        );

        descricao.wrappingWidthProperty().bind(stage.widthProperty().subtract(260));

        HBox linha = new HBox(25, circulo, descricao);
        linha.setAlignment(Pos.CENTER_LEFT);

        return linha;
    }

    private Region criarDivisor() {
        Region divisor = new Region();
        divisor.getStyleClass().add("divider");
        return divisor;
    }

    private Button criarBotaoMenu(String icone, String texto, String... classes) {

        Label lblIcone = new Label(icone);
        lblIcone.getStyleClass().add("menu-button-icon");

        Label lblTexto = new Label(texto);
        lblTexto.getStyleClass().add("menu-button-text");

        HBox conteudo = new HBox(12, lblIcone, lblTexto);
        conteudo.setAlignment(Pos.CENTER);

        Button btn = new Button();
        btn.setGraphic(conteudo);

        btn.getStyleClass().add("menu-button");

        for (String classe : classes) {
            btn.getStyleClass().add(classe);
        }

        return btn;
    }

    private void mostrarPaginaJogar() {
        Button btnVoltarTopo = new Button("← Voltar");
        btnVoltarTopo.getStyleClass().add("top-back");
        btnVoltarTopo.setOnAction(e -> mostrarMenuInicial());

        Label titulo = new Label("Jogar");
        titulo.getStyleClass().add("page-title");

        TextField txtNome = new TextField();
        txtNome.setPromptText("Nickname");
        txtNome.getStyleClass().add("server-input");

        TextField txtIp = new TextField();
        txtIp.setPromptText("IP do servidor");
        txtIp.getStyleClass().add("server-input");
        txtIp.setVisible(false);
        txtIp.setManaged(false);

        Button btnCriarServidor = criarBotaoMenu("⌂", "Criar Servidor", "btn-blue");
        Button btnLigarServidor = criarBotaoMenu("⇄", "Ligar ao Servidor", "btn-purple");

        Button btnEntrar = criarBotaoMenu("➜", "Entrar", "btn-pink");
        btnEntrar.setVisible(false);
        btnEntrar.setManaged(false);

        btnCriarServidor.setOnAction(e -> {
            String nome = txtNome.getText().trim();

            if (nome.isEmpty()) {
                mostrarMensagem("Nome obrigatório", "Escreve o teu nickname.");
                return;
            }

            nomeJogador1 = nome;
            nomeJogador2 = "Jogador 2";

            iniciarServidor();
        });

        btnLigarServidor.setOnAction(e -> {
            txtIp.setVisible(true);
            txtIp.setManaged(true);
            btnEntrar.setVisible(true);
            btnEntrar.setManaged(true);
        });

        btnEntrar.setOnAction(e -> {
            String nome = txtNome.getText().trim();
            String ip = txtIp.getText().trim();

            if (nome.isEmpty()) {
                mostrarMensagem("Nome obrigatório", "Escreve o teu nickname.");
                return;
            }

            if (ip.isEmpty()) {
                mostrarMensagem("IP obrigatório", "Escreve o IP do servidor.");
                return;
            }

            nomeJogador1 = "Jogador 1";
            nomeJogador2 = nome;

            iniciarCliente(ip);
        });

        VBox form = new VBox(22, titulo, txtNome, btnCriarServidor, btnLigarServidor, txtIp, btnEntrar);
        form.getStyleClass().add("server-card");
        form.setAlignment(Pos.CENTER);
        form.setMaxWidth(560);

        BorderPane root = new BorderPane();
        root.setTop(btnVoltarTopo);
        root.setCenter(form);
        root.setPadding(new Insets(25, 40, 40, 40));

        Scene scene = new Scene(root, 1000, 650);
        aplicarCss(scene);

        stage.setTitle("Ouri - Jogar");
        stage.setScene(scene);
        stage.show();
    }

    private VBox criarDeposito(String titulo, Label valor, String classe) {
        Label lblTitulo = new Label(titulo);
        lblTitulo.getStyleClass().add("store-title");

        valor.getStyleClass().add("store-value");

        VBox box = new VBox(10, lblTitulo, valor);
        box.getStyleClass().add(classe);
        box.setAlignment(Pos.CENTER);

        return box;
    }

    private HBox criarBarraJogadores() {
        Circle corJ1 = new Circle(8);
        corJ1.setFill(Color.web("#2d8cff"));

        lblNomeJ1 = new Label(nomeJogador1);
        lblNomeJ1.getStyleClass().add("player-name-blue");

        HBox jogador1Topo = new HBox(8, corJ1, lblNomeJ1);
        jogador1Topo.setAlignment(Pos.CENTER_LEFT);

        lblDepositoJ1 = new Label();
        lblDepositoJ1.getStyleClass().add("player-score-blue");

        VBox jogador1 = new VBox(6, jogador1Topo, lblDepositoJ1);
        jogador1.setAlignment(Pos.CENTER_LEFT);

        VBox centro = new VBox(lblJogadorAtual);
        centro.setAlignment(Pos.CENTER);

        Circle corJ2 = new Circle(8);
        corJ2.setFill(Color.web("#e83f98"));

        lblNomeJ2 = new Label(nomeJogador2);
        lblNomeJ2.getStyleClass().add("player-name-pink");

        HBox jogador2Topo = new HBox(8, lblNomeJ2, corJ2);
        jogador2Topo.setAlignment(Pos.CENTER_RIGHT);

        lblDepositoJ2 = new Label();
        lblDepositoJ2.getStyleClass().add("player-score-pink");

        VBox jogador2 = new VBox(6, jogador2Topo, lblDepositoJ2);
        jogador2.setAlignment(Pos.CENTER_RIGHT);

        Region espaco1 = new Region();
        Region espaco2 = new Region();

        HBox.setHgrow(espaco1, Priority.ALWAYS);
        HBox.setHgrow(espaco2, Priority.ALWAYS);

        HBox barra = new HBox(20, jogador1, espaco1, centro, espaco2, jogador2);
        barra.getStyleClass().add("players-bar");
        barra.setAlignment(Pos.CENTER);

        return barra;
    }

    private VBox criarDepositoApenasNumero(Label valor, String classe) {
        VBox box = new VBox(valor);
        box.getStyleClass().add(classe);
        box.setAlignment(Pos.CENTER);
        return box;
    }
    
    private void enviarQuandoPronto(String mensagem) {
        new Thread(() -> {
            try {
                Thread.sleep(800);

                if (connection != null) {
                    connection.send(mensagem);
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

}