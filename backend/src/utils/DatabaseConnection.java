package utils;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import exceptions.DatabaseException;

public class DatabaseConnection {

    private static DatabaseConnection instance;
    private MongoClient mongoClient;
    private MongoDatabase database;
    private static final String DATABASE_NAME = "financeDB";

    private DatabaseConnection() throws DatabaseException {
        try {
            String mongoUri = System.getenv("MONGODB_URI");

            if (mongoUri == null || mongoUri.isEmpty()) {
                mongoUri = "mongodb://localhost:27017";
                Logger.log("Using local MongoDB connection");
            } else {
                Logger.log("Connecting to MongoDB Atlas");
            }

            mongoClient = MongoClients.create(mongoUri);
            database = mongoClient.getDatabase(DATABASE_NAME);

            Logger.log(
                "Successfully connected to MongoDB database: " + DATABASE_NAME
            );
        } catch (Exception e) {
            Logger.logError("Failed to connect to MongoDB: " + e.getMessage());
            throw new DatabaseException(
                "Failed to establish database connection",
                e
            );
        }
    }

    public static synchronized DatabaseConnection getInstance()
        throws DatabaseException {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public static void initialize() throws DatabaseException {
        getInstance();
    }

    public MongoDatabase getDatabase() {
        return database;
    }

    public void close() {
        if (mongoClient != null) {
            mongoClient.close();
            Logger.log("MongoDB connection closed");
        }
    }
}
