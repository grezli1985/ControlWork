package Services;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import Model.*;

public class PetRepository implements IRepository<Pet> {

    private final Creator petCreator;
    private ResultSet resultSet;
    private String SQLst;

    public PetRepository() {
        this.petCreator = new PetCreator();
    }

    @Override
    public List<Pet> getAll() {
        List<Pet> farm = new ArrayList<>();
        Pet pet;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection dbConnection = getConnection()) {
                Statement sqlSt = dbConnection.createStatement();
                SQLst = "SELECT  Name, Command, Birthday FROM newfriendHuman ORDER BY id";
                resultSet = sqlSt.executeQuery(SQLst);
                while (resultSet.next()) {

                    pet = getPet();
                    farm.add(pet);
                }
                return farm;
            }
        } catch (ClassNotFoundException | IOException | SQLException ex) {
            Logger.getLogger(PetRepository.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex.getMessage());
        }
    }

    private Pet getPet() throws SQLException {
        Pet pet;
        PetType type = PetType.getType(resultSet.getInt(1));
        int id = resultSet.getInt(2);
        String name = resultSet.getString(3);
        LocalDate birthday = resultSet.getDate(4).toLocalDate();

        pet = petCreator.createPet(type, name, birthday);
        pet.setPetId(id);
        return pet;
    }

    @Override
    public Pet getById(int petId) {
        Pet pet = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection dbConnection = getConnection()) {

                SQLst = "SELECT Name, Command, Birthday FROM newfriendHuman WHERE id = ?";
                PreparedStatement prepSt = dbConnection.prepareStatement(SQLst);
                prepSt.setInt(1, petId);
                resultSet = prepSt.executeQuery();

                if (resultSet.next()) {

                    pet = getPet();
                }
                return pet;
            }
        } catch (ClassNotFoundException | IOException | SQLException ex) {
            Logger.getLogger(PetRepository.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex.getMessage());
        }
    }

    @Override
    public int create(Pet pet) {
        int rows;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection dbConnection = getConnection()) {

                SQLst = "INSERT INTO newfriendHuman (Name, Command, Birthday) SELECT ?, ?, (SELECT id FROM newfriendHuman WHERE Name = ?)";
                PreparedStatement prepSt = dbConnection.prepareStatement(SQLst);
                prepSt.setString(1, pet.getName());
                prepSt.setDate(2, Date.valueOf(pet.getBirthdayDate()));
                prepSt.setString(3, pet.getClass().getSimpleName());

                rows = prepSt.executeUpdate();
                return rows;
            }
        } catch (ClassNotFoundException | IOException | SQLException ex) {
            Logger.getLogger(PetRepository.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex.getMessage());
        }
    }

    public void train(int id, String command) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection dbConnection = getConnection()) {
                String SQLstr = "INSERT INTO newfriendHuman (Name, Command, Birthday) SELECT ?, (SELECT id FROM Command WHERE Command = ?)";
                PreparedStatement prepSt = dbConnection.prepareStatement(SQLstr);
                prepSt.setInt(1, id);
                prepSt.setString(2, command);

                prepSt.executeUpdate();
            }
        } catch (ClassNotFoundException | IOException | SQLException ex) {
            Logger.getLogger(PetRepository.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex.getMessage());
        }
    }

    public List<String> getCommandsById(int petId, int commands_type) {

        // commands type = 1 - получить команды, выполняемые питомцем, 2 - команды,
        // выполнимые животным того рода, к которому относится питомец

        List<String> commands = new ArrayList<>();
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection dbConnection = getConnection()) {
                if (commands_type == 1) {
                    SQLst = "SELECT Command_name FROM pet_command pc JOIN commands c ON pc.CommandId = c.Id WHERE pc.PetId = ?";
                } else {
                    SQLst = "SELECT Command_name FROM commands c JOIN Genus_command gc ON c.Id = gc.CommandId WHERE gc.GenusId = (SELECT GenusId FROM pet_list WHERE Id = ?)";
                }
                PreparedStatement prepSt = dbConnection.prepareStatement(SQLst);
                prepSt.setInt(1, petId);
                resultSet = prepSt.executeQuery();
                while (resultSet.next()) {
                    commands.add(resultSet.getString(1));
                }
                return commands;
            }
        } catch (ClassNotFoundException | IOException | SQLException ex) {
            Logger.getLogger(PetRepository.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex.getMessage());
        }
    }

    @Override
    public int update(Pet pet) {
        int rows;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection dbConnection = getConnection()) {
                SQLst = "UPDATE newfriendHuman SET Name = ?, Birthday = ? WHERE Id = ?";
                PreparedStatement prepSt = dbConnection.prepareStatement(SQLst);

                prepSt.setString(1, pet.getName());
                prepSt.setDate(2, Date.valueOf(pet.getBirthdayDate()));
                prepSt.setInt(3, pet.getPetId());

                rows = prepSt.executeUpdate();
                return rows;
            }
        } catch (ClassNotFoundException | IOException | SQLException ex) {
            Logger.getLogger(PetRepository.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex.getMessage());
        }
    }

    @Override
    public void delete(int id) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection dbConnection = getConnection()) {
                SQLst = "DELETE FROM newfriendHuman WHERE Id = ?";
                PreparedStatement prepSt = dbConnection.prepareStatement(SQLst);
                prepSt.setInt(1, id);
                prepSt.executeUpdate();
            }
        } catch (ClassNotFoundException | IOException | SQLException ex) {
            Logger.getLogger(PetRepository.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex.getMessage());
        }
    }

    public static Connection getConnection() throws SQLException, IOException {

        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream("System/src/Resources/database.properties")) {

            props.load(fis);
            String url = props.getProperty("url");
            String username = props.getProperty("username");
            String password = props.getProperty("password");

            return DriverManager.getConnection(url, username, password);
        }
    }
}