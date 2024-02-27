package config;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;

public class DatabaseConnection {

    public static String getProduct(String idProduct){

        //vairables para conexión
        String ip = "";
        String user= "";
        String password="";
        String database = "";
        int contLine= 0;
        String separator = "";
        String productName="";
        separator = System.getProperty("file.separator");

        String filePath = "C:"+separator+"config.txt";
        try (BufferedReader readerLocal = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = readerLocal.readLine()) != null) {
                contLine++;
                switch (contLine){
                    case 1:
                        ip = line;
                        break;
                    case 2:
                        user = line;
                        break;
                    case 3:
                        password = line;
                        break;
                    case 4:
                        database = line;
                        break;

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        String connectionUrl = "jdbc:sqlserver://"+ip+":1433;" +
                "sslProtocol=TLSv1.2;"+
                "databaseName="+database+";" +
                "user="+user+";" +
                "password="+password+";" +
                "loginTimeout=60;"+
                "encrypt=false;"+
                "TrustServerCertificate=True;";

        try (Connection connection = DriverManager.getConnection(connectionUrl);
             Statement statement = connection.createStatement()) {

            //busqueda del producto
            String selectSql ="SELECT ITM_ID, RCPT_DESCR FROM [dbo].[PLU]"+
                    "WHERE ITM_ID IN ('"+idProduct+"')";
            ResultSet resultSet = statement.executeQuery(selectSql);

            while (resultSet.next()) {
                productName= resultSet.getString(2);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return productName;

    }

}
