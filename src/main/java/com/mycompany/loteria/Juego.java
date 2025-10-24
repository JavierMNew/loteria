/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.loteria;

import com.mycompany.clases.ReproductorDeSonido;
import com.mycompany.conexionSQLServer.Conexion;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author javie
 */
public class Juego extends javax.swing.JFrame {

    public void close() {
        WindowEvent closeWindow = new WindowEvent(this, WindowEvent.WINDOW_CLOSING);
        Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(closeWindow);
    }

    /**
     * Creates new form NewJFrame
     */
    private ReproductorDeSonido reproductor;
    private int posicionCartaActual = 1;
    private int[] ordenCartasActuales;
    private boolean ordenCreado = false;

    private javax.swing.Timer timerCartaActual;
    private boolean juegoIniciado = false;

    private void inicializarTimer() {
        timerCartaActual = new javax.swing.Timer(2000, new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                if (juegoIniciado) {
                    cargarCartaActual();
                }
            }
        });

        timerCartaActual.setRepeats(true);
    }

    private void iniciarTimerCartas() {
        if (timerCartaActual != null) {
            juegoIniciado = true;
            timerCartaActual.start();
        }
    }

    private void detenerTimerCartas() {
        if (timerCartaActual != null && timerCartaActual.isRunning()) {
            timerCartaActual.stop();
            juegoIniciado = false;
            System.out.println("Timer de cartas detenido");
        }
    }

    private void pausarReanudarTimer() {
        if (timerCartaActual != null) {
            if (timerCartaActual.isRunning()) {
                timerCartaActual.stop();
            } else {
                timerCartaActual.start();
            }
        }
    }

    private void crearOrdenAleatorio() {
        ordenCartasActuales = new int[54];

        for (int i = 0; i < 54; i++) {
            ordenCartasActuales[i] = i + 1;
        }

        java.util.Random random = new java.util.Random();
        for (int i = 53; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int temp = ordenCartasActuales[i];
            ordenCartasActuales[i] = ordenCartasActuales[j];
            ordenCartasActuales[j] = temp;
        }

        ordenCreado = true;
        posicionCartaActual = 0;
    }

    private void configurarBotonTamañoFijo(javax.swing.JButton boton) {
        boton.setPreferredSize(new java.awt.Dimension(100, 150));
        boton.setMinimumSize(new java.awt.Dimension(100, 150));
        boton.setMaximumSize(new java.awt.Dimension(100, 150));
        boton.setSize(100, 150);

        boton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        boton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        boton.setIconTextGap(0);
        boton.setFocusPainted(false);
        boton.setBorderPainted(true);
        boton.setContentAreaFilled(true);
    }

    private void configurarBotonesIniciales() {
        javax.swing.JButton[] botones = {
            carta1, carta2, carta3, carta4,
            carta5, carta6, carta7, carta8,
            carta9, carta10, carta11, carta12,
            carta13, carta14, carta15, carta16
        };

        for (javax.swing.JButton boton : botones) {
            configurarBotonTamañoFijo(boton);
        }
    }

    private void procesarClickCarta(javax.swing.JButton boton) {
        if (!boton.isEnabled()) {
            return;
        }

        if (reproductor != null) {
            reproductor.detener();
        }

        ReproductorDeSonido clipBoton = new ReproductorDeSonido();
        clipBoton.cargarSonido("src\\main\\resources\\ping.wav");
        clipBoton.reproducir();

        boton.setEnabled(false);
    }

    public Juego() {
        initComponents();
        configurarBotonesIniciales();
        btnReiniciar.setVisible(false);
        btnIniciar.setVisible(true);
        btnSigCarta.setVisible(false);
        btnSigCarta.setVisible(false); //quitar para modo maualn
        btnPausa.setVisible(false);
        inicializarTimer();
    }

    //1x1
    private void reiniciarContadorCartasActuales() {
        posicionCartaActual = 1;
        System.out.println("restart en 1");
    }

    private void cargarCartaActual() {
        try {
            if (!ordenCreado) {
                crearOrdenAleatorio();
            }

            Conexion conexion = new Conexion();
            Connection conn = conexion.establecerConexion();

            int cveCartaActual = ordenCartasActuales[posicionCartaActual];

            String sqlCarta = "SELECT c.cve_carta, c.nombre_carta, ci.ruta_imagen "
                    + "FROM cartas c "
                    + "JOIN cartas_imagen_grandes ci ON c.cve_carta = ci.cve_carta "
                    + "WHERE c.cve_carta = ?";

            java.sql.PreparedStatement pst = conn.prepareStatement(sqlCarta);
            pst.setInt(1, cveCartaActual);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                String nombreCarta = rs.getString("nombre_carta");
                String rutaImagen = rs.getString("ruta_imagen");

                java.net.URL imageURL = getClass().getResource(rutaImagen);

                if (imageURL != null) {
                    javax.swing.ImageIcon icono = new javax.swing.ImageIcon(imageURL);

                    java.awt.Image imgEscalada = icono.getImage().getScaledInstance(
                            cartaAcual.getWidth(), cartaAcual.getHeight(),
                            java.awt.Image.SCALE_SMOOTH
                    );

                    cartaAcual.setIcon(new javax.swing.ImageIcon(imgEscalada));
                    cartaAcual.setToolTipText(nombreCarta);
                    /*String txtCartaActual = nombreCarta;
                    tittleCartaActual.setText(txtCartaActual);*/

                    System.out.println(" actual  " + (posicionCartaActual + 1) + ", cve_carta " + cveCartaActual + "): " + nombreCarta);

                    posicionCartaActual++;

                    if (posicionCartaActual >= 54) {
                        ordenCreado = false;
                    }

                } else {
                    System.out.println("errore n la imagen " + rutaImagen);
                }
            }

            rs.close();
            pst.close();
            conn.close();

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

    }

    //alltodas, creamos mazo temporal en la bd y ponemos aleatoriamente en cada boton con  un array de botones  get strin y java.net.URL imageURL = getClass().getResource(rutaImagen);
    private void cargarTableroCompleto() {
        try {
            Conexion conexion = new Conexion();
            Connection conn = conexion.establecerConexion();

            try {
                String sqlLimpiarPrevio = "IF OBJECT_ID('tempdb..#mazo') IS NOT NULL DROP TABLE #mazo";
                Statement stLimpiarPrevio = conn.createStatement();
                stLimpiarPrevio.executeUpdate(sqlLimpiarPrevio);
                stLimpiarPrevio.close();
            } catch (SQLException e) {
            }

            String sqlCrearMazo = "SELECT ROW_NUMBER() OVER (ORDER BY NEWID()) AS orden, * "
                    + "INTO #mazo "
                    + "FROM cartas";

            Statement stCrear = conn.createStatement();
            stCrear.executeUpdate(sqlCrearMazo);
            stCrear.close();

            String sqlCartas = "SELECT m.orden, m.cve_carta, m.nombre_carta, ci.ruta_imagen "
                    + "FROM #mazo m "
                    + "JOIN cartas_imagen_pequeñas ci ON m.cve_carta = ci.cve_carta "
                    + "WHERE m.orden <= 16 "
                    + "ORDER BY m.orden";

            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sqlCartas);

            javax.swing.JButton[] botones = {
                carta1, carta2, carta3, carta4,
                carta5, carta6, carta7, carta8,
                carta9, carta10, carta11, carta12,
                carta13, carta14, carta15, carta16
            };

            for (int i = 0; i < 16; i++) {
                configurarBotonTamañoFijo(botones[i]);
                botones[i].setIcon(null);
                botones[i].setText("");
                botones[i].setToolTipText("");
            }

            int indice = 0;
            while (rs.next() && indice < 16) {
                int orden = rs.getInt("orden");
                String nombreCarta = rs.getString("nombre_carta");
                String rutaImagen = rs.getString("ruta_imagen");

                java.net.URL imageURL = getClass().getResource(rutaImagen);

                if (imageURL != null) {
                    javax.swing.ImageIcon icono = new javax.swing.ImageIcon(imageURL);

                    java.awt.Image imgEscalada = icono.getImage().getScaledInstance(
                            100, 150,
                            java.awt.Image.SCALE_SMOOTH
                    );

                    botones[indice].setIcon(new javax.swing.ImageIcon(imgEscalada));
                    botones[indice].setToolTipText(nombreCarta);

                    System.out.println("posicon " + (indice + 1) + " (orden  " + orden + "): " + nombreCarta);
                } else {
                    System.out.println("mo se pudo cargar la i magen : " + rutaImagen);
                }
                indice++;
            }

            rs.close();
            st.close();

            String sqlLimpiar = "DROP TABLE #mazo";
            Statement stLimpiar = conn.createStatement();
            stLimpiar.executeUpdate(sqlLimpiar);
            stLimpiar.close();

            conn.close();

            System.out.println("bien");

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        tittleLoteria = new javax.swing.JLabel();
        carta1 = new javax.swing.JButton();
        carta2 = new javax.swing.JButton();
        carta3 = new javax.swing.JButton();
        carta4 = new javax.swing.JButton();
        carta5 = new javax.swing.JButton();
        carta6 = new javax.swing.JButton();
        carta7 = new javax.swing.JButton();
        carta8 = new javax.swing.JButton();
        carta9 = new javax.swing.JButton();
        carta10 = new javax.swing.JButton();
        carta11 = new javax.swing.JButton();
        carta12 = new javax.swing.JButton();
        carta13 = new javax.swing.JButton();
        carta14 = new javax.swing.JButton();
        carta15 = new javax.swing.JButton();
        carta16 = new javax.swing.JButton();
        btnIniciar = new javax.swing.JButton();
        btnReiniciar = new javax.swing.JButton();
        btnSalir = new javax.swing.JButton();
        cartaAcual = new javax.swing.JButton();
        btnSigCarta = new javax.swing.JButton();
        btnPausa = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(255, 231, 176));

        tittleLoteria.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        tittleLoteria.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        tittleLoteria.setText("Loteria");

        carta1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/detras.png"))); // NOI18N
        carta1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        carta1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                carta1MouseClicked(evt);
            }
        });
        carta1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                carta1ActionPerformed(evt);
            }
        });

        carta2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/detras.png"))); // NOI18N
        carta2.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        carta2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                carta2ActionPerformed(evt);
            }
        });

        carta3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/detras.png"))); // NOI18N
        carta3.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        carta3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                carta3ActionPerformed(evt);
            }
        });

        carta4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/detras.png"))); // NOI18N
        carta4.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        carta4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                carta4ActionPerformed(evt);
            }
        });

        carta5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/detras.png"))); // NOI18N
        carta5.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        carta5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                carta5ActionPerformed(evt);
            }
        });

        carta6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/detras.png"))); // NOI18N
        carta6.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        carta6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                carta6ActionPerformed(evt);
            }
        });

        carta7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/detras.png"))); // NOI18N
        carta7.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        carta7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                carta7ActionPerformed(evt);
            }
        });

        carta8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/detras.png"))); // NOI18N
        carta8.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        carta8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                carta8ActionPerformed(evt);
            }
        });

        carta9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/detras.png"))); // NOI18N
        carta9.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        carta9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                carta9ActionPerformed(evt);
            }
        });

        carta10.setIcon(new javax.swing.ImageIcon(getClass().getResource("/detras.png"))); // NOI18N
        carta10.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        carta10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                carta10ActionPerformed(evt);
            }
        });

        carta11.setIcon(new javax.swing.ImageIcon(getClass().getResource("/detras.png"))); // NOI18N
        carta11.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        carta11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                carta11ActionPerformed(evt);
            }
        });

        carta12.setIcon(new javax.swing.ImageIcon(getClass().getResource("/detras.png"))); // NOI18N
        carta12.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        carta12.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                carta12ActionPerformed(evt);
            }
        });

        carta13.setIcon(new javax.swing.ImageIcon(getClass().getResource("/detras.png"))); // NOI18N
        carta13.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        carta13.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                carta13ActionPerformed(evt);
            }
        });

        carta14.setIcon(new javax.swing.ImageIcon(getClass().getResource("/detras.png"))); // NOI18N
        carta14.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        carta14.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                carta14ActionPerformed(evt);
            }
        });

        carta15.setIcon(new javax.swing.ImageIcon(getClass().getResource("/detras.png"))); // NOI18N
        carta15.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        carta15.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                carta15ActionPerformed(evt);
            }
        });

        carta16.setIcon(new javax.swing.ImageIcon(getClass().getResource("/detras.png"))); // NOI18N
        carta16.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        carta16.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                carta16ActionPerformed(evt);
            }
        });

        btnIniciar.setText("Iniciar");
        btnIniciar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnIniciar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnIniciarActionPerformed(evt);
            }
        });

        btnReiniciar.setText("Reiniciar mazo");
        btnReiniciar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnReiniciar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnReiniciarActionPerformed(evt);
            }
        });

        btnSalir.setText("Salir al menú");
        btnSalir.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnSalir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSalirActionPerformed(evt);
            }
        });

        cartaAcual.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Cartatrasera.png"))); // NOI18N

        btnSigCarta.setText("Siguiente carta");
        btnSigCarta.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnSigCarta.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSigCartaActionPerformed(evt);
            }
        });

        btnPausa.setText("Pausar");
        btnPausa.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnPausa.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPausaActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(67, 67, 67)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(btnIniciar)
                            .addComponent(cartaAcual, javax.swing.GroupLayout.PREFERRED_SIZE, 235, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnPausa)
                            .addComponent(btnSalir)
                            .addComponent(btnReiniciar)))
                    .addComponent(btnSigCarta))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 56, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(tittleLoteria, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(29, 29, 29)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(carta1, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(carta5, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(carta9, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(carta13, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(carta2, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(carta6, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(carta10, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(carta14, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(carta3, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(carta7, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(carta11, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(carta15, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(carta4, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(carta8, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(carta12, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(carta16, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(33, 33, 33)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tittleLoteria)
                .addGap(6, 6, 6)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(carta3, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(carta4, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(carta1, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(carta2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(carta6, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(carta5, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(carta7, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(carta8, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(carta10, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(carta9, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(carta11, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(carta12, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(carta14, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(carta13, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(carta15, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(carta16, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(cartaAcual, javax.swing.GroupLayout.PREFERRED_SIZE, 410, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnIniciar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnPausa)
                        .addGap(42, 42, 42)
                        .addComponent(btnReiniciar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnSigCarta, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(btnSalir, javax.swing.GroupLayout.Alignment.TRAILING))))
                .addContainerGap(20, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void carta1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_carta1ActionPerformed

        procesarClickCarta(carta1);

    }//GEN-LAST:event_carta1ActionPerformed

    private void carta5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_carta5ActionPerformed

        procesarClickCarta(carta5);

    }//GEN-LAST:event_carta5ActionPerformed

    private void carta9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_carta9ActionPerformed

        procesarClickCarta(carta9);

    }//GEN-LAST:event_carta9ActionPerformed

    private void carta13ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_carta13ActionPerformed

        procesarClickCarta(carta13);

    }//GEN-LAST:event_carta13ActionPerformed

    private void btnIniciarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnIniciarActionPerformed

        if (reproductor != null) {
            reproductor.detener();
        }

        ReproductorDeSonido clipBoton = new ReproductorDeSonido();
        clipBoton.cargarSonido("src\\main\\resources\\button-124476.wav");
        clipBoton.reproducir();

        try {
            Thread.sleep(400);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }

        cargarTableroCompleto();
        cargarCartaActual();
        iniciarTimerCartas();

        //btnReiniciar.setVisible(true);
        btnIniciar.setVisible(false);
        btnPausa.setVisible(true);
        //btnSigCarta.setVisible(true);
    }//GEN-LAST:event_btnIniciarActionPerformed

    private void btnSalirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSalirActionPerformed

        if (reproductor != null) {
            reproductor.detener();
        }

        detenerTimerCartas();

        ReproductorDeSonido clipBoton = new ReproductorDeSonido();
        clipBoton.cargarSonido("src\\main\\resources\\button-124476.wav");
        clipBoton.reproducir();

        try {
            Thread.sleep(400);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }

        close();
        new Principal().setVisible(true);
    }//GEN-LAST:event_btnSalirActionPerformed

    private void btnReiniciarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnReiniciarActionPerformed

        if (reproductor != null) {
            reproductor.detener();
        }

        ReproductorDeSonido clipBoton = new ReproductorDeSonido();
        clipBoton.cargarSonido("src\\main\\resources\\button-124476.wav");
        clipBoton.reproducir();

        try {
            Thread.sleep(400);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }

        cargarTableroCompleto();

        //cargarCartaActual();reiniciarcarta actual
    }//GEN-LAST:event_btnReiniciarActionPerformed

    private void btnSigCartaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSigCartaActionPerformed
        if (reproductor != null) {
            reproductor.detener();
        }

        ReproductorDeSonido clipBoton = new ReproductorDeSonido();
        clipBoton.cargarSonido("src\\main\\resources\\button-124476.wav");
        clipBoton.reproducir();

        try {
            Thread.sleep(400);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }

        cargarCartaActual();
        btnReiniciar.setVisible(false);
    }//GEN-LAST:event_btnSigCartaActionPerformed

    private void carta1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_carta1MouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_carta1MouseClicked

    private void carta2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_carta2ActionPerformed

        procesarClickCarta(carta2);

    }//GEN-LAST:event_carta2ActionPerformed

    private void carta3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_carta3ActionPerformed

        procesarClickCarta(carta3);

    }//GEN-LAST:event_carta3ActionPerformed

    private void carta4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_carta4ActionPerformed

        procesarClickCarta(carta4);

    }//GEN-LAST:event_carta4ActionPerformed

    private void carta6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_carta6ActionPerformed

        procesarClickCarta(carta6);

    }//GEN-LAST:event_carta6ActionPerformed

    private void carta7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_carta7ActionPerformed

        procesarClickCarta(carta7);

    }//GEN-LAST:event_carta7ActionPerformed

    private void carta8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_carta8ActionPerformed

        procesarClickCarta(carta8);

    }//GEN-LAST:event_carta8ActionPerformed

    private void carta10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_carta10ActionPerformed

        procesarClickCarta(carta10);

    }//GEN-LAST:event_carta10ActionPerformed

    private void carta11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_carta11ActionPerformed

        procesarClickCarta(carta11);

    }//GEN-LAST:event_carta11ActionPerformed

    private void carta12ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_carta12ActionPerformed

        procesarClickCarta(carta12);

    }//GEN-LAST:event_carta12ActionPerformed

    private void carta14ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_carta14ActionPerformed

        procesarClickCarta(carta14);

    }//GEN-LAST:event_carta14ActionPerformed

    private void carta15ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_carta15ActionPerformed

        procesarClickCarta(carta15);

    }//GEN-LAST:event_carta15ActionPerformed

    private void carta16ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_carta16ActionPerformed

        procesarClickCarta(carta16);

    }//GEN-LAST:event_carta16ActionPerformed

    private void btnPausaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPausaActionPerformed

        if (reproductor != null) {
            reproductor.detener();
        }

        ReproductorDeSonido clipBoton = new ReproductorDeSonido();
        clipBoton.cargarSonido("src\\main\\resources\\button-124476.wav");
        clipBoton.reproducir();

        pausarReanudarTimer();

        String txtBtnPausa = btnPausa.getText();

        if (txtBtnPausa == "Pausar") {
            btnPausa.setText("Reanudar");
        } else if (txtBtnPausa == "Reanudar") {
            btnPausa.setText("Pausar");
        }

    }//GEN-LAST:event_btnPausaActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Juego.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Juego.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Juego.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Juego.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Juego().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnIniciar;
    private javax.swing.JButton btnPausa;
    private javax.swing.JButton btnReiniciar;
    private javax.swing.JButton btnSalir;
    private javax.swing.JButton btnSigCarta;
    private javax.swing.JButton carta1;
    private javax.swing.JButton carta10;
    private javax.swing.JButton carta11;
    private javax.swing.JButton carta12;
    private javax.swing.JButton carta13;
    private javax.swing.JButton carta14;
    private javax.swing.JButton carta15;
    private javax.swing.JButton carta16;
    private javax.swing.JButton carta2;
    private javax.swing.JButton carta3;
    private javax.swing.JButton carta4;
    private javax.swing.JButton carta5;
    private javax.swing.JButton carta6;
    private javax.swing.JButton carta7;
    private javax.swing.JButton carta8;
    private javax.swing.JButton carta9;
    private javax.swing.JButton cartaAcual;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel tittleLoteria;
    // End of variables declaration//GEN-END:variables
}
