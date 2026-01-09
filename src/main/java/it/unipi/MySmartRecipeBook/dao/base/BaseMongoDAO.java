package it.unipi.MySmartRecipeBook.dao.base;
//connessione con mongoDB
//è così di default?


import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ReadPreference;
import com.mongodb.WriteConcern;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class BaseMongoDAO {

    public MongoDatabase mongoDB;
    protected MongoClient client; // It represents a pool of connections to the database, also for replicas.
    public static final String MONGO_PRIMARY_HOST = "10.1.1.55"; //10.1.1.55
    public static final Integer MONGO_PRIMARY_HOST_PORT = 27018;
    public static String MONGO_SECONDARY_HOST = "10.1.1.54";//"10.1.1.54";
    public static int MONGO_SECONDARY_HOST_PORT = 27018;
    public static String MONGO_THIRD_HOST = "10.1.1.48";//"10.1.1.48";
    public static int MONGO_THIRD_HOST_PORT = 27018;
    public static final String MONGO_DATABASE_NAME = "BeansBet";
    public static final String DB_URL = "mongodb://" + MONGO_PRIMARY_HOST + ":" + MONGO_PRIMARY_HOST_PORT;

    public BaseMongoDAO() {
        this.client = null;
    }


}
