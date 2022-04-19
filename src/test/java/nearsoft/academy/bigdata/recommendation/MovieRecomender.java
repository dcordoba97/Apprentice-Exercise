package nearsoft.academy.bigdata.recommendation;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.UserBasedRecommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

class MovieRecommender {
    UserBasedRecommender recommender;
    HashMap<Long, String> userIds = new HashMap<>();
    HashMap<String, Long> reverseUserIds = new HashMap<>();
    HashMap<Long, String> productIds = new HashMap<>();
    HashMap<String, Long> reverseProductIds = new HashMap<>();
    long userId = 0;
    long productId = 0;
    int totalReviews = 0;
    String csvPath;
    String csvLineUser = "";
    String csvLineProduct = "";
    public MovieRecommender(String moviesFilePath) throws IOException, TasteException {
        this.csvPath = System.getProperty("user.dir") + "/recommender.csv"; // Filepath of csv
        FileReader file = new FileReader(moviesFilePath);
        BufferedReader bufferR = new BufferedReader(file);
        FileWriter fileW = new FileWriter(this.csvPath);
        BufferedWriter bufferW = new BufferedWriter(fileW);

        String line = bufferR.readLine();
        while (line != null) {
            if  (line.contains("review/userId")) {
                String rowValue = line.split(":")[1].trim();
                if (reverseUserIds.containsKey(rowValue)) {
                    this.csvLineUser = reverseUserIds.get(rowValue).toString() + ','; // grabs userId for csv
                }   else {
                        userIds.put(userId, rowValue);
                        reverseUserIds.put(rowValue, userId);
                        userId++;
                        this.csvLineUser = reverseUserIds.get(rowValue).toString() + ','; // grabs userId for csv
                    }
            }   else if (line.contains("product/productId")) {
                    String rowValue = line.split(":")[1].trim();
                    if (reverseProductIds.containsKey(rowValue)) {
                        this.csvLineProduct = reverseProductIds.get(rowValue).toString() + ','; // grabs productId for csv
                    }   else {
                            productIds.put(productId, rowValue);
                            reverseProductIds.put(rowValue, productId);
                            productId++;
                            this.csvLineProduct = reverseProductIds.get(rowValue).toString() + ','; // grabs productId for csv
                            }
            }   else if (line.contains("review/score")) {
                    String rowValue = line.split(":")[1].trim();
                    bufferW.write(this.csvLineUser + this.csvLineProduct + rowValue + '\n'); //writes in csv
                    this.csvLineUser = "";
                    this.csvLineProduct = "";
                    totalReviews++;
            }
            line = bufferR.readLine();
        }
        this.csvPath = System.getProperty("user.dir") + "/recommender.csv";
        bufferW.close();
        file.close();
        createRecommender();
    }
    public int getTotalProducts() {
        return this.productIds.size();
    }

    public int getTotalReviews() {
        return this.totalReviews;
    }

    public int getTotalUsers() {
        return this.userIds.size();
    }
    private void createRecommender() throws IOException, TasteException {
        DataModel model = new FileDataModel(new File(this.csvPath));
        UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
        UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.1, similarity, model);
        this.recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);
    }
    public List<String> getRecommendationsForUser (String userId) throws TasteException, IOException {
        ArrayList<String> resultList = new ArrayList<>();
        long userIdLong = reverseUserIds.get(userId);
        List<RecommendedItem> recommendations = recommender.recommend(userIdLong, 3);
        for (RecommendedItem recommendation : recommendations) {
            resultList.add(productIds.get(recommendation.getItemID()));
        }
        return resultList;
    }
}


