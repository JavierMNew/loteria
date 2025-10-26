package com.mycompany.clases;

public class SesionUsuario {
    private static SesionUsuario instancia;
    private int idUsuario;
    private String nombreUsuario;
    private int puntuacion;
    private boolean sesionActiva;
    
    private SesionUsuario() {
        this.sesionActiva = false;
    }
    
    public static SesionUsuario getInstancia() {
        if (instancia == null) {
            instancia = new SesionUsuario();
        }
        return instancia;
    }
    
    public void iniciarSesion(int idUsuario, String nombreUsuario) {
        this.idUsuario = idUsuario;
        this.nombreUsuario = nombreUsuario;
        this.sesionActiva = true;
        
        UsuarioDAO dao = new UsuarioDAO();
        this.puntuacion = dao.obtenerPuntuacion(idUsuario);
    }
    
    public void cerrarSesion() {
        this.idUsuario = 0;
        this.nombreUsuario = null;
        this.puntuacion = 0;
        this.sesionActiva = false;
    }
    
    public boolean isSesionActiva() {
        return sesionActiva;
    }
    
    public int getIdUsuario() {
        return idUsuario;
    }
    
    public String getNombreUsuario() {
        return nombreUsuario;
    }
    
    public int getPuntuacion() {
        return puntuacion;
    }
    
    public void incrementarPuntuacion() {
        UsuarioDAO dao = new UsuarioDAO();
        if (dao.incrementarPuntuacion(idUsuario)) {
            this.puntuacion++;
        }
    }
    
    public void actualizarPuntuacion() {
        UsuarioDAO dao = new UsuarioDAO();
        this.puntuacion = dao.obtenerPuntuacion(idUsuario);
    }
}