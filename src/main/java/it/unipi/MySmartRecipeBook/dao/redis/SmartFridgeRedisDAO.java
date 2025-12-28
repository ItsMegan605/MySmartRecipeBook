package it.unipi.MySmartRecipeBook.dao.redis;
import it.unipi.MySmartRecipeBook.dao.SmartFridgeDAO;
import it.unipi.MySmartRecipeBook.model.SmartFridge;
import com.google.gson.Gson;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

//prova di modifica
//megan puzza, ma chiara di più
public class SmartFridgeRedisDAO  implements SmartFridgeDAO {

//questo sarebbe il DAO (Data Access Object): serve per salvare e recuperare dati dal database

    private static final JedisPool pool; //mi connetto a redis con nuovi pool connessione
    private static final String REDIS_HOST = "localhost";
    private static final Integer REDIS_PORT = 6379; //collegamento con la porta ho lasciato quella del prof
    private static final String APP_NS = "smart-fridge"; //namespace mi sono rifatta al prof, questo è il prefisso
    //NS = application namespace
    static { //pool di connessioni per evitare di dover chiudere e aprire ogni volta
        pool = new JedisPool(REDIS_HOST, REDIS_PORT);
    }
    //adesso genero le chiavi

    private static String SmartFridgeIngredientKeyNS(Integer userId) { //per ora ho messo cosa mi sembrava lopgico da wuello del prof
        return APP_NS + ":" + userId + ":ingredients"; //smart-fridge:10:ingredients
    }

    private static String SmartFridgeUpdateDateKeyNS(Integer userId) { //potrebbe essere per quando aggiungo gli inredienti(?)
        return APP_NS + ":" + userId + ":updatedDate"; //smart-fridge:10:updatedDate
    }

    //salvo i dati
    public static void persist(SmartFridge smartFridge) {
        Gson gson = new Gson();
        Integer userId = smartFridge.getUserId(); //converto in json
        String smartFridgeIngredientKey = SmartFridgeIngredientKeyNS(userId);
        String updateDateKey = SmartFridgeUpdateDateKeyNS(userId);
        System.out.println("SmartFridge ingredients items key: " + smartFridgeIngredientKey);
        System.out.println("SmartFridge updatedDateKey key: " + updateDateKey);
        try (Jedis jedis = pool.getResource()) { //prendo connessione dal pool blocco try-catch per errore
            jedis.set(smartFridgeIngredientKey, gson.toJson(smartFridge));
            jedis.set(updateDateKey, System.currentTimeMillis() + "");
            jedis.expire(smartFridgeIngredientKey, 60); //cancellamento contenuti, probabilmente dq togliere(?)
        }
    }

    //carico i dati
    public static SmartFridge load(Integer userId) {
        Gson gson = new Gson(); //opposto di prima
        String smartFridgeIngredientKey = SmartFridgeIngredientKeyNS(userId);
        System.out.println("Shopping cart key: " + smartFridgeIngredientKey);
        try (Jedis jedis = pool.getResource()) {
            String data = jedis.get(smartFridgeIngredientKey);
            if (data != null){
                System.out.println("Data from Redis: " + data);
                return gson.fromJson(data, SmartFridge.class);
            }
        }
        return null;
    }

    //chiusura connessione
    public static void closePool(){
        if (!pool.isClosed()){
            pool.close();
        }
    }

}
