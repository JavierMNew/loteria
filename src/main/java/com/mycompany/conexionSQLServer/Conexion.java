/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.conexionSQLServer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author javie
 */
public class Conexion {

    Connection conectar = null;

    String usuario = "loteria";
    String contrasenia = "LolasO5566";
    String bd = "laloteria";
    String ip = "localhost";
    String puerto = "1433";

    String cadena = "jdbc:sqlserver://" + puerto + ";" + "databaseName=" + bd;

    public Connection establecerConexion() {
        try {
            String cadena = "jdbc:sqlserver://localhost:" + puerto + ";" + "databaseName=" + bd + ";trustServerCertificate=true;";
            conectar = DriverManager.getConnection(cadena, usuario, contrasenia);
        } catch (Exception e) {
            System.out.println("error de conn" + e.getMessage());
            conectar = null;
        }
        return conectar;
    }


    /*
    private static final String BASE_URL = "jdbc:sqlserver://localhost:1433";
    private static final String USER = "loteria";
    private static final String PASSWORD = "LolasO5566";

    public static Connection getConnection(String loteria) throws SQLException {
        
        
        String url = BASE_URL + ";laloteria=" + loteria + ";trustServerCertificate=true";
        return DriverManager.getConnection(url, USER, PASSWORD);
    }*/
}
