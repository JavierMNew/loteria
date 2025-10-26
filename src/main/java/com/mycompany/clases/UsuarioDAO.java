package com.mycompany.clases;

import com.mycompany.conexionSQLServer.Conexion;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;

/**
 *   gestionar operaciones de usuarios en la db
 */
public class UsuarioDAO {
    
    /**
     * hash contraseña SHA-256
     */
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
    public boolean registrarUsuario(String nombreUsuario, String correo, String contrasena) {
        Connection conn = null;
        PreparedStatement pst = null;
        
        try {
            Conexion conexion = new Conexion();
            conn = conexion.establecerConexion();
            
            if (conn == null) {
                System.out.println("Error: No se pudo establecer conexión con la base de datos");
                return false;
            }
            
            String sqlVerificar = "SELECT COUNT(*) FROM usuarios WHERE correo = ? OR nombre_usuario = ?";
            pst = conn.prepareStatement(sqlVerificar);
            pst.setString(1, correo);
            pst.setString(2, nombreUsuario);
            ResultSet rs = pst.executeQuery();
            
            if (rs.next() && rs.getInt(1) > 0) {
                System.out.println("El correo o nombre de usuario ya existe");
                return false;
            }
            rs.close();
            pst.close();
            
            String sqlInsertar = "INSERT INTO usuarios (nombre_usuario, correo, contrasena) VALUES (?, ?, ?)";
            pst = conn.prepareStatement(sqlInsertar);
            pst.setString(1, nombreUsuario);
            pst.setString(2, correo);
            pst.setString(3, hashPassword(contrasena));
            
            int filasAfectadas = pst.executeUpdate();
            
            return filasAfectadas > 0;
            
        } catch (SQLException e) {
            System.out.println("Error al registrar usuario: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (pst != null) pst.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
   
    public int validarLogin(String correo, String contrasena) {
        Connection conn = null;
        PreparedStatement pst = null;
        
        try {
            Conexion conexion = new Conexion();
            conn = conexion.establecerConexion();
            
            if (conn == null) {
                System.out.println("Error: No se pudo establecer conexión con la base de datos");
                return -1;
            }
            
            String sql = "SELECT cve_usuario FROM usuarios WHERE correo = ? AND contrasena = ?";
            pst = conn.prepareStatement(sql);
            pst.setString(1, correo);
            pst.setString(2, hashPassword(contrasena));
            
            ResultSet rs = pst.executeQuery();
            
            if (rs.next()) {
                int idUsuario = rs.getInt("cve_usuario");
                
                String sqlActualizar = "UPDATE usuarios SET ultima_conexion = GETDATE() WHERE cve_usuario = ?";
                PreparedStatement pstUpdate = conn.prepareStatement(sqlActualizar);
                pstUpdate.setInt(1, idUsuario);
                pstUpdate.executeUpdate();
                pstUpdate.close();
                
                return idUsuario;
            }
            
            return -1;
            
        } catch (SQLException e) {
            System.out.println("Error al validar login: " + e.getMessage());
            e.printStackTrace();
            return -1;
        } finally {
            try {
                if (pst != null) pst.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    public boolean incrementarPuntuacion(int idUsuario) {
        Connection conn = null;
        PreparedStatement pst = null;
        
        try {
            Conexion conexion = new Conexion();
            conn = conexion.establecerConexion();
            
            if (conn == null) return false;
            
            String sqlUpdate = "UPDATE usuarios SET puntuacion = puntuacion + 1 WHERE cve_usuario = ?";
            pst = conn.prepareStatement(sqlUpdate);
            pst.setInt(1, idUsuario);
            int filasActualizadas = pst.executeUpdate();
            
            if (filasActualizadas > 0) {
                String sqlHistorial = "INSERT INTO historial_partidas (cve_usuario, puntos_ganados) VALUES (?, 1)";
                PreparedStatement pstHistorial = conn.prepareStatement(sqlHistorial);
                pstHistorial.setInt(1, idUsuario);
                pstHistorial.executeUpdate();
                pstHistorial.close();
            }
            
            return filasActualizadas > 0;
            
        } catch (SQLException e) {
            System.out.println("Error al incrementar puntuación: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (pst != null) pst.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    public int obtenerPuntuacion(int idUsuario) {
        Connection conn = null;
        PreparedStatement pst = null;
        
        try {
            Conexion conexion = new Conexion();
            conn = conexion.establecerConexion();
            
            if (conn == null) return 0;
            
            String sql = "SELECT puntuacion FROM usuarios WHERE cve_usuario = ?";
            pst = conn.prepareStatement(sql);
            pst.setInt(1, idUsuario);
            
            ResultSet rs = pst.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("puntuacion");
            }
            
            return 0;
            
        } catch (SQLException e) {
            System.out.println("Error al obtener puntuación: " + e.getMessage());
            e.printStackTrace();
            return 0;
        } finally {
            try {
                if (pst != null) pst.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    public String obtenerNombreUsuario(int idUsuario) {
        Connection conn = null;
        PreparedStatement pst = null;
        
        try {
            Conexion conexion = new Conexion();
            conn = conexion.establecerConexion();
            
            if (conn == null) return null;
            
            String sql = "SELECT nombre_usuario FROM usuarios WHERE cve_usuario = ?";
            pst = conn.prepareStatement(sql);
            pst.setInt(1, idUsuario);
            
            ResultSet rs = pst.executeQuery();
            
            if (rs.next()) {
                return rs.getString("nombre_usuario");
            }
            
            return null;
            
        } catch (SQLException e) {
            System.out.println("Error al obtener nombre de usuario: " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (pst != null) pst.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}